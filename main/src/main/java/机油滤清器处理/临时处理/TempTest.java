package 机油滤清器处理.临时处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import 机油滤清器处理.商品车型数据处理.ExcelExporter;
import 机油滤清器处理.生成sql脚本.GoodsCarSqlGen;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/27.
 */
public class TempTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/安心礼包机滤商品信息/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品sn", "goodsSn");

        String excel = path + "保险礼包机滤型号信息-20161026.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> newDataList = readerXLSX.getDataList();
        Print.printList(newDataList);
        Set<String> newSnSet = new HashSet<>();
        for(Map<String, String> data : newDataList){
            String goodsSn = data.get("goodsSn");
            if(!"".equals(goodsSn)){
                newSnSet.add(goodsSn);
            }
        }
        Print.info("新的商品sn："+newSnSet.size());

        excel = path + "机油滤清器商品信息-20160929-old.xlsx";
        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> oldDataList = readerXLSX.getDataList();
        Print.printList(oldDataList);
        Set<String> oldSnSet = new HashSet<>();
        for(Map<String, String> data : oldDataList){
            String goodsSn = data.get("goodsSn");
            if(!"".equals(goodsSn)){
                oldSnSet.add(goodsSn);
            }
        }
        Print.info("老的商品sn："+oldSnSet.size());

        Set<String> newSet = new HashSet<>(newSnSet);
        newSet.removeAll(oldSnSet);
        Print.info("需要添加的sn："+newSet);

        Set<String> oldSet = new HashSet<>(oldSnSet);
        oldSet.removeAll(newSnSet);
        Print.info("需要去掉的sn："+oldSet);

    }



    @Test
    public void test_1128() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/";

        String excel = path + "待处理的数据/箭冠补充数据/最终数据/箭冠可以补充的型号(修订后)-20161009.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");
        attrMap.put("排量", "power");
        attrMap.put("年款", "year");
        attrMap.put("车款", "carName");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> goodsCarList = readerXLSX.getDataList();
        Print.printList(goodsCarList);

        excel = path + "商品数据/箭冠机滤-1128.xls";
        attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("goods_format", "goodsFormat");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.printList(goodsList);

        //处理生成 db_goods_car_mini 的sql
        handleGoodsCarSql(goodsCarList, goodsList);

/*
        Set<String> matchFormatSet = new HashSet<>();
        Set<String> unMatchFormatSet = new HashSet<>();
        List<Map<String, String>> unMatchGoodsCarList = new ArrayList<>();
        for(Map<String, String> gc : goodsCarList){
            String format = gc.get("goodsFormat");
            Map<String, String> goods = getGoods(format, goodsList);
            if(goods==null){
//                Print.info("没有上架的型号："+format);
                unMatchFormatSet.add(format);
                unMatchGoodsCarList.add(gc);
            }else{
                matchFormatSet.add(format);
            }
        }

        Print.info("");
        Print.info("已上架的型号："+matchFormatSet.size());
        Print.info(matchFormatSet);
        Print.info("");
        Print.info("没有上架的型号："+unMatchFormatSet.size());
        Print.info(unMatchFormatSet);
        Print.info("");

        Set<String> set = new HashSet<>();
        set.addAll(matchFormatSet);
        set.addAll(unMatchFormatSet);
        Print.info(set.size());

        for(Map<String, String> goods : goodsList){
            String goodsFormat = goods.get("goodsFormat");
            if(!set.contains(goodsFormat)){
                Print.info("奇怪的型号："+goodsFormat);
            }
        }

        String excelPath = path + "待处理的数据/箭冠补充数据/最终数据/";
//        ExcelExporter.exportCarGoodsData(excelPath, "箭冠机滤未上架的型号-车型", unMatchGoodsCarList);
*/

    }

    public Map<String, String> getGoods(String format, List<Map<String, String>> goodsList){
        for(Map<String, String> goods : goodsList){
            if(format.equals(goods.get("goodsFormat"))){
                return goods;
            }
        }
        return null;
    }

    public void handleGoodsCarSql(List<Map<String, String>> goodsCarList, List<Map<String, String>> goodsList){
        String filePath = path + "待处理的数据/箭冠补充数据/最终数据/";
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(filePath, commonMapper);
        for(Map<String, String> gc : goodsCarList){
            sqlGen.handleGoodsCar(goodsList, gc.get("carId"), gc.get("goodsFormat"));
        }

        GoodsCarSqlGen.GOODS_CAR_TABLE = "db_goods_car_mini";
        sqlGen.handleSql("db_goods_car_mini");
    }

}
