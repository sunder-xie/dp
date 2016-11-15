package 库存商品处理;

import base.BaseTest;
import dp.common.util.Print;
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

        GoodsCarCommon common = new GoodsCarCommon();

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
        GoodsCarCommon common = new GoodsCarCommon();

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

}
