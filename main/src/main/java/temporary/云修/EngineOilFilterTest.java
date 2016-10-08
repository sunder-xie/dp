package temporary.云修;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 云修机油滤清器处理
 * Created by huangzhangting on 16/6/7.
 */
@Deprecated
public class EngineOilFilterTest extends BaseTest{
    private static final int YX_CODE = 1; //云修机滤
    private static final int BS_CODE = 2; //博世机滤
    private static final int AC_CODE = 3; //AC德科机滤
    private static final int JG_CODE = 4; //箭冠机滤
    private static final int HY_CODE = 5; //海业机滤

    private static final int AS_CODE = 8; //奥盛机滤


    private static final double MAX_POWER_OFFSET = 3; //功率差值

    private static int type = 0;

    //特殊的车型
    private static List<String> spCarModels = new ArrayList<String>(){{
        add("Escape/Kuga [翼虎]");
    }};


    private String getCarInfoSql(){
        Set<String> fieldSet = new HashSet<>();
        fieldSet.add("leyel_id");
        fieldSet.add("car_brand");
        fieldSet.add("factory_name");
        fieldSet.add("car_series");
        fieldSet.add("vehicle_type");
        fieldSet.add("model_year");
        fieldSet.add("displacement");
        fieldSet.add("intake_style");
        fieldSet.add("fuel_type");
        fieldSet.add("max_power");
        fieldSet.add("create_year");
        fieldSet.add("transmission_type");

        StringBuilder sql = new StringBuilder();
        for(String field : fieldSet){
            sql.append(",").append(field);
        }
        sql.deleteCharAt(0);
        sql.insert(0, "select ");
        sql.append(" from db_car_info_all");

        return sql.toString();
    }

    private Map<String, String> getAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("序号", "index");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");
        attrMap.put("年款", "year");
        attrMap.put("排量", "power");
        attrMap.put("进气形式", "inletType");
        attrMap.put("最大功率", "maxPower");
        attrMap.put("燃料类型", "fuelType");
        attrMap.put("生产年份", "createYear");
        attrMap.put("变速器类型", "transmissionType");

        //没有参与比较的属性
        attrMap.put("车系（原）", "car_series");
        attrMap.put("车型（原）", "car_model");
        attrMap.put("年款（年/月）", "car_year");
        attrMap.put("发动机", "car_engine");
        attrMap.put("功率(KW)", "car_power");
        attrMap.put("机油滤清器", "oil_filter");
        attrMap.put("尺寸", "goods_size");

        attrMap.put("产量名称", "goods_name");

        return attrMap;
    }

    // TODO 处理原始数据，生成力洋id和商品编号的对应关系
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/";

        String excel = path + "机滤正确数据总表整理版20160926（奥盛版）.xlsx";

        type = AS_CODE;

        int sheet = 1;

        CommReaderXLSX readerXLSX = new CommReaderXLSX(getAttrMap(), Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, sheet);

        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

