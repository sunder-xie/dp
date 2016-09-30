package temporary.车型数据处理;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by huangzhangting on 16/9/20.
 */
public class LiYangCarTest extends BaseTest {

    @Test
    public void testPrice() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/";
        String excel = path + "需要处理价格的力洋车型.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("leyel_id", "lid");
        attrMap.put("guide_price", "price");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        writer = IoUtil.getWriter(path + "update_guide_price.sql");

        for(Map<String, String> data : dataList){
            String[] ps = data.get("price").split("-");
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);
            double gp = (Double.parseDouble(ps[0])+Double.parseDouble(ps[1]))/2;

            String sql = "update db_car_info_all set guide_price='"+df.format(gp)
                    +"' where leyel_id='"+data.get("lid")+"';\n";

            IoUtil.writeFile(writer, sql);
        }

        IoUtil.closeWriter(writer);
    }


}
