package temporary.souche;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangzhangting on 16/4/12.
 */
public class Init {
    public static Map<String, String> initLyAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("力洋ID", "力洋ID");
        attrMap.put("品牌", "品牌");
        attrMap.put("厂家", "厂家");
        attrMap.put("车系", "车系");
        attrMap.put("车型", "车型");
        attrMap.put("销售名称", "销售名称");
        attrMap.put("年款", "年款");
        attrMap.put("排量(升)", "排量");
        attrMap.put("国产合资进口", "是否进口");
        attrMap.put("长度(mm)", "长");
        attrMap.put("宽度(mm)", "宽");
        attrMap.put("高度(mm)", "高");
        attrMap.put("整备质量(kg)", "重量");

        return attrMap;
    }

    public static Map<String, String> initAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("品牌", "品牌");
        attrMap.put("品牌编码", "品牌编码");
        attrMap.put("厂家", "厂家");
        attrMap.put("车系", "车系");
        attrMap.put("车系编码", "车系编码");
        attrMap.put("车型", "车型");
        attrMap.put("车型编码", "车型编码");
        attrMap.put("年款", "年款");
        attrMap.put("排量", "排量");
        attrMap.put("是否进口", "是否进口");
        attrMap.put("变速器", "变速器");
        attrMap.put("长", "长");
        attrMap.put("宽", "宽");
        attrMap.put("高", "高");
        attrMap.put("重量", "重量");

        return attrMap;
    }

    public static Map<String, String> initCarModelMap(){
        Map<String, String> carModelMap = new HashMap<>();
        carModelMap.put("第九代索纳塔", "索纳塔九");
        carModelMap.put("伊兰特悦动", "悦动");
        carModelMap.put("伊兰特-三厢", "伊兰特");
        carModelMap.put("伊兰特-两厢", "伊兰特");

        return carModelMap;
    }

    public static Map<String, String> initSpBrandMap() {
        Map<String, String> spBrandMap = new HashMap<>();
        spBrandMap.put("宝骏-乐驰", "雪佛兰");

        return spBrandMap;
    }

    public static Map<String, String> initBrandMap(){
        Map<String, String> brandMap = new HashMap<>();
        brandMap.put("北京汽车", "北京");
        brandMap.put("北汽新能源", "北京");
        brandMap.put("北汽幻速", "幻速");
        brandMap.put("北汽威旺", "威旺");
        brandMap.put("北汽制造", "北汽");
        brandMap.put("东风风神", "风神");
        brandMap.put("东风风行", "风行");
        brandMap.put("广汽传祺", "传祺");
        brandMap.put("五菱汽车", "五菱");
        brandMap.put("猎豹汽车", "猎豹");
        brandMap.put("广汽吉奥", "吉奥");
        brandMap.put("福汽启腾", "启腾");
        brandMap.put("莲花汽车", "莲花");
        brandMap.put("上汽大通", "大通");
        brandMap.put("中欧汽车", "中欧");

        return brandMap;
    }

    public static Set<String> initSpCompanySet() {
        Set<String> set = new HashSet<>();
        set.add("吉利汽车");

        return set;
    }
}
