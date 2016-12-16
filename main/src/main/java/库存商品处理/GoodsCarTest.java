package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * Created by huangzhangting on 16/11/10.
 */
public class GoodsCarTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        String excelPath = path + "关系数据/";
        File directory = new File(excelPath);

        String[] fileNames = directory.list();
        Print.info(fileNames.length);
        Map<String, Map<String, String>> dataMap = new HashMap<>();

        GoodsCarCommon common = new GoodsCarCommon(commonMapper, path);

        for(String name : fileNames){
//            Print.info(excelPath + name);
            List<Map<String, String>> dataList = common.getGoodsCarList(excelPath + name);
            common.handleDataList(dataMap, dataList);
        }

        /**/
        String excel = path + "2级别商品与电商车型20161104.xlsx";
        for(int j=1; j<7; j++){
            List<Map<String, String>> dataList = common.getGoodsCarList(excel, j);
            common.handleDataList(dataMap, dataList);
        }

        common.handleCarIdGoodsSnList(dataMap.values());
    }

    // 2016-11-15
    @Test
    public void test_1115() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        Map<String, Map<String, String>> dataMap = new HashMap<>();
        GoodsCarCommon common = new GoodsCarCommon(commonMapper, path);

        String excel = path + "剩余商品数据与电商车型关系表20161114.xlsx";
        for(int j=1; j<4; j++){
            List<Map<String, String>> dataList = common.getGoodsCarList(excel, j);
            common.handleDataList(dataMap, dataList);
        }

        common.handleCarIdGoodsSnList(dataMap.values());
    }


    // 2016-11-16
    @Test
    public void test_1116() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/1116/";

        String excel = path + "剩余库存商品整理完成情况表2(1)";
        List<Map<String, String>> scPianList = Common.getOKGoodsList(excel, 1); //刹车片
        List<Map<String, String>> scPanList = Common.getOKGoodsList(excel, 2); //刹车盘
        List<Map<String, String>> scTiList = Common.getOKGoodsList(excel, 3); //刹车蹄
        List<Map<String, String>> jiLvList = Common.getOKGoodsList(excel, 4); //机滤

        excel = path + "剩余商品表2与力洋ID关系表(1)";
        List<Map<String, String>> lyIdScPianList = Common.getLyIdGoodsList(excel, 1);
        List<Map<String, String>> lyIdScPanList = Common.getLyIdGoodsList(excel, 2);
        List<Map<String, String>> lyIdScTiList = Common.getLyIdGoodsList(excel, 3);
        List<Map<String, String>> lyIdJiLvList = Common.getLyIdGoodsList(excel, 4);

        Common common = new Common(commonMapper);
        String filePath = path + "处理后的/";

        common.handleGoodsCar(scPianList, lyIdScPianList, "刹车片", filePath);
        common.handleGoodsCar(scPanList, lyIdScPanList, "刹车盘", filePath);
        common.handleGoodsCar(scTiList, lyIdScTiList, "刹车蹄", filePath);
        common.handleGoodsCar(jiLvList, lyIdJiLvList, "机滤", filePath);

    }


    // 2016-11-21
    @Test
    public void test_1121() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        Map<String, Map<String, String>> dataMap = new HashMap<>();
        GoodsCarCommon common = new GoodsCarCommon(commonMapper, path);

        String excel = path + "关系数据/1116/剩余商品2与电商车型关系表.xlsx";
        int size = 6;
        for(int j=1; j<size; j++){
            List<Map<String, String>> dataList = common.getGoodsCarList(excel, j);
            common.handleDataList(dataMap, dataList);
        }

        common.handleCarIdGoodsSnList(dataMap.values());
    }


    @Test
    public void test_1216_数据补充() throws Exception{
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/";

        String excel = path + "云修机滤补充车型20131213.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("规格型号2", "goodsFormat");
        attrMap.put("力洋ID", "lyId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        Set<String> lyIdSet = new HashSet<>();
        for(Map<String, String> data : dataList){
            lyIdSet.add(data.get("lyId"));
        }
        Print.info(lyIdSet.size());


        writer = IoUtil.getWriter(path + "ly_id_goods.sql");
        IoUtil.writeFile(writer, "truncate table ly_id_goods;\n");

        StringBuilder sql = new StringBuilder();
        for(String lyId : lyIdSet){
            sql.setLength(0);
            sql.append("insert into ly_id_goods(goods_id, ly_id) value(393195, '");
            sql.append(lyId).append("');\n");

            IoUtil.writeFile(writer, sql.toString());
        }

    }


}
