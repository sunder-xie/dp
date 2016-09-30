package crawl.jd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangzhangting on 16/4/13.
 */
public class Init {

    public static Map<String, Set<String>> intBrandKwMap(){
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> set = new HashSet<>();

//        set.add("力霸");
//        set.add("力霸9000");
        set.add("速霸1000");
        set.add("速霸2000");
        set.add("美孚1号");
        map.put("美孚", set);

        set = new HashSet<>();
        set.add("9000");
        set.add("7000");
        set.add("5000");
        set.add("4000");
        set.add("3000");
        map.put("道达尔", set);

        set = new HashSet<>();
        set.add("极护");
        set.add("磁护");
        set.add("嘉护");
        set.add("嘉力");
        map.put("嘉实多", set);

        set = new HashSet<>();
        set.add("灰");
        set.add("黄");
        set.add("蓝");
        set.add("红");
        set.add("超凡");
        map.put("壳牌", set);

        return map;
    }


    public static Map<String, String> initDsAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("g.goods_id", "ds_id");
        attrMap.put("b.品牌", "电商品牌");
        attrMap.put("g.名称", "电商商品名称");
        attrMap.put("g.规格型号", "规格型号");

        return attrMap;
    }

    public static Map<String, String> initJdAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "jd_id");
        attrMap.put("商品名称", "京东商品名称");
        attrMap.put("短名称", "短名称");
        attrMap.put("品牌", "京东品牌");
        attrMap.put("匹配参数", "匹配参数");

        return attrMap;
    }

}
