package 商品车型关系数据处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 17/5/10.
 */
public class JiHangTest extends BaseTest {

    @Test
    public void test_match_goods() throws Exception {
        path = "/Users/huangzhangting/Desktop/数据处理/";

        String goodsExcel = path + "吉航电瓶.xls";
        String gcExcel = path + "宝骏吉航电瓶与力洋ID.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goods_id");
        attrMap.put("goods_name", "goods_name");
        attrMap.put("goods_format", "goods_format");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(goodsExcel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        attrMap = new HashMap<>();
        attrMap.put("商品型号", "goods_format");
        attrMap.put("力洋ID", "ly_id");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(gcExcel);
        List<Map<String, String>> gcList = readerXLSX.getDataList();
        Print.info(gcList.size());
        Print.info(gcList.get(0));

        Set<String> matchFormatSet = new HashSet<>();
        Set<String> unMatchFormatSet = new HashSet<>();
        for(Map<String, String> gcData : gcList){
            Map<String, String> goods = compareFormat(gcData, goodsList);
            if(goods==null){
                unMatchFormatSet.add(gcData.get("goods_format"));
            }else{
                matchFormatSet.add(gcData.get("goods_format"));
            }
        }
        Print.info("\n匹配上的规格型号："+matchFormatSet);
        Print.info("\n没有匹配上的规格型号："+unMatchFormatSet);


        //导出没有匹配上的规格型号
        exportUnMatchFormat(unMatchFormatSet);

    }

    public Map<String, String> compareFormat(Map<String, String> gcData, List<Map<String, String>> goodsList){
        String format = StrUtil.toUpCase(gcData.get("goods_format"));
        for(Map<String, String> goods : goodsList){
            String goodsFormat = StrUtil.toUpCase(goods.get("goods_format"));
            List<String> list = splitFormat(goodsFormat);
            if(list.contains(format)){
                return goods;
            }
        }
        return null;
    }

    public List<String> splitFormat(String format){
        String[] strings = format.split("/");
        return Arrays.asList(strings);
    }

    public void exportUnMatchFormat(Set<String> unMatchFormatSet) throws Exception{
        List<Map<String, String>> unMatchList = new ArrayList<>();
        for(String str : unMatchFormatSet){
            Map<String, String> map = new HashMap<>();
            map.put("goodsFormat", str);
            unMatchList.add(map);
        }
        String[] heads = new String[]{"规格型号"};
        String[] fields = new String[]{"goodsFormat"};
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("没有匹配上的规格型号", path, heads, fields, unMatchList);
    }

}
