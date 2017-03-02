package 机油滤清器处理.商品车型数据处理;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import org.springframework.util.StringUtils;
import 机油滤清器处理.BrandEnum;

import java.util.*;

/**
 *
 * 人工整理的商品-车型匹配关系数据处理
 * 跟力洋车型 或者 电商车型匹配
 * 生成商品编码-车型对应关系
 *
 * Created by huangzhangting on 16/10/1.
 */
public class GoodsCarExcelTest extends BaseTest {

    private static int BRAND_CODE = 0; //品牌编号


    //TODO 方便测试其他方法
    @Test
    public void justTest() throws Exception{

    }

    //TODO 商品-力洋车型关系处理
    @Test
    public void goodsLyCarTest() throws Exception{
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/未处理/国文/";

        String excel = path + "奥盛机滤没有匹配上的数据修改版-20170302.xlsx";

        BRAND_CODE = BrandEnum.AO_SHENG.getCode();

        int sheet = 1; //第几个sheet

        boolean exportUnMatchDataFlag = true; //导出没有匹配上的数据
        boolean modifyFlag = true; //需要过滤错误的数据

        CommReaderXLSX readerXLSX = new CommReaderXLSX(GoodsCarConfig.getGcExcelAttrMap());
        readerXLSX.processOneSheet(excel, sheet);

        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        if(modifyFlag){
            int size = dataList.size();
            for(int i=0; i<size; i++){
                Map<String, String> data = dataList.get(i);
                if(!"OK".equals(data.get("modifyStatus"))){
                    dataList.remove(i);
                    i--;
                    size--;
                }
            }
            Print.info("去掉了错误的数据，剩下数据：");
            Print.printList(dataList);
        }


        List<Map<String, Object>> carInfoList = commonMapper.selectListBySql(GoodsCarConfig.getLyCarInfoSql());

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
        if(exportUnMatchDataFlag) {
            ExcelExporter.exportUnMatchData(path, unMatchList, BRAND_CODE);
        }
    }

    private void init(){
        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = sqlPath+"insert_goods_lyid_rel_"+dateStr+"_"+ BRAND_CODE +".sql";
        writer = IoUtil.getWriter(sqlFile);
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
            Print.info("数据错误：" + data);
            unMatchList.add(data);
        }else{
            handleSql(data.get("goodsFormat"), lyIdSet, BRAND_CODE);

            //云修号处理
            String yunFormat = data.get("yunFormat");
            if(StringUtils.isEmpty(yunFormat) || yunFormat.contains("#N/A")){

            }else{
                Print.info("有云修号："+yunFormat);
                handleSql(yunFormat, lyIdSet, BrandEnum.YUN_XIU.getCode());
            }
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

            return Math.abs(mp1 - mp2) <= GoodsCarConfig.MAX_POWER_OFFSET;

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
        if(GoodsCarConfig.SP_CAR_MODELS.contains(attrs)){
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


    private void handleSql(String goodsFormat, Set<String> lyIdSet, int brandCode){
        Print.info(goodsFormat+" : "+lyIdSet.size());

        StringBuilder sb = new StringBuilder();
        for(String lyId : lyIdSet){
            sb.append(",");
            sb.append("('").append(goodsFormat);
            sb.append("', '").append(lyId).append("', ");
            sb.append(brandCode).append(")");
        }
        sb.deleteCharAt(0);

        writeSql(sb);
    }

    private void writeSql(StringBuilder sb){
        sb.insert(0, "insert ignore into temp_goods_lyid_rel(goods_format, ly_id, brand_code) values");
        sb.append(";\n");
        IoUtil.writeFile(writer, sb.toString());
    }

}
