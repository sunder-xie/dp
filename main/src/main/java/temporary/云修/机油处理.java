package temporary.云修;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/9/20.
 */
public class 机油处理 extends BaseTest {

    //TODO 机油适配车型规则（汽油车）

    //特殊的高端车品牌（这些品牌部分车型价格会低于25W）
    private Set<String> getSpCarBrands(){
        Set<String> set = new HashSet<>();
        set.add("奔驰");
        set.add("宝马");
        set.add("奥迪");
        set.add("英菲尼迪");
        set.add("雷克萨斯");
        set.add("讴歌");
        set.add("捷豹");
        set.add("路虎");
        set.add("沃尔沃");
        set.add("MINI");
        set.add("林肯");
        set.add("凯迪拉克");

        return set;
    }

    @Test
    public void test() throws Exception{
        Set<String> spCarBrands = getSpCarBrands();

        String createYear = "2013"; //生产年份分界点

        path = "/Users/huangzhangting/Desktop/安心保险数据对接/云修汽机油数据处理/";
        writer = IoUtil.getWriter(path + "insert_temp_goods_lyid_rel_new.sql");

        String sql = "select leyel_id,car_brand,intake_style,guide_price " +
                "from db_car_info_all " +
                "where create_year!='' and create_year<='"+createYear+"' " +
                "and guide_price!=''";

        List<Map<String, Object>> dataList = commonMapper.selectListBySql(sql);
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        for(Map<String, Object> data : dataList){
            String lid = data.get("leyel_id").toString();
            if(containT(data)){
                if(isHighGrade(data, spCarBrands)){
                    // 0W-40
                    writeInsertSql("0W-40", lid);
                }else{
                    // 5W-40
                    writeInsertSql("5W-40", lid);
                }
            }else{
                if(isHighGrade(data, spCarBrands)){
                    // 5W-40
                    writeInsertSql("5W-40", lid);
                }else{
                    // 10W-40
                    writeInsertSql("10W-40", lid);
                }
            }
        }


        sql = "select leyel_id,car_brand,intake_style,guide_price " +
            "from db_car_info_all " +
            "where create_year!='' and create_year>'"+createYear+"' " +
            "and guide_price!=''";

        dataList = commonMapper.selectListBySql(sql);
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        for(Map<String, Object> data : dataList){
            String lid = data.get("leyel_id").toString();
            if(containT(data)){
                // 5W-30
                writeInsertSql("5W-30", lid);
            }else{
                if(isHighGrade(data, spCarBrands)){
                    // 5W-30
                    writeInsertSql("5W-30", lid);
                }else{
                    // 10W-40
                    writeInsertSql("10W-40", lid);
                }
            }
        }

        IoUtil.closeWriter(writer);
    }

    private void writeInsertSql(String goodsFormat, String lid){
        StringBuilder sql = new StringBuilder();
        sql.append("insert ignore into temp_goods_lyid_rel_new(goods_format, ly_id) value ('");
        sql.append(goodsFormat);
        sql.append("','");
        sql.append(lid);
        sql.append("');\n");

        IoUtil.writeFile(writer, sql.toString());
    }

    private boolean containT(Map<String, Object> data){
        Object obj = data.get("intake_style");
        if(obj==null){
            return false;
        }
        String str = obj.toString().trim();
        if("".equals(str) || "自然吸气".equals(str)){
            return false;
        }

        return true;
    }

    //价格高于25W，或者是特殊品牌，都归并为高端车
    private boolean isHighGrade(Map<String, Object> data, Set<String> spCarBrands){
        double price = Double.parseDouble(data.get("guide_price").toString());
        if(price>25){
            return true;
        }
        return spCarBrands.contains(data.get("car_brand").toString());
    }

    /*

    select t1.car_models_id, t2.goods_format
from db_car_all t1, temp_goods_lyid_rel_new t2
where t1.new_l_id=t2.ly_id
group by t1.car_models_id, t2.goods_format

    * */


    // 查询只有适配一个型号的，车型id-规格型号数据
    private String getOneSql(){
        String sql = "select c.id as carId, t3.goods_format as goodsFormat " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel_new t1, db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id " +
                "group by t2.car_models_id,t1.goods_format) t3, db_car_category c " +
                "where t3.car_models_id=c.id and c.is_del=0 " +
                "and c.`name` not like '%柴油%' and c.power != '电动' " +
                "group by c.id having count(1)=1";

        return sql;
    }

