package 库存商品处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/21.
 */
public class TempTest extends BaseTest {
    private Common common;

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";

        String excel = path + "机油滤清器商品数据与车型关系20161017.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("机油滤型号1", "goodsFormat");
        attrMap.put("id", "lyId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> lyIdGoodsList = readerXLSX.getDataList();
        Print.printList(lyIdGoodsList);

        attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> goodsList = readerXLSX.getDataList();
        Print.printList(goodsList);

        common = new Common(commonMapper);
        List<Map<String, String>> goodsCarList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIdSet = common.getLyIdSet(goodsFormat, lyIdGoodsList);
            if(!lyIdSet.isEmpty()){
                Collection<Map<String, String>> matchGoodsCarList = common.getMatchGoodsCarList(lyIdSet);
                common.handleMatchGoodsCarList(goods, matchGoodsCarList);
                goodsCarList.addAll(matchGoodsCarList);
            }
        }
        Print.info("\n========== 需要处理的数据 ==========");
        Print.printList(goodsCarList);
        Print.info("");

        //判断有没有重复的数据
        Set<String> oldGoodsCarSet = getOldGoodsCarSet();
        List<Map<String, String>> repeatGoodsList = new ArrayList<>();
        int size = goodsCarList.size();
        for(int i=0; i<size; i++){
            Map<String, String> data = goodsCarList.get(i);
            String key = getKey(data);
            if(oldGoodsCarSet.contains(key)){
                if(goodsCarList.remove(data)) {
                    i--;
                    size--;
                    repeatGoodsList.add(data);
                }else{
                    Print.info("删除重复数据失败："+data);
                }
            }
        }

        Print.info("存在重复的数据："+repeatGoodsList.size());
        Print.info("有用数据："+goodsCarList.size());

        String filePath = path + "处理后/机油滤清器/";
        common.exportGoodsCarExcel("机滤-车款适配关系-补充", filePath, goodsCarList);

    }

    private String getKey(Map<String, String> data){
        return data.get("carId")+"_"+data.get("goodsFormat");
    }
    private Set<String> getOldGoodsCarSet() throws Exception{

        String filePath = path + "处理后/机油滤清器/";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("id", "carId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "机滤-车款适配关系-20161019.xlsx");
        List<Map<String, String>> oneDataList = readerXLSX.getDataList();
        Print.printList(oneDataList);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "机滤-车款适配关系(一对多)-20161019.xlsx");
        List<Map<String, String>> multiDataList = readerXLSX.getDataList();
        Print.printList(multiDataList);

        oneDataList.addAll(multiDataList);

        Set<String> keySet = new HashSet<>();
        for(Map<String, String> data : oneDataList){
            String key = getKey(data);
            keySet.add(key);
        }

        return keySet;
    }


    // 2016-10-24
    @Test
    public void test_1024() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";

        String excel = path + "OK状态下没有匹配上的商品-20161024.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("产品品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> kongqilv = readerXLSX.getDataList();
        Print.printList(kongqilv);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> kongtiaolv = readerXLSX.getDataList();
        Print.printList(kongtiaolv);

        //力洋id关系数据
        attrMap = new HashMap<>();
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("id", "lyId");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 2);
        List<Map<String, String>> kongqilvLyIds = readerXLSX.getDataList();
        Print.printList(kongqilvLyIds);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 4);
        List<Map<String, String>> kongtiaolvLyIds = readerXLSX.getDataList();
        Print.printList(kongtiaolvLyIds);

        common = new Common(commonMapper);

        //处理空气滤清器
        common.handleGoodsCar(kongqilv, kongqilvLyIds, "空气滤清器补充", path + "处理后/空气滤清器/");

        //处理空调滤清器
        common.handleGoodsCar(kongtiaolv, kongtiaolvLyIds, "空调滤清器补充", path + "处理后/空调滤清器/");
    }


    //2016-10-31
    @Test
    public void test_1031() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";
        String excel = path + "1031/1级别商品完成情况整理表20161021";
        //火花塞
        List<Map<String, String>> huoHuaList = Common.getOKGoodsList(excel, 1);
        //刹车片
        List<Map<String, String>> shaCheList = Common.getOKGoodsList(excel, 2);

        excel = path + "1031/1级别商品与力洋ID对应关系20161027";
        //力洋id火花塞
        List<Map<String, String>> lyHuoHuaList = Common.getLyIdGoodsList(excel, 1);
        //力洋id刹车片
        List<Map<String, String>> lyShaCheList = Common.getLyIdGoodsList(excel, 2);

        excel = path + "monkey商品库/力洋id博世刹车片数据.xls";
        List<Map<String, String>> mkLyShaCheList = Common.getMonkeyLyIdGoodsList(excel);


        common = new Common(commonMapper);
        String filePath = path+"1031/处理后的/";

        //处理火花塞
        common.handleGoodsCar(huoHuaList, lyHuoHuaList, "火花塞数据", filePath);

        //处理刹车片
        lyShaCheList.addAll(mkLyShaCheList);
        common.handleGoodsCar(shaCheList, lyShaCheList, "刹车片数据", filePath);

    }

}
