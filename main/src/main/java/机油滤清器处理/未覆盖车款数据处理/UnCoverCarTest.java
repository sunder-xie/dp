package 机油滤清器处理.未覆盖车款数据处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import 机油滤清器处理.生成sql脚本.GoodsCarSqlGen;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/10.
 */
public class UnCoverCarTest extends BaseTest {
    @Test
    public void justTest() throws Exception{

    }

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/未覆盖车款数据/处理后的/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");
        attrMap.put("排量", "power");
        attrMap.put("年款", "year");
        attrMap.put("车款", "carName");
        attrMap.put("奥胜机滤编码", "goodsFormat");
        attrMap.put("备注", "remark");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path + "机滤未覆盖的车款-20160929(20161010-处理后).xlsx", 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Set<String> formatSet = new HashSet<>();
        for(Map<String, String> data : dataList){
            String remark = data.get("remark");
            if("OK".equals(remark)){
                formatSet.add(data.get("goodsFormat"));
            }
        }
        Print.info(formatSet);

        List<Map<String, String>> yxGoodsList = getGoodsDataList("云修机油滤清器.xls");
        Set<String> availableFormats = availableGoodsFormats();

        Set<String> aoFormatSet = new HashSet<>(); //奥盛号
        Set<String> yxFormatSet = new HashSet<>(); //云修号
        for(String gf : formatSet){
            boolean flag = true;
            for(Map<String, String> goods : yxGoodsList){
                String goodsFormat = goods.get("goodsFormat");
                if(gf.equals(goodsFormat)){
                    flag = false;
                    if(!availableFormats.contains(gf)){
                        Print.info("存在新的云修号："+gf);
                    }
                    yxFormatSet.add(gf);
                    break;
                }
            }
            if(flag){
                Print.info("非云修号："+gf);
                aoFormatSet.add(gf);
            }
        }
        Print.info("云修号："+yxFormatSet.size());
        Print.info("奥盛号："+aoFormatSet.size());

        Map<String, String> relMap = yxAsGoodsFormatMap();
        for(String af : aoFormatSet){
            String yf = relMap.get(af);
            if(yf!=null){
                Print.info("存在对应的云修号："+yf+"  奥盛号："+af);
            }
        }


        List<Map<String, String>> asGoodsCarList = new ArrayList<>();
        //TODO 处理云修型号
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);
        for(Map<String, String> data : dataList){
            String format = data.get("goodsFormat");
            if(yxFormatSet.contains(format)){
                sqlGen.handleGoodsCar(yxGoodsList, data.get("carId"), format);

            }else if(aoFormatSet.contains(format)){
                asGoodsCarList.add(data);
            }
        }
        sqlGen.handleSql("add_car_oil_filter_yx");


        //TODO 处理奥盛型号
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"carId", "goodsFormat", "brand", "company", "series", "model", "power", "year", "carName"};
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("可以补充的奥盛型号", path, heads, fields, asGoodsCarList);

    }

    //云修-奥盛 商品编号对应关系
    private Map<String, String> yxAsGoodsFormatMap() throws Exception{
        String excel = "/Users/huangzhangting/Desktop/机滤数据处理/云修机滤奥盛号与云修号对应关系.xlsx";
        Map<String, String> attrMap = new HashMap<>();
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

        return relMap;
    }

    //电商商品数据
    private List<Map<String, String>> getGoodsDataList(String fileName) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/电商机滤商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("new_goods_sn", "goodsSn");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }

    //可用的机滤型号信息
    private List<Map<String, String>> availableGoodsInfo() throws Exception{
        String excel = "/Users/huangzhangting/Desktop/机滤数据处理/安心礼包机滤商品信息/可用的机滤型号信息-20161009.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("品牌", "brand");
        attrMap.put("商品型号", "goodsFormat");
        attrMap.put("商品sn", "goodsSn");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info("可用的机滤型号信息："+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }
    private Set<String> availableGoodsFormats() throws Exception{
        List<Map<String, String>> goodsList = availableGoodsInfo();
        Set<String> set = new HashSet<>();
        for(Map<String, String> goods : goodsList){
            set.add(goods.get("goodsFormat"));
        }
        Print.info("可用的机滤型号信息："+set.size());

        return set;
    }

}
