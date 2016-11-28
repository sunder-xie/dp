package crawl.sohu;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/11/28.
 */
public class CheckCarTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/数据抓取/搜狐/";

        String excel = path + "搜狐-淘汽车型匹配验证-反馈-20161128.xls";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("c6.tq_car", "tqCarId");
        attrMap.put("c6.淘汽品牌", "tqBrand");
        attrMap.put("c6.淘汽厂家", "tqCompany");
        attrMap.put("c6.淘汽车系", "tqSeries");
        attrMap.put("c6.淘汽车型", "tqModel");
        attrMap.put("c6.淘汽排量", "tqPower");
        attrMap.put("c6.淘汽年款", "tqYear");
        attrMap.put("c6.淘汽车款", "tqCarName");
        attrMap.put("c6.进口合资国产", "importInfo");
        attrMap.put("sc.搜狐品牌", "shBrand");
        attrMap.put("sc.搜狐厂家", "shCompany");
        attrMap.put("sc.搜狐车型", "shModel");
        attrMap.put("sc.搜狐年款", "shYear");
        attrMap.put("sc.搜狐车款", "shCarName");
        attrMap.put("sc.sh_car", "shCarId");
        attrMap.put("核对结果", "status");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        List<Map<String, String>> needCheckList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            String status = data.get("status");
            if("OK".equals(status) && compareTransmissionType(data)){
                needCheckList.add(data);
            }
        }
        Print.info(needCheckList.size());


        String[] heads = new String[]{"tq_car", "淘汽品牌", "淘汽厂家", "淘汽车系", "淘汽车型", "淘汽排量", "淘汽年款", "淘汽车款",
                "进口合资国产", "搜狐品牌", "搜狐厂家", "搜狐车型", "搜狐年款", "搜狐车款", "sh_car", "核对结果"};

        String[] fields = new String[]{"tqCarId", "tqBrand", "tqCompany", "tqSeries", "tqModel", "tqPower", "tqYear", "tqCarName",
                "importInfo", "shBrand", "shCompany", "shModel", "shYear", "shCarName", "shCarId", "status"};

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("搜狐-淘汽车型匹配验证", path, heads, fields, needCheckList);

    }

    public boolean compareTransmissionType(Map<String, String> data){
        String tqCarName = StrUtil.toUpCase(data.get("tqCarName"));
        String shCarName = StrUtil.toUpCase(data.get("shCarName"));

        if(shCarName.contains("DSG")){
            return tqCarName.contains("双离合");
        }
        if(shCarName.contains("CVT")){
            return tqCarName.contains("无级");
        }
        if(shCarName.contains("手自一体") || shCarName.contains("自动") || shCarName.contains("AT")){
            return tqCarName.contains("自动");
        }
        if(shCarName.contains("手动") || shCarName.contains("MT")){
            return tqCarName.contains("手动");
        }

        return true;
    }

}
