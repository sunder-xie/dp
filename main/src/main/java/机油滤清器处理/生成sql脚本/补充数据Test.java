package 机油滤清器处理.生成sql脚本;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/9/29.
 */
public class 补充数据Test extends BaseTest {

    //TODO 奥盛数据处理
    @Test
    public void test_ao_sheng() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/奥盛机滤/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path+"奥盛可以补充的型号.xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));


        String excel = "/Users/huangzhangting/Desktop/机滤数据处理/云修机滤奥盛号与云修号对应关系.xlsx";
        attrMap = new HashMap<>();
        attrMap.put("厂家编码", "format");
        attrMap.put("云修号", "goodsFormat");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> relationList = readerXLSX.getDataList();
        Print.info(relationList.size());
        Print.info(relationList.get(0));

        Map<String, String> relMap = new HashMap<>();
        for(Map<String, String> r : relationList){
            String format = r.get("format");
            String goodsFormat = r.get("goodsFormat");
            String str = relMap.get(format);
            if(str==null){
                relMap.put(format, goodsFormat);
            }else{
                Print.info("有疑问的型号："+format+"  "+str+"  "+goodsFormat);
            }
        }
        Print.info(relMap.size());

        //TODO 读取云修商品信息
        List<Map<String, String>> goodsList = new ArrayList<>();

        for(Map<String, String> data : oToDataList){
            String goodsFormat = data.get("goodsFormat");
            String str = relMap.get(goodsFormat);
            if(str!=null){
                Print.info("存在对应的云修号："+str+"  奥盛号："+goodsFormat);
            }
        }

    }


    //TODO 处理箭冠补充数据
    @Test
    public void test_jian_guan() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/箭冠补充数据/最终数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path+"箭冠可以补充的型号(修订后)-20161009.xlsx", 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        //TODO 读取箭冠商品
        List<Map<String, String>> goodsList = new ArrayList<>();

        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);

        for(Map<String, String> data : dataList){
            String carId = data.get("carId");
            sqlGen.handleGoodsCar(goodsList, carId, data.get("goodsFormat"));
        }

        sqlGen.handleSql("add_car_oil_filter_jg");

    }

}