//        if(true){
//            return;
//        }

        List<Map<String, Object>> carInfoList = commonMapper.selectListBySql(getCarInfoSql());

        Print.info(carInfoList.size());
        Print.info("力洋车型："+carInfoList.get(0));

        init();

        List<Map<String, String>> unMatchList = new ArrayList<>();

        for(Map<String, String> data : dataList){
            if(StringUtils.isEmpty(data.get("goodsFormat"))
                    || StringUtils.isEmpty(data.get("brand"))
                    || StringUtils.isEmpty(data.get("company"))
                    || StringUtils.isEmpty(data.get("series"))){

                Print.info("暂不处理的数据："+data);
            }else{
                handleData(data, carInfoList, unMatchList);
            }
        }

        IoUtil.closeWriter(writer);

        Print.info("没有匹配上的数据："+unMatchList.size());

        //导出没有匹配上的数据
        exportExcel(unMatchList);
    }

    private void init(){
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = path+"sql/insert_goods_lyid_rel_"+dateStr+"_"+type+".sql";
        writer = IoUtil.getWriter(sqlFile);
    }

    //校验数据
    private boolean checkData(List<Map<String, String>> dataList){
        Map<String, String> map = new HashMap<>();
        Set<String> set = new HashSet<>();
        for(Map<String, String> data : dataList){
            String goodsFormat = data.get("goodsFormat");
            String goods_size = data.get("goods_size");

            String goodsSize = map.get(goodsFormat);
            if(goodsSize == null){
                map.put(goodsFormat, goods_size);
            }else{
                if(!goodsSize.equals(goods_size)){
                    set.add(goodsFormat);
                    Print.info("存在同一个云修号，不同尺寸数据，云修号："+goodsFormat+"  尺寸1："+goodsSize+"  尺寸2："+goods_size);
                }
            }
        }

        return set.isEmpty();
    }

    //处理数据
    private void handleData(Map<String, String> data, List<Map<String, Object>> carInfoList, List<Map<String, String>> unMatchList){
        String brand = data.get("brand");
        String company = data.get("company");
        String series = data.get("series");

        Set<String> modelSet = getAttrSet(data.get("model")); //车型
        Set<String> powerSet = getAttrSet(data.get("power")); //排量

        String inletType = null; //进气形式
        if(powerSet!=null){ //如果没有排量，则不考虑进气形式
            inletType = data.get("inletType");
        }

        String maxPower = data.get("maxPower"); //最大功率
        String fuelType = data.get("fuelType"); //燃料类型
        String transmissionType = data.get("transmissionType"); //变速器

        //年款
        String year = data.get("year");
        String startYear = getStartYear(year);
        String endYear = getEndYear(year);

        //生产年份
        String createYear = data.get("createYear");
        String createYearStart = getStartYear(createYear);
        String createYearEnd = getEndYear(createYear);

        Set<String> lyIdSet = new HashSet<>();
        for(Map<String, Object> car : carInfoList){
            if(brand.equals(car.get("car_brand").toString())
                    && company.equals(car.get("factory_name").toString())
                    && series.equals(car.get("car_series").toString())){

                if(compareAttrs(modelSet, car.get("vehicle_type"))
                        && compareAttrs(powerSet, car.get("displacement"))
                        && compareInletType(inletType, car)
                        && compareMaxPower(maxPower, car)
                        && compareFuelType(fuelType, car)){

                    if(compareYear(year, startYear, endYear, car.get("model_year"))
                            && compareYear(createYear, createYearStart, createYearEnd, car.get("create_year"))){

                        if(compareTransmissionType(transmissionType, car)) {
                            lyIdSet.add(car.get("leyel_id").toString());
                        }
                    }
                }

            }
        }

        if(lyIdSet.isEmpty()){
            Print.info("数据错误："+data);
            unMatchList.add(data);
        }else{
            handleSql(data.get("goodsFormat"), lyIdSet);
        }
    }

    //比较变速器类型
    private boolean compareTransmissionType(String type, Map<String, Object> car){
        if(StringUtils.isEmpty(type)){
            return true;
        }
        Object obj = car.get("transmission_type");
        if(obj==null){
            return true;
        }
        //String str = obj.toString().replaceAll("[^A-Z]", "");
        String str = obj.toString().trim();
        if("".equals(str)){
            return true;
        }

        return type.equals(str);
    }

    //比较进气形式
    private boolean compareInletType(String inletType, Map<String, Object> car){
        if(StringUtils.isEmpty(inletType)){
            return true;
        }
        Object obj = car.get("intake_style");
        if(obj==null){
            return true;
        }
        String str = obj.toString().trim();
        if("".equals(str)){
            return true;
        }

        if("自然吸气".equals(str)){
            return "L".equals(inletType);
        }else{
            return "T".equals(inletType);
        }
    }

    //燃料类型
    private boolean compareFuelType(String fuelType, Map<String, Object> car){
        if(StringUtils.isEmpty(fuelType)){
            return true;
        }
        Object obj = car.get("fuel_type");
        if(obj==null){
            return true;
        }
        String str = obj.toString().trim();
        if("".equals(str)){
            return true;
        }
        return str.contains(fuelType);
    }

    //比较最大功率
    private boolean compareMaxPower(String maxPower, Map<String, Object> car){
        if(StringUtils.isEmpty(maxPower)){
            return true;
        }
        Object obj = car.get("max_power");
        if(obj==null){
            return true;
        }
        String str = obj.toString().trim();
        if("".equals(str)){
            return true;
        }

        if(maxPower.equals(str)){
            return true;
        }

        try{
            double mp1 = Double.parseDouble(maxPower);
            double mp2 = Double.parseDouble(str);

            return Math.abs(mp1 - mp2) <= MAX_POWER_OFFSET;

        }catch (Exception e){
            Print.info(e.getMessage());
        }

        return false;
    }

    //比较年款
    private boolean compareYear(String yearStr, String startYear, String endYear, Object obj){
        if(StringUtils.isEmpty(yearStr)){
            return true;
        }
        if(obj==null){
            return false;
        }
        String year = obj.toString().trim();
        if("".equals(year)){
            return false;
        }
        if(year.compareTo(startYear)<0){
            return false;
        }
        if(endYear==null){
            return true;
        }
        return year.compareTo(endYear)<=0;
    }

    //比较有多个值的属性
    private boolean compareAttrs(Set<String> attrs, Object val){
        if(attrs==null){
            return true;
        }
        if(val==null){
            return false;
        }
        String str = val.toString().trim();
        if("".equals(str)){
            return false;
        }
        return attrs.contains(str);
    }

    //有多个值的属性，封装成set
    private Set<String> getAttrSet(String attrs){
        if(attrs==null){
            return null;
        }
        attrs = attrs.trim();
        if("".equals(attrs)){
            return null;
        }
        Set<String> set = new HashSet<>();

        //特殊车型
        if(spCarModels.contains(attrs)){
            set.add(attrs);
            return set;
        }

        String[] ps = attrs.split("/");
        for(int i=0; i<ps.length; i++){
            set.add(ps[i]);
        }
        return set;
    }

    private String getStartYear(String year){
        if(StringUtils.isEmpty(year)){
            return null;
        }
        int idx = year.indexOf("-");
        if(idx>0){
            return year.substring(0, idx).trim();
        }
        return year.trim();
    }
    private String getEndYear(String year){
        if(StringUtils.isEmpty(year)){
            return null;
        }
        String[] ys = year.split("-");
        if(ys.length==1){
            return null;
        }
        return ys[1].trim();
    }


    private void handleSql(String goodsFormat, Set<String> lyIdSet){
        Print.info(goodsFormat+" : "+lyIdSet.size());

        StringBuilder sb = new StringBuilder();
        for(String lyId : lyIdSet){
            sb.append(",");
            sb.append("('").append(goodsFormat);
            sb.append("', '").append(lyId).append("', ");
            sb.append(type).append(")");
        }
        sb.deleteCharAt(0);

        writeSql(sb);
    }

    private void writeSql(StringBuilder sb){
        sb.insert(0, "insert ignore into temp_goods_lyid_rel(goods_format, ly_id, brand_code) values");
        sb.append(";\n");
        IoUtil.writeFile(writer, sb.toString());
    }


    //TODO 处理云修商品
    @Test
    public void testGoods() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/云修机滤数据处理/";
        String excel = path + "云修机油滤清器.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "id");
        attrMap.put("new_goods_sn", "sn");
        attrMap.put("goods_name", "name");
        attrMap.put("goods_format", "format");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        if(!checkGoods(dataList)){
            //return;
        }

        Print.info("开始处理云修商品");

        //TODO 暂时不处理的云修号
        Set<String> ignoreFormatSet = new HashSet<>();
        ignoreFormatSet.add("YO-6974");

        writer = IoUtil.getWriter(path + "update_goods_sn.sql");

        for(Map<String, String> data : dataList){
            String format = data.get("format");
            if(!ignoreFormatSet.contains(format)){
                Print.info("云修编号："+format);
                String sql = "update temp_goods_lyid_rel set goods_sn='"+data.get("sn")
                        +"' where goods_format='"+format+"';\n";

                IoUtil.writeFile(writer, sql);
            }
        }

        IoUtil.closeWriter(writer);
    }

    //校验商品
    public boolean checkGoods(List<Map<String, String>> dataList){
        Set<String> snSet = new HashSet<>();
        Set<String> formatSet = new HashSet<>();
        boolean checkFlag = true;
        for(Map<String, String> data : dataList){
            String sn = data.get("sn");
            if(!snSet.add(sn)){
                Print.info("存在重复的goodsSn："+sn);
                checkFlag = false;
            }
            String format = data.get("format");
            if(!formatSet.add(format)){
                Print.info("存在重复的goodsFormat："+format);
                checkFlag = false;
            }
        }

        return checkFlag;
    }




    // TODO 转化成db_goods_car数据
    @Test
    public void testGC() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云修滤清器处理/";

        String carExcel = path + "tq车型-滤清器-1.xls";
        Map<String, String> carAttrMap = new HashMap<>();
        carAttrMap.put("t1.goods_format", "goods_format");
        carAttrMap.put("t2.car_models_id", "car_id");
        carAttrMap.put("t2.car_models", "car_name");
        carAttrMap.put("t2.year", "year");
        carAttrMap.put("t2.year_id", "year_id");
        carAttrMap.put("t2.power", "power");
        carAttrMap.put("t2.power_id", "power_id");
        carAttrMap.put("t2.model", "model");
        carAttrMap.put("t2.model_id", "model_id");
        carAttrMap.put("t2.series", "series");
        carAttrMap.put("t2.series_id", "series_id");
        carAttrMap.put("t2.brand", "brand");
        carAttrMap.put("t2.brand_id", "brand_id");

        CommReaderXLS readerXLS = new CommReaderXLS(carAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(carExcel, carAttrMap.size());
        List<Map<String, String>> carInfoList = readerXLS.getDataList();
        Print.info(carInfoList.size());

        String goodsExcel = path + "云修机油滤清器.xls";
        Map<String, String> goodsAttrMap = new HashMap<>();
        goodsAttrMap.put("goods_id", "goods_id");
        goodsAttrMap.put("goods_format", "goods_format");

        readerXLS = new CommReaderXLS(goodsAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(goodsExcel, 3);
        Map<String, String> goodsMap = new HashMap<>();
        for(Map<String, String> goods : readerXLS.getDataList()){
            goodsMap.put(goods.get("goods_format"), goods.get("goods_id"));
        }
        Print.info(goodsMap);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "addGoodsCar_1_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleSql(carInfoList, goodsMap);

        IoUtil.closeWriter(writer);
    }

    public void handleSql(List<Map<String, String>> carInfoList, Map<String, String> goodsMap){
        int count = 500;
        int size = carInfoList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addGcVal(sb, carInfoList.get(i), goodsMap);
            if((i+1)%count==0){
                writeGcSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIdx==i){
                writeGcSql(sb);
            }
            sb.append(",");
        }
    }

    public void addGcVal(StringBuilder sb, Map<String, String> data, Map<String, String> goodsMap){
        String gf = data.get("goods_format");
        if(gf.equals("YO-6811")){
            gf = "YO-6881";
        }

        sb.append("(");
        sb.append(goodsMap.get(gf)).append(",");
        sb.append(data.get("car_id")).append(",'");
        sb.append(data.get("car_name")).append("',");
        sb.append(data.get("brand_id")).append(",'");
        sb.append(data.get("brand")).append("',");
        sb.append(data.get("series_id")).append(",'");
        sb.append(data.get("series")).append("',");
        sb.append(data.get("model_id")).append(",'");
        sb.append(data.get("model")).append("',");
        sb.append(data.get("power_id")).append(",'");
        sb.append(data.get("power")).append("',");
        sb.append(data.get("year_id")).append(",'");
        sb.append(data.get("year")).append("',1,@nowTime)");

    }
    public void writeGcSql(StringBuilder sb){
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("insert ignore into db_goods_car");
        sqlSb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sqlSb.append(" values ").append(sb).append(";\n");

        IoUtil.writeFile(writer, sqlSb.toString());
    }



    //TODO 到处excel
    private void exportExcel(List<Map<String, String>> dataList){
        PoiUtil poiUtil = new PoiUtil();

        String[] heads = null;
        String[] fields = null;
        String fileName = null;

        if(type==YX_CODE){ //云修机滤
            fileName = "云修机滤没有匹配上的数据";

            heads = new String[]{"序号", "车系（原）", "车型（原）", "年款（年/月）", "发动机", "功率(KW)", "机油滤清器", "尺寸",
                    "品牌", "厂家", "车系", "车型", "年款", "排量", "进气形式", "最大功率", "燃料类型", "商品编码"};

            fields = new String[]{"index", "car_series", "car_model", "car_year", "car_engine", "car_power", "oil_filter", "goods_size",
                    "brand", "company", "series", "model", "year", "power", "inletType", "maxPower", "fuelType", "goodsFormat"};

        }else if(type==BS_CODE){ //博世机滤
            fileName = "博世机滤没有匹配上的数据";

            heads = new String[]{"序号", "尺寸", "商品编码",
                    "品牌", "厂家", "车系", "车型", "年款", "排量", "进气形式", "生产年份"};

            fields = new String[]{"index", "goods_size", "goodsFormat",
                    "brand", "company", "series", "model", "year", "power", "inletType", "createYear"};

        }else if(type==AC_CODE){ //AC德科机滤
            fileName = "AC德科机滤没有匹配上的数据";

            heads = new String[]{"序号", "商品编码",
                    "品牌", "厂家", "车系", "车型", "排量", "进气形式", "生产年份"};

            fields = new String[]{"index", "goodsFormat",
                    "brand", "company", "series", "model", "power", "inletType", "createYear"};

        }else if(type==JG_CODE){ //箭冠滤清器
            fileName = "箭冠滤清器没有匹配上的数据";

            heads = new String[]{"序号", "商品编码",
                    "品牌", "厂家", "车系", "车型", "排量", "年款", "进气形式", "变速器类型"};

            fields = new String[]{"index", "goodsFormat",
                    "brand", "company", "series", "model", "power", "year", "inletType", "transmissionType"};

        }else if(type==HY_CODE){ //海业滤清器
            fileName = "海业滤清器没有匹配上的数据";

            heads = new String[]{"序号", "商品编码", "产量名称",
                    "品牌", "厂家", "车系", "车型", "排量", "年款", "进气形式", "最大功率"};

            fields = new String[]{"index", "goodsFormat", "goods_name",
                    "brand", "company", "series", "model", "power", "year", "inletType", "maxPower"};

        } else {
            dataList = new ArrayList<>();
        }

        try {
            poiUtil.exportXlsxWithMap(fileName, path+"数据处理后/", heads, fields, dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
