package temporary.obd;

import dp.common.util.Constant;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/6/20.
 */
public class ObdTest {
    private String path;


    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/";
        String excel = path + "obd数据.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.obd", "obd");
        attrMap.put("t2.attr_name", "attr_name");
        attrMap.put("t3.attr_value", "attr_value");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());

        Set<String> attrSet = new HashSet<>();
        for(Map<String, String> data : dataList){
            attrSet.add(data.get("attr_name"));
        }
        Print.info(attrSet);
        List<String> attrList = new ArrayList<>(attrSet);
        Print.info(attrList);

        Map<String, Map<String, String>> dataMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String obd = data.get("obd").toUpperCase();
            Map<String, String> map = dataMap.get(obd);
            if(map==null){
                map = handleMap(obd, attrList);
                dataMap.put(obd, map);
            }
            map.put(data.get("attr_name"), data.get("attr_value"));
        }
        Print.info(dataMap.size());


        attrList.add(0, "故障码");
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsWithMap("obd数据", path, attrList, dataMap.values());
    }

    public Map<String, String> handleMap(String obd, List<String> attrList){
        Map<String, String> map = new HashMap<>();
        map.put("故障码", obd);
        for(String attr : attrList){
            map.put(attr, "");
        }

        return map;
    }

}