    // 查询适配多个型号的，车型id-规格型号数据
    private String getSomeSql(){
        String sql = "select t5.car_models_id as carId,t5.goods_format as goodsFormat " +
                "from " +
                "(select t3.car_models_id " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel_new t1, db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id " +
                "group by t2.car_models_id,t1.goods_format) t3 " +
                "group by t3.car_models_id  " +
                "having count(1)>1) t4,  " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel_new t1, db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id " +
                "group by t2.car_models_id,t1.goods_format " +
                ") t5, db_car_category c " +
                "where t4.car_models_id=t5.car_models_id and t5.car_models_id=c.id " +
                "and c.is_del=0 and c.`name` not like '%柴油%' and c.power != '电动' " +
                "order by c.brand,c.company,c.series,c.model,c.power,c.`year`,c.`name`";

        return sql;
    }

    /*
因为电商车型结构只取了年款，没有体现生产年份，所以可能出现，一款车对应多个规格机油
出现多个时，推荐顺序如下：优先推荐低级的（便宜的）
10W-40（最低级别）
5W-40
5W-30
0W-40（最高级别）
     */
    private Map<String, Integer> getGoodsFormatMap(){
        Map<String, Integer> map = new HashMap<>();
        map.put("10W-40", 1);
        map.put("5W-40", 2);
        map.put("5W-30", 3);
        map.put("0W-40", 4);

        return map;
    }


    //TODO 云修机油处理
    @Test
    public void testYunXiuOil() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/云修汽机油数据处理/";
        String excel = path + "云修机油数据.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));
        if(goodsList.isEmpty()){
            Print.info("没有商品数据");
            return;
        }

        // 生成 db_goods_car_mini 表数据
        List<Map<String, String>> goodsCarList = new ArrayList<>();

        //就一条对应关系的数据处理
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(getOneSql());
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        for(Map<String, Object> data : dataList){
            handleGoodsCarData(goodsCarList, goodsList, data);
        }

        //多条对应关系的数据处理
        dataList = commonMapper.selectListBySql(getSomeSql());
        Print.info(dataList.size());
        Print.info(dataList.get(0));
        Print.info(dataList.get(1));

        Map<String, List<String>> carIdMap = new HashMap<>();
        for(Map<String, Object> data : dataList){
            String carId = data.get("carId").toString();
            List<String> goodsFormats = carIdMap.get(carId);
            if(goodsFormats==null){
                goodsFormats = new ArrayList<>();
                carIdMap.put(carId, goodsFormats);
            }
            goodsFormats.add(data.get("goodsFormat").toString());
        }
        Print.info(carIdMap.size());

        Map<String, Integer> goodsFormatMap = getGoodsFormatMap();
        Print.info(goodsFormatMap);

        for(Map.Entry<String, List<String>> entry : carIdMap.entrySet()){
            Integer i = null;
            for(String gf : entry.getValue()){
                if(i==null){
                    i = goodsFormatMap.get(gf);
                }else{
                    int n = goodsFormatMap.get(gf);
                    if(n<i){
                        i = n;
                    }
                }
            }

            //Print.info(i);
            for(Map.Entry<String, Integer> gfEntry : goodsFormatMap.entrySet()){
                if(gfEntry.getValue().equals(i)){
                    //Print.info(entry.getKey()+"  "+gfEntry.getKey());
                    Map<String, Object> data = new HashMap<>();
                    data.put("carId", entry.getKey());
                    data.put("goodsFormat", gfEntry.getKey());

                    handleGoodsCarData(goodsCarList, goodsList, data);
                    break;
                }
            }
        }

