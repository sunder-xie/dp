package temporary.epc;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/18.
 */
public class 数据比较Test extends BaseTest{

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/云配商品数据/20161018/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("g.oe_num", "oeNum");
        attrMap.put("gt.price", "price");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path + "佳宝有价格有oe码的商品数据.xls", attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        attrMap = new HashMap<>();
        attrMap.put("oe_number", "oeNum");
        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path + "epc-oe码.xls", attrMap.size());
        List<Map<String, String>> oeDataList = readerXLS.getDataList();
        Print.info(oeDataList.size());
        Print.info(oeDataList.get(0));

        Set<String> oeSet = new HashSet<>();
        for(Map<String, String> oeData : oeDataList){
            oeSet.add(oeData.get("oeNum"));
        }

        List<Map<String, String>> matchGoodsList = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for(Map<String, String> goods : goodsList){
            String oeNum = StrUtil.handleOe(goods.get("oeNum"));
            if(oeSet.contains(oeNum)){
                matchGoodsList.add(goods);
                set.add(oeNum);
            }
        }
        Print.info("匹配上的商品："+matchGoodsList.size());
        Print.info("匹配上的oe码："+set.size());


        String[] heads = new String[]{"oe码", "价格"};
        String[] fields = new String[]{"oeNum", "price"};

        PoiUtil poiUtil = new PoiUtil();
//        poiUtil.exportXlsxWithMap("匹配上的数据", path, heads, fields, matchGoodsList);

        attrMap = new HashMap<>();
        attrMap.put("gc.car_id", "carId");
        attrMap.put("g.oe_number", "oeNum");
        attrMap.put("p.id", "picId");
        attrMap.put("p.epc_pic", "epc_pic");
        attrMap.put("p.epc_pic_num", "epc_pic_num");
        attrMap.put("p.epc_index", "epc_index");

        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path + "epc有图片的oe码.xls", attrMap.size());
        List<Map<String, String>> picOeDataList = readerXLS.getDataList();
        Print.info(picOeDataList.size());
        Print.info(picOeDataList.get(0));

        //Print.info(set);

        List<Map<String, String>> matchPicOeDataList = new ArrayList<>();
        for(Map<String, String> pe : picOeDataList){
            String poe = pe.get("oeNum");
//            Print.info(poe);
            if(set.contains(poe)){
                matchPicOeDataList.add(pe);
            }
        }
        Print.info("有图有价的oe码："+matchPicOeDataList.size());

    }

}
