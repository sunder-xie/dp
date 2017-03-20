import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 17/3/20.
 */
public class CarBrandNameEnTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Documents/每周工作/2017/0320/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "id");
        attrMap.put("英文名称", "brandNameEn");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path+"车型品牌--0317.xls", attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        writer = IoUtil.getWriter(path + "update_car_brand_name.sql");
        String specialStr = " "; //不是普通的空格
        for(Map<String, String> data : dataList){
            String brandNameEn = data.get("brandNameEn").trim();
            if(brandNameEn.contains(specialStr)){
                brandNameEn = brandNameEn.replace(specialStr, "");
            }
            if("".equals(brandNameEn)){
                continue;
            }
            StringBuilder sql = new StringBuilder();
            sql.append("update db_car_category set brand_name_en='");
            sql.append(brandNameEn);
            sql.append("' where id=");
            sql.append(data.get("id"));
            sql.append(";\n");
            IoUtil.writeFile(writer, sql.toString());
        }
    }
}
