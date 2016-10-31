package 机油滤清器处理.临时处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

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
}
