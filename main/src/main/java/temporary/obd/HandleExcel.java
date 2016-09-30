package temporary.obd;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/7/25.
 */
public class HandleExcel extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/obd车型数据/obd车型匹配数据验证/";
        String excel = path + "淘汽车型-obd车型-订正版.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.vid", "vid");
        attrMap.put("老的ID", "oid");
        attrMap.put("t1.obd车系ID", "nid");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> mdList = readerXLS.getDataList();
        Print.info(mdList.size());
        Print.info(mdList.get(0));


        excel = path + "淘汽车型-obd车系-定正版.xls";
        attrMap = new HashMap<>();
        attrMap.put("t1.vid", "vid");
        attrMap.put("new_id", "nid");

        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> sdList = readerXLS.getDataList();
        Print.info(sdList.size());
        Print.info(sdList.get(0));


        List<Map<String, String>> dataList = new ArrayList<>();
        for(Map<String, String> md : mdList){
            if (StringUtils.isEmpty(md.get("oid"))){
                dataList.add(md);
            }
        }
        for(Map<String, String> sd : sdList){
            if(!StringUtils.isEmpty(sd.get("nid"))){
                dataList.add(sd);
            }
        }

        writer = IoUtil.getWriter(path + "update_obd_rel.sql");

        for(Map<String, String> data : dataList){
            writeSql(data);
        }

        IoUtil.closeWriter(writer);
    }

    public void writeSql(Map<String, String> data){
        String vid = data.get("vid");
        String nid = data.get("nid");
        if(StringUtils.isEmpty(vid) || StringUtils.isEmpty(nid)){
            return;
        }

        String sql = "update db_vehicle_obd_vehicle_rel set obd_vehicle_id="+nid
                +" where vehicle_id="+vid+";\n";

        IoUtil.writeFile(writer, sql);
    }


    @Test
    public void test2() throws Exception{
        path = "/Users/huangzhangting/Desktop/obd车型数据/obd车型匹配数据验证/";
        String excel = path + "淘汽车系-obd车型-匹配20160725-订正版.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t2.id", "vid");
        attrMap.put("ov.obd车型id", "oid");
        attrMap.put("修改ID", "nid");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Map<String, String> idMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String nid = data.get("nid");
            String oid = data.get("oid");
            if(!StringUtils.isEmpty(nid)){
                oid = nid;
            }
            String vid = data.get("vid");
            String str = idMap.get(vid);
            if(str!=null && !str.equals(oid)){
                Print.info(vid+" : "+str+"    "+oid);
            }
            idMap.put(vid, oid);
        }

        Print.info(idMap.size());

        writer = IoUtil.getWriter(path+"add_series_rel.sql");

        for(Map.Entry<String, String> entry : idMap.entrySet()){
            String sql = "insert into db_vehicle_obd_vehicle_rel(vehicle_id,obd_vehicle_id) value("
                    +entry.getKey()+","+entry.getValue()+");\n";

            IoUtil.writeFile(writer, sql);
        }

        IoUtil.closeWriter(writer);
    }

}
