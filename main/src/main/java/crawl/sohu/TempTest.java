package crawl.sohu;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/29.
 */
public class TempTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/数据抓取/搜狐/";
        String excel = path + "搜狐-淘汽车型匹配验证-20161128.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("tq_car", "tq_car");
        attrMap.put("sh_car", "sh_car");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        excel = path + "数据检查/人工匹配多余的数据.xls";
        attrMap = new HashMap<>();
        attrMap.put("c6.tq_car", "tq_car");
        attrMap.put("sc.sh_car", "sh_car");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> needDelList = readerXLS.getDataList();
        Print.printList(needDelList);

        //过滤需要删除的数据
        grepDataList(dataList, needDelList);

        Print.info("过滤后的数据："+dataList.size());

        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);
        writer = IoUtil.getWriter(sqlPath + "sohu_car_relation.sql");
        IoUtil.writeFile(writer, "truncate table sohu_car_relation;\n");

        int count = 1000;
        int size = dataList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            Map<String, String> data = dataList.get(i);
            sql.append("(").append(data.get("tq_car"));
            sql.append(",");
            sql.append(data.get("sh_car")).append(")");

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

    }
    public void grepDataList(List<Map<String, String>> dataList, List<Map<String, String>> needDelList){
        int size = dataList.size();
        for(int i=0; i<size; i++){
            Map<String, String> data = dataList.get(i);
            for(Map<String, String> del : needDelList){
                if(data.get("tq_car").equals(del.get("tq_car")) && data.get("sh_car").equals(del.get("sh_car"))){
                    dataList.remove(i);
                    i--;
                    size--;
                    break;
                }
            }
        }
    }
    public void writeSql(StringBuilder sql){
        sql.insert(0, "insert ignore into sohu_car_relation(tq_car_id,sh_car_id) values");
        sql.append(";\n");
        IoUtil.writeFile(writer, sql.toString());
    }


    /**
     * 对应多个保养方案的车型数据导出
     * @throws Exception
     */
    @Test
    public void multi_maintenance_test() throws Exception{
        path = "/Users/huangzhangting/Desktop/数据抓取/搜狐/";
        String excel = path + "待补充的车型保养方案-1129.xls";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t4.id", "id");
        attrMap.put("t5.搜狐id", "shId");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);
        Map<String, Set<String>> idMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String id = data.get("id");
            String shId = data.get("shId");
            Set<String> set = idMap.get(id);
            if(set==null){
                set = new HashSet<>();
                idMap.put(id, set);
            }
            set.add(shId);
        }

        Set<String> multiIdSet = new HashSet<>();
        List<Map<String, String>> list = new ArrayList<>();
        for(final Map.Entry<String, Set<String>> entry : idMap.entrySet()){
            if(entry.getValue().size()>1){
                if(multiIdSet.add(entry.getKey())) {
                    list.add(new HashMap<String, String>() {{
                        put("id", entry.getKey());
                        put("count", entry.getValue().size()+"");
                    }});
                }
            }
        }
        Print.info(multiIdSet.size());
        Print.info("存在多个方案的淘汽车款id："+multiIdSet);

        String[] heads = new String[]{"淘汽车款id", "方案数量"};
        String[] fields = new String[]{"id", "count"};
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("存在多个方案的淘汽车款id", path, heads, fields, list);
    }


    @Test
    public void test_maintenance() throws Exception{
        path = "/Users/huangzhangting/Desktop/数据抓取/搜狐/";
        String excel = path + "待补充的车型保养方案-1129.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t5.搜狐id", "id");
        attrMap.put("t3.里程数", "mileage");
        attrMap.put("机油", "机油");
        attrMap.put("火花塞", "火花塞");
        attrMap.put("机油滤清器", "机油滤清器");
        attrMap.put("空气滤清器", "空气滤清器");
        attrMap.put("空调滤清器", "空调滤清器");
        attrMap.put("汽油滤清器", "汽油滤清器");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        Map<String, List<Map<String, String>>> map = new HashMap<>();
        for(Map<String, String> data : dataList){
            String id = data.get("id");
            List<Map<String, String>> list = map.get(id);
            if(list==null){
                list = new ArrayList<>();
                map.put(id, list);
            }
            list.add(data);
        }

        List<Map<String, String>> idList = new ArrayList<>();
        for(final Map.Entry<String, List<Map<String, String>>> entry : map.entrySet()){
            if(is_empty_detail(entry.getValue())){
                idList.add(new HashMap<String, String>(){{
                    put("id", entry.getKey());
                }});
            }
        }
        Print.info("方案详情有为空的车型："+idList.size());

        String[] heads = new String[]{"搜狐车型id"};
        String[] fields = new String[]{"id"};
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("方案详情有为空的车型id", path, heads, fields, idList);

    }
    public boolean is_empty_detail(List<Map<String, String>> list){
        boolean 机油 = true;
        boolean 火花塞 = true;
        boolean 机油滤清器 = true;
        boolean 空气滤清器 = true;
        boolean 空调滤清器 = true;
        boolean 汽油滤清器 = true;
        for(Map<String, String> map : list){
            if(!StringUtils.isEmpty(map.get("机油"))){
                机油 = false;
            }
            if(!StringUtils.isEmpty(map.get("火花塞"))){
                火花塞 = false;
            }
            if(!StringUtils.isEmpty(map.get("机油滤清器"))){
                机油滤清器 = false;
            }
            if(!StringUtils.isEmpty(map.get("空气滤清器"))){
                空气滤清器 = false;
            }
            if(!StringUtils.isEmpty(map.get("空调滤清器"))){
                空调滤清器 = false;
            }
            if(!StringUtils.isEmpty(map.get("汽油滤清器"))){
                汽油滤清器 = false;
            }
        }

        return 机油 | 火花塞 | 机油滤清器 | 空气滤清器 | 空调滤清器 | 汽油滤清器;
    }

}