        //处理车型-商品数据
        handleGoodsCarList(goodsCarList);
    }

    private void handleGoodsCarData(List<Map<String, String>> goodsCarList, List<Map<String, String>> goodsList,
                                    Map<String, Object> data){
        //Print.info(data);
        for(Map<String, String> goods : goodsList){
            String goodsFormat = goods.get("goodsFormat");
            String[] gfs = goodsFormat.split(" ");
            if(gfs[1].equals(data.get("goodsFormat"))){
                Map<String, String> goodsCar = new HashMap<>();
                goodsCar.put("goodsId", goods.get("goodsId"));
                goodsCar.put("carId", data.get("carId").toString());
                //Print.info(goodsCar);
                goodsCarList.add(goodsCar);
            }
        }
    }

    private void handleGoodsCarList(List<Map<String, String>> goodsCarList){
        if(goodsCarList.isEmpty()){
            Print.info("没有车型商品关系数据");
            return;
        }

        writer = IoUtil.getWriter(path + "insert_goods_car_mini.sql");

        int count = 1000;
        int size = goodsCarList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            appendVal(sql, goodsCarList.get(i));
            if((i+1)%count==0){
                writeSql(sql);
                sql.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeSql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    private void appendVal(StringBuilder sql, Map<String, String> data){
        sql.append("(").append(data.get("goodsId"));
        sql.append(", ").append(data.get("carId"));
        sql.append(")");
    }

    private void writeSql(StringBuilder sql){
        sql.insert(0, "insert ignore into db_goods_car_mini(goods_id, car_id) values ");
        sql.append(";\n");

        IoUtil.writeFile(writer, sql.toString());
    }



    @Test
    public void testGoodsCar() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/云修汽机油数据处理/sql/";

        //需要删除的车款机油关系数据
        String sql = "select goods_id from db_goods where brand_id=849 and cat_id=5025";

        List<String> goodsIdList = commonMapper.selectOneFieldBySql(sql);
        Print.info(goodsIdList.size());
        Print.info(goodsIdList);

        handleDeleteGoodsCar(goodsIdList);


        //需要添加的车款机油关系数据
        sql = "select goods_id, car_id from db_goods_car_mini";
        List<Map<String, Object>> goodsList = commonMapper.selectListBySql(sql);
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        handleAddGoodsCar(goodsList);
    }

    private void handleDeleteGoodsCar(List<String> dataList){
        if(dataList.isEmpty()){
            Print.info("没有需要删除的数据");
            return;
        }
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "delete_car_oil_"+dateStr+".sql");
        StringBuilder sql = new StringBuilder();
        for(String gid : dataList){
            sql.append(", ").append(gid);
        }
        sql.deleteCharAt(0);
        sql.insert(0, "delete from db_goods_car where goods_id in(");
        sql.append(" );\n");
        IoUtil.writeFile(writer, sql.toString());
        IoUtil.closeWriter(writer);
    }

    private List<Map<String, Object>> getCarInfoList(){
        String sql = "select brand,brand_id,series,series_id,model,model_id," +
                "power,power_id,year,year_id,car_models as car_name,car_models_id as car_id " +
                "from db_car_all group by car_models_id";

        return commonMapper.selectListBySql(sql);
    }

    private Map<String, Object> getCarInfo(String carId, List<Map<String, Object>> carInfoList){
        for(Map<String, Object> car : carInfoList){
            if(carId.equals(car.get("car_id").toString())){
                return ObjectUtil.copyMap(car);
            }
        }

        return null;
    }

    private void handleAddGoodsCar(List<Map<String, Object>> dataList){
        if(dataList.isEmpty()){
            Print.info("没有需要新增的数据");
            return;
        }
        //全部的车款信息
        List<Map<String, Object>> carInfoList = getCarInfoList();
        Print.info(carInfoList.size());
        Print.info(carInfoList.get(0));

        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        for(Map<String, Object> data : dataList){
            Map<String, Object> gc = getCarInfo(data.get("car_id").toString(), carInfoList);
            if(gc!=null){
                //Print.info(data.get("goods_id"));
                gc.put("goods_id", data.get("goods_id"));
                goodsCarList.add(gc);
            }
        }

        handleSql(goodsCarList);
    }

    public void handleSql(List<Map<String, Object>> goodsCarList){
        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "add_car_oil_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        int count = 500;
        int size = goodsCarList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addGcVal(sb, goodsCarList.get(i));
            if((i+1)%count==0){
                writeGcSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIdx==i){
                writeGcSql(sb);
                break;
            }
            sb.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    public void addGcVal(StringBuilder sb, Map<String, Object> data){
        //Print.info(data.get("goods_id")+"  "+data.get("car_id"));

        sb.append("(");
        sb.append(data.get("goods_id")).append(",");
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
}
