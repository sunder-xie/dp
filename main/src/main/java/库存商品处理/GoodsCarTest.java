package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import 机油滤清器处理.生成sql脚本.GoodsCarSqlGen;

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





    //读取商品数据
    private List<Map<String, String>> getDsGoodsList(String excelName) throws Exception{
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goods_id");
        attrMap.put("goods_name", "goods_name");
        attrMap.put("goods_format", "goods_format");
        attrMap.put("brand_partcode", "brand_code");

        String excel = "/Users/huangzhangting/Desktop/商品车型关系数据补充/电商商品/"+excelName;

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);
        return dataList;
    }
    private String getGoodsId(List<Map<String, String>> goodsList, String goodsFormat){
        for(Map<String, String> goods : goodsList){
            if(goodsFormat.equals(goods.get("goods_format"))){
                return goods.get("goods_id");
            }
        }
        return null;
    }
    private String getGoodsId2(List<Map<String, String>> goodsList, String brandCode){
        for(Map<String, String> goods : goodsList){
            if(brandCode.equals(goods.get("brand_code"))){
                return goods.get("goods_id");
            }
        }
        return null;
    }


    @Test
    public void test_0302() throws Exception{
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/未处理/国文/";

        List<Map<String, String>> goodsList = getDsGoodsList("奥胜机滤.xls");
        goodsList.addAll(getDsGoodsList("云修机滤.xls"));

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("gc.goods_format", "goods_format");
        attrMap.put("c.car_id", "car_id");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path + "商品型号-车款id.xls", attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        String table = "db_goods_car_mini";
        writer = IoUtil.getWriter(path + table + ".sql");
        IoUtil.writeFile(writer, "truncate table "+table+";\n");

        for(Map<String, String> data : dataList){
            String goodsId = getGoodsId(goodsList, data.get("goods_format"));
            if(goodsId==null){
                Print.info("不存在的商品编码："+data);
            }else{
                //data.put("goods_id", goodsId);
                StringBuilder sql = new StringBuilder();
                sql.append("insert ignore into ").append(table).append("(goods_id, car_id) value (");
                sql.append(goodsId).append(", ").append(data.get("car_id"));
                sql.append(");\n");
                IoUtil.writeFile(writer, sql.toString());
            }
        }

        IoUtil.closeWriter(writer);
    }



    @Test
    public void test_0307() throws Exception{
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/未处理/美龄/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品型号", "goodsFormat");
        attrMap.put("力洋ID", "lyId");

        String excel = path + "博世电瓶与车型关系汇总.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        List<Map<String, String>> goodsList = getDsGoodsList("博世电瓶.xls");



    }

}
