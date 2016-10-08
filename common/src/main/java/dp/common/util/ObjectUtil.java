package dp.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/7/29.
 */
public class ObjectUtil {

    public static Map<String, Object> copyMap(Map<String, Object> map){
        if(map==null){
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        for(Map.Entry<String, Object> entry : map.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static List<Map<String, String>> objToStrMapList(List<Map<String, Object>> dataList){
        if(dataList.isEmpty()){
            return new ArrayList<>();
        }
        List<Map<String, String>> list = new ArrayList<>();
        for(Map<String, Object> map : dataList){
            Map<String, String> m = new HashMap<>();
            for(Map.Entry<String, Object> entry : map.entrySet()){
                m.put(entry.getKey(), entry.getValue()==null?"":entry.getValue().toString());
            }
            list.add(m);
        }

        return list;
    }

    public static List<Map<String, Object>> strToObjMapList(List<Map<String, String>> dataList){
        if(dataList.isEmpty()){
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Map<String, String> map : dataList){
            Map<String, Object> m = new HashMap<>();
            for(Map.Entry<String, String> entry : map.entrySet()){
                m.put(entry.getKey(), entry.getValue()==null?"":entry.getValue());
            }
            list.add(m);
        }

        return list;
    }

    //保留两位小数，四舍五入
    public static String dbToStrHalfUp(double db){
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        return df.format(db);
    }

}
