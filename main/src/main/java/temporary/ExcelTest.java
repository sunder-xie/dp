package temporary;

import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/5/16.
 */
public class ExcelTest {
    String path;
    Writer writer;


    //vin码数据处理
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/";
        String excel = path+"vin码数据.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("vin", "vin");
        attrMap.put("new_l_id", "new_l_id");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, 2);

        init();

        handleCarVin(readerXLS.getDataList());
    }

    public void init(){
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path+"insert_car_vin_"+dateStr+".sql");
        IoUtil.writeFile(writer, "truncate table db_car_vin;\n");
        IoUtil.writeFile(writer, "alter table db_car_vin add UNIQUE KEY uk_vin_lid(vin, new_l_id);\n");
    }

    public void handleCarVin(List<Map<String, String>> dataList){
        Print.info("记录数："+dataList.size());
        if(dataList.isEmpty()){
            return;
        }
        int count = 1000;
        int size = dataList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addValueSql(dataList.get(i), sb);
            if((i+1)%count==0){
                writeSql(sb);
                sb.setLength(0);
                continue;
            }
            if(i==lastIdx){
                writeSql(sb);
            }
            sb.append(",");
        }
    }

    public void addValueSql(Map<String, String> data, StringBuilder sb){
        sb.append("('").append(data.get("vin"));
        sb.append("', '").append(data.get("new_l_id")).append("')");
    }

    public void writeSql(StringBuilder sb){
        sb.insert(0, "insert into db_car_vin(vin, new_l_id) values");
        sb.append(";\n");
        IoUtil.writeFile(writer, sb.toString());
    }



    public void test1() throws Exception{
        path = "/Users/huangzhangting/Downloads/";
        String excel = path + "TM_EPC_CD_BodyDesc.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("Body", "body");
        attrMap.put("Description_CN", "desc_cn");
        attrMap.put("Description_EN", "desc_en");
        attrMap.put("Catalog_Code", "code");


    }


}
