package crawl.qpbkw;

import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import temporary.epc.PicUploader;

import java.io.File;
import java.io.Writer;
import java.util.*;

/**
 * 比较淘汽-汽配百科配件数据
 * Created by huangzhangting on 16/7/6.
 */
public class ComparePart {
    String path;
    Writer writer;
    String carModelStr;
    boolean uploadFlag;

    @Test
    public void test() throws Exception{
        uploadFlag = false;
        carModelStr = "奥迪-A8";

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/汽配百科网/Excel文件/"+carModelStr+"/";

        String excel = path + carModelStr + "-处理后-20160721.xlsx";
        String excel2 = path + "tq-A8配件数据.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("oe码", "oe");
        attrMap.put("图编号", "pic_num");
        attrMap.put("配件位置", "epc_index");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        if(dataList.isEmpty()){
            Print.info("没有数据, excel:"+excel);
            return;
        }
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Set<String> oeSet = new HashSet<>();
        List<Map<String, String>> list = new ArrayList<>();
        for(Map<String, String> data : dataList){
            if(oeSet.add(data.get("oe"))){
                list.add(data);
            }
        }
        Print.info(list.size());

        Map<String, String> attrMap2 = new HashMap<>();
        attrMap2.put("gc.pic_id", "pic_id");
        attrMap2.put("g.oe_number", "oe");
        attrMap2.put("gcp.epc_pic_num", "pic_num");
        attrMap2.put("gcp.epc_index", "epc_index");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap2, Constant.TYPE_LIST, 0);
        readerXLS.process(excel2, attrMap2.size());
        List<Map<String, String>> tqDataList = readerXLS.getDataList();
        if(tqDataList.isEmpty()){
            Print.info("没有淘汽配件数据, excel:"+excel2);
            return;
        }
        Print.info(tqDataList.size());

        Map<String, List<Map<String, String>>> picMap = new HashMap<>();
        for(Map<String, String> tqData : tqDataList){
            List<Map<String, String>> oeList = picMap.get(tqData.get("pic_id"));
            if(oeList==null){
                oeList = new ArrayList<>();
                picMap.put(tqData.get("pic_id"), oeList);
            }
            oeList.add(tqData);
        }
        Print.info(picMap.size());

        Print.info("开始处理图片");
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "update_pic_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        for(Map.Entry<String, List<Map<String, String>>> entry : picMap.entrySet()){
            handlePicture(entry.getKey(), getPicName(entry.getValue(), list));
        }

        IoUtil.closeWriter(writer);
    }

    public String getPicName(List<Map<String, String>> tqOeList, List<Map<String, String>> dataList){
        for(Map<String, String> tqOe : tqOeList){
            for(Map<String, String> data : dataList){
                if( tqOe.get("oe").equals(data.get("oe"))
                        && tqOe.get("pic_num").equals(data.get("pic_num"))
                        && tqOe.get("epc_index").equals(data.get("epc_index")) ){

                    return data.get("pic_num");
                }
            }
        }

        return null;
    }

    public void handlePicture(String picId, String fileName){
        if(fileName==null){
            return;
        }
        String imgPath = path + "img/";
        File pic = new File(imgPath + fileName + ".png");
        //Print.info(picId+"  "+pic.getPath());

        String newFileName = PicUploader.newFileName(".png");
        Print.info(newFileName);
        //TODO 上传图片
        if(uploadFlag) {
            if(PicUploader.uploadImg(newFileName, pic)){
                writeSql(picId, newFileName);
            }
        }else{
            writeSql(picId, newFileName);
        }

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void writeSql(String picId, String picUrl){
        String sql = "update center_goods_car_picture set gmt_modified=@nowTime, epc_pic='" + picUrl
                + "' where id="+picId;

        IoUtil.writeFile(writer, sql+";\n");
    }

}
