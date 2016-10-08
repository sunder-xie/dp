package temporary.云修;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/9/29.
 */
@Deprecated
public class 机滤补充数据处理 extends BaseTest {
    private static final int BW_CODE = 6; //豹王机滤
    private static final int ML_CODE = 7; //马勒机滤

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/补充数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("g.goods_format", "goodsFormat");
        attrMap.put("gc.liyang_Id", "lyId");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "豹王机滤-力洋id.xls", attrMap.size());

        List<Map<String, String>> bwDataList = readerXLS.getDataList();
        Print.info(bwDataList.size());
        Print.info(bwDataList.get(0));

        writeSql(BW_CODE, bwDataList);


        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "马勒机滤-力洋id.xls", attrMap.size());

        List<Map<String, String>> mlDataList = readerXLS.getDataList();
        Print.info(mlDataList.size());
        Print.info(mlDataList.get(0));

        writeSql(ML_CODE, mlDataList);
    }

    private void writeSql(int brandCode, List<Map<String, String>> dataList){

        writer = IoUtil.getWriter(path + "insert_goods_ly_id_rel_"+brandCode+".sql");

        for(Map<String, String> data : dataList){
            StringBuilder sql = new StringBuilder();
            sql.append("insert ignore into temp_goods_lyid_rel(goods_format, ly_id, brand_code) value ");
            sql.append("('").append(data.get("goodsFormat"));
            sql.append("', '").append(data.get("lyId"));
            sql.append("', ").append(brandCode);
            sql.append(");\n");

            IoUtil.writeFile(writer, sql.toString());
        }

        IoUtil.closeWriter(writer);
    }

}
