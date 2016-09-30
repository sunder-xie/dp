package temporary.epc;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/6/15.
 */
public class PicTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/";
        String excel = path + "图片有问题的oe码.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("oe码", "oe");
        attrMap.put("配件", "part");
        attrMap.put("图片", "pic_url");
        attrMap.put("图编号", "pic_no");
        attrMap.put("位置", "pic_idx");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());

        List<Map<String, String>> list = readerXLS.getDataList();
        int size = list.size();
        for(int i=0; i<size; i++){
            list.get(i).put("id", (i+1)+"");
        }

        Print.info(JsonUtil.objectToStr(list.get(0)));

        writer = IoUtil.getWriter(path + "json测试/data.json");
        IoUtil.writeFile(writer, JsonUtil.objectToStr(list));
        IoUtil.closeWriter(writer);
    }


    @Test
    public void handlePic() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/epc/";
        String excel = path + "配件图片0704.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.epc_pic", "pic");
        attrMap.put("t2.pic_id", "id");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Map<String, String> picMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            picMap.put(data.get("id"), data.get("pic"));
        }
        Print.info(picMap.size());

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "update_pic_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        for(Map.Entry<String, String> entry : picMap.entrySet()){
            String sql = "update center_goods_car_picture set gmt_modified=@nowTime, epc_pic='"+entry.getValue()
                    +"' where id="+entry.getKey()+";\n";

            IoUtil.writeFile(writer, sql);
        }

        IoUtil.closeWriter(writer);
    }

}
