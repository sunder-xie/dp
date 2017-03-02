package 机油滤清器处理.商品车型数据处理;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class GoodsCarConfig {
    //功率差值
    public static final double MAX_POWER_OFFSET = 3;

    //特殊的车型
    public static final List<String> SP_CAR_MODELS = new ArrayList<String>(){{
        add("Escape/Kuga [翼虎]");
    }};

    //力洋车型数据
    public static String getLyCarInfoSql(){
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

    //excel字段
    public static Map<String, String> getGcExcelAttrMap(){
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

        attrMap.put("云修号", "yunFormat");
        attrMap.put("修改标识", "modifyStatus");

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

}
