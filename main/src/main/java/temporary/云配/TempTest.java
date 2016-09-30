package temporary.云配;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/7/25.
 */
public class TempTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/epc 2.0/";
        String excel = path + "云配商品oe码.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("oe码", "oe");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        writer = IoUtil.getWriter(path+"insert_temp_oe.sql");
        for(Map<String, String> data : dataList){
            String sql = "insert into temp_oe(oe) value('"+data.get("oe")+"');\n";
            IoUtil.writeFile(writer, sql);
        }
        IoUtil.closeWriter(writer);
    }


    @Test
    public void testOe() throws Exception{
        path = "/Users/huangzhangting/Desktop/云配商品数据/";
        String excel = path + "明锐达商品数据-20160919.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品编码", "goodsSn");
        attrMap.put("商品名称", "goodsName");
        attrMap.put("oe码", "oeNum");
        attrMap.put("价格", "price");
        attrMap.put("云修价", "yxPrice");
        attrMap.put("品质", "quality");
        attrMap.put("品牌名称", "brandName");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        List<String> oeNumList = commonMapper.selectOneFieldBySql("select distinct oe_number from center_goods");
        Print.info(oeNumList.size());

        List<Map<String, String>> matchList = new ArrayList<>();
        List<Map<String, String>> unMatchList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            String oe = StrUtil.handleOe(goods.get("oeNum"));
            //Print.info(oe);
            if(oeNumList.contains(oe)){
                matchList.add(goods);
            }else{
                unMatchList.add(goods);
            }
        }

        Print.info(matchList.size());
        Print.info(unMatchList.size());

        //导出excel
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"商品编码", "商品名称", "oe码", "价格", "云修价", "品质", "品牌名称"};
        String[] fields = new String[]{"goodsSn", "goodsName", "oeNum", "price", "yxPrice", "quality", "brandName"};
        poiUtil.exportXlsxWithMap("明锐达商品数据-oe码匹配上的", path, heads, fields, matchList);
    }


    @Test
    public void testOe2() throws Exception{
        path = "/Users/huangzhangting/Desktop/云配商品数据/";
        String excel = path + "佳宝商品数据-20160920.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品编码", "goodsSn");
        attrMap.put("商品名称", "goodsName");
        attrMap.put("oe码", "oeNum");
        attrMap.put("价格", "price");
        attrMap.put("云修价", "yxPrice");
        attrMap.put("品质", "quality");
        attrMap.put("品牌名称", "brandName");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        excel = path + "epc五菱oe码.xls";
        attrMap = new HashMap<>();
        attrMap.put("oe_number", "oeNum");
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> oeMapList = readerXLS.getDataList();
        Print.info(oeMapList.size());
        Print.info(oeMapList.get(0));


        List<String> oeNumList = new ArrayList<>();
        for(Map<String, String> oeMap : oeMapList){
            oeNumList.add(oeMap.get("oeNum"));
        }
        Print.info(oeMapList.size());

        List<Map<String, String>> matchList = new ArrayList<>();
        List<Map<String, String>> unMatchList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            String oe = StrUtil.handleOe(goods.get("oeNum"));
            Print.info(oe);
            if(oeNumList.contains(oe)){
                matchList.add(goods);
            }else{
                unMatchList.add(goods);
            }
        }

        Print.info(matchList.size());
        Print.info(unMatchList.size());

        //导出excel
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"商品编码", "商品名称", "oe码", "价格", "云修价", "品质", "品牌名称"};
        String[] fields = new String[]{"goodsSn", "goodsName", "oeNum", "price", "yxPrice", "quality", "brandName"};
        poiUtil.exportXlsxWithMap("佳宝商品数据-oe码匹配上的", path, heads, fields, matchList);
    }

}
