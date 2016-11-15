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

}
