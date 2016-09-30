package temporary.保险;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/9/19.
 */
public class CarTest extends BaseTest {

    //TODO 生成力洋id，和保险车型编码的对应关系
    @Test
    public void testCar() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/";
        String excel = path + "保险车型数据-20160918.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("车型码", "vehicleCode");
        attrMap.put("利洋车型关系", "lyIds");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> carList = readerXLSX.getDataList();
        Print.info(carList.size());
        Print.info(carList.get(0));
        Print.info(carList.get(1));

        Set<String> vcSet = new HashSet<>();
        boolean checkFlag = true;
        for(Map<String, String> car : carList){
            String vc = car.get("vehicleCode");
            if(!vcSet.add(vc)){
                Print.info("重复的车型编码："+vc);
                checkFlag = false;
            }
        }

        if(!checkFlag){
            return;
        }

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "insert_temp_insurance_car_rel_" + dateStr + ".sql");

        for(Map<String, String> car : carList){
            handleCar(car);
        }

        IoUtil.closeWriter(writer);
    }

    private void handleCar(Map<String, String> car){
        List<String> lyIdList = JsonUtil.strToList(car.get("lyIds"), String.class);
        String vc = car.get("vehicleCode");
        Print.info(vc+"    "+lyIdList.size());

        if(lyIdList.isEmpty()){
            return;
        }

        StringBuilder sql = new StringBuilder();
        for(String lyId : lyIdList){
            sql.append(",('").append(vc);
            sql.append("','").append(lyId);
            sql.append("')");
        }
        sql.deleteCharAt(0);
        sql.insert(0, "insert ignore into temp_insurance_car_rel(vehicle_code, ly_id) values");
        sql.append(";\n");

        IoUtil.writeFile(writer, sql.toString());
    }


    //TODO 力洋id，转换成淘汽车款id
    @Test
    public void test() throws Exception{
        String sql = "select t1.vehicle_code,t2.car_models_id " +
                "from temp_insurance_car_rel t1, db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id " +
                "group by t1.vehicle_code,t2.car_models_id";


        List<Map<String, Object>> dataList = commonMapper.selectListBySql(sql);

        Print.info(dataList.size());
        Print.info(dataList.get(0));

        path = "/Users/huangzhangting/Desktop/安心保险数据对接/";
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "insert_insurance_car_rel_" + dateStr + ".sql");

        IoUtil.writeFile(writer, "select @nowTime := now();\n");


        int count = 500;
        int size = dataList.size();
        int lastIndex = size - 1;
        StringBuilder sqlSb = new StringBuilder();
        for(int i=0; i<size; i++){
            appendVal(sqlSb, dataList.get(i));
            if((i+1)%count==0){
                writeSql(sqlSb);
                sqlSb.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeSql(sqlSb);
            }
            sqlSb.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    private void appendVal(StringBuilder sql, Map<String, Object> data){
        sql.append("('").append(data.get("vehicle_code"));
        sql.append("','").append(data.get("car_models_id"));
        sql.append("', @nowTime)");
    }

    private void writeSql(StringBuilder sql){
        sql.insert(0, "insert ignore into db_insurance_car_rel(vehicle_code, car_id, gmt_create) values ");
        sql.append(";\n");
        IoUtil.writeFile(writer, sql.toString());
    }

}
