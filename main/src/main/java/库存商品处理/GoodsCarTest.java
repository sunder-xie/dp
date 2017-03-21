package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
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
    public void test_0320() throws Exception{
        String brandName = "瓦尔塔电瓶";
        String dataExcel = "瓦尔塔电瓶与车型关系汇总.xlsx";
        String goodsExcel = brandName+".xls";

        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/未处理/美龄/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品型号", "goodsFormat");
        attrMap.put("力洋ID", "lyId");

        String excel = path + dataExcel;
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        List<Map<String, String>> goodsList = getDsGoodsList(goodsExcel);

        List<Map<String, String>> matchList = new ArrayList<>();
        Set<String> matchFormatSet = new HashSet<>();
        Set<String> unMatchFormatSet = new HashSet<>();
        for(Map<String, String> data : dataList){
            String goodsFormat = getRepGoodsFormat(data.get("goodsFormat"));
            if("".equals(goodsFormat)){
                Print.info("没有商品型号的数据："+data);
                continue;
            }
            String goodsId = getGoodsId3(goodsList, goodsFormat);
            if(goodsId!=null){
                data.put("goodsId", goodsId);
                matchList.add(data);
                matchFormatSet.add(goodsFormat);
            }else{
                unMatchFormatSet.add(handleGoodsFormat(goodsFormat));
            }
        }

        writer = IoUtil.getWriter(path+brandName+"_商品匹配结果.txt");
        IoUtil.writeFile(writer, "匹配上的型号:\n"+matchFormatSet.toString());
        IoUtil.writeFile(writer, "\n\n没有匹配上的型号:\n"+unMatchFormatSet.toString());
        IoUtil.closeWriter(writer);

        writer = IoUtil.getWriter(path + brandName+"_ly_id_goods.sql");
        batchInsertIntoLyIdGoods(matchList);
        IoUtil.closeWriter(writer);
    }
    private String handleGoodsFormat(String goodsFormat){
        return StrUtil.toUpCase(goodsFormat).replace("-", "");
    }
    private String getGoodsId3(List<Map<String, String>> goodsList, String goodsFormat){
        goodsFormat = handleGoodsFormat(goodsFormat);
        for(Map<String, String> goods : goodsList){
            String goods_format = handleGoodsFormat(goods.get("goods_format"));
            if(goodsFormat.equals(goods_format)){
                return goods.get("goods_id");
            }
            String brand_code = handleGoodsFormat(goods.get("brand_code"));
            if(goodsFormat.equals(brand_code)){
                return goods.get("goods_id");
            }
        }
        return null;
    }
    private void batchInsertIntoLyIdGoods(List<Map<String, String>> dataList){
        if(dataList.isEmpty()){
            Print.info("没有数据");
            return;
        }
        int count = 500;
        int size = dataList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            appendValue(sql, dataList.get(i));
            if((i+1)%count==0){
                writeSql(sql);
                sql.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeSql(sql);
                sql.setLength(0);
                break;
            }
            sql.append(",");
        }
    }
    private void appendValue(StringBuilder sql, Map<String, String> data){
        sql.append("(");
        sql.append(data.get("goodsId"));
        sql.append(",'");
        sql.append(data.get("lyId"));
        sql.append("')");
    }
    private void writeSql(StringBuilder sql){
        sql.insert(0, "insert into ly_id_goods(goods_id,ly_id) values ");
        sql.append(";\n");
        IoUtil.writeFile(writer, sql.toString());
    }
    private String getRepGoodsFormat(String goodsFormat){
        Map<String, String> map = getGoodsFormatRepMap();
        String str = map.get(goodsFormat);
        if(str==null){
            return goodsFormat;
        }
        return str;
    }
    private Map<String, String> getGoodsFormatRepMap(){
        Map<String, String> map = new HashMap<>();
        map.put("D2665RT2", "D26-60(65)-R-T2");
        map.put("80D26L", "2S 80D26 L");

        map.put("L2350MF", "L2350");

        map.put("46B24L", "46B24L-MF");
        map.put("46B24L/R", "46B24L-MF");
        map.put("46B24LX", "46B24LX-MF");
        map.put("46B24R", "46B24R-MF");
        map.put("46B24L/R", "46B24R-MF");
        map.put("55530", "555 30MF");
        map.put("55D23L", "55D23L-MF");
        map.put("55D23L\\R", "55D23L-MF");
        map.put("55D23R", "55D23R-MF");
        map.put("55D23L\\R", "55D23R-MF");
        map.put("55D26R/L", "55D26L-MF");
        map.put("55D26L", "55D26L-MF");
        map.put("55D26LX", "55D26LX-MF");
        map.put("55D26R/L", "55D26R-MF");
        map.put("55D26R", "55D26R-MF");
        map.put("56613", "566 13MF");
        map.put("6QW36反", "6-QW-36L");
        map.put("6QW36", "6-QW-36R");
        map.put("L2400", "L2 400MF");
        map.put("80D26R\\L", "80D26R-MF");
        map.put("80D26R/L", "80D26R-MF");
        map.put("80D26R", "80D26R-MF");
        map.put("95D31R\\L", "95D31L-MF");
        map.put("95D31L", "95D31L-MF");
        map.put("95D31R\\L", "95D31R-MF");
        map.put("95D31R", "95D31R-MF");
        return map;
    }

}
