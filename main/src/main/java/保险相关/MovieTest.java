package 保险相关;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLSX;
import org.apache.shiro.crypto.AesCipherService;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/12/8.
 */
public class MovieTest extends BaseTest {
    AesCipherService aesCipherService = new AesCipherService();


    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/保险项目/后台系统/电影票项目/电影票兑换码/";

        String excel = path + "14张电影票-1226.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("兑换码", "code");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        List<Map<String, String>> mapList = new ArrayList<>();
        int len = 12;
        Set<String> set = new HashSet<>();
        for(Map<String, String> data : dataList){
            String code = data.get("code");
            if("".equals(code)){
                continue;
            }

            if(code.length() != len){
                Print.info("长度有疑问："+code);
            }
            if(!set.add(code)){
                Print.info("存在重复的兑换码："+code);
            }

            data.put("encryptCode", encryptStr(code));
            mapList.add(data);
        }

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlName = path + "mana_insert_data_" + dateStr + ".sql";

        writer = IoUtil.getWriter(sqlName);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        int count = 100;
        int size = mapList.size();
        Print.info("最终数量："+size);
        int lastIdx = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            appendVal(sql, mapList.get(i));
            if((i+1)%count==0){
                writeSql(sql);
                sql.setLength(0);
                continue;
            }
            if(i==lastIdx){
                writeSql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }
    private void appendVal(StringBuilder sql, Map<String, String> data){
        sql.append("(@nowTime,@nowTime,1,1,'");
        sql.append(data.get("encryptCode"));
        sql.append("')");
    }
    private void writeSql(StringBuilder sql){
        sql.insert(0, "insert into mana_coupon(start_date,end_date,coupon_type_id,is_encrypt,coupon_content) values");
        sql.append(";\n");
        IoUtil.writeFile(writer, sql.toString());
    }



    private String encryptStr(String string){
        String key = LocalConfig.MANA_ENCRYPT_KEY;
        byte[] keys = StrUtil.strToBytes(key);

        //加密
        String text = aesCipherService.encrypt(string.getBytes(), keys).toHex();
        //Print.info(text);

        return text;
    }

    @Test
    public void testSql(){
        List<String> codeList = new ArrayList<String>(){{
            add("test00000000");
            add("test11111111");
            add("test22222222");
            add("test33333333");
            add("test44444444");
        }};

        Print.info("\n");
        for(String code : codeList){
            StringBuilder sql = new StringBuilder();
            sql.append("insert into mana_coupon(start_date,end_date,coupon_type_id,is_encrypt,coupon_content) ");
            sql.append("value(@nowTime,@nowTime,1,1,'");
            sql.append(encryptStr(code));
            sql.append("');");

            Print.info(sql.toString());

        }
        Print.info("\n");
    }

}
