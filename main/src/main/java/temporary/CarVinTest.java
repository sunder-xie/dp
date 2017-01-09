package temporary;

import base.BaseTest;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/12/27.
 */
public class CarVinTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/";

        String excel = path + "车架号适配机滤整理表-1226(2).xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("车架", "vin");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 2);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        Set<String> vinSet = new HashSet<>();
        List<String> vinList = getAllVin();
        for(Map<String, String> data : dataList){
            String vin = data.get("vin");
            if(!vinList.contains(vin)){
                Print.info("新的vin码："+vin);
            }else{
                vinSet.add(vin);
            }
        }

        List<Map<String, Object>> mapList = new ArrayList<>();
        Set<String> set2 = new HashSet<>();
        for(String vin : vinSet){
            List<Map<String, Object>> carInfoList = getCarInfoByVin(vin);
            if(carInfoList.isEmpty()){
                set2.add(vin);
            }

            setVin(vin, carInfoList);

            mapList.addAll(carInfoList);
        }
        Print.info(set2.size());
        Print.printList(mapList);


        String[] heads = new String[]{"vin", "brand", "company", "series", "model", "year", "power", "car_models"};
        PoiUtil poiUtil = new PoiUtil();

        poiUtil.exportXlsxWithMap("vin码车型对应关系", path, heads, ObjectUtil.objToStrMapList(mapList));
    }

    public List<String> getAllVin(){
        String sql = "select distinct vin from db_car_vin";
        return commonMapper.selectOneFieldBySql(sql);
    }

    public List<Map<String, Object>> getCarInfoByVin(String vin){
        String sql = "select ca.* " +
                "from db_car_vin cv,db_car_all ca " +
                "where cv.vin='" +vin+ "' and cv.new_l_id=ca.new_l_id " +
                "group by ca.car_models_id";

        return commonMapper.selectListBySql(sql);
    }

    public void setVin(String vin, List<Map<String, Object>> carInfoList){
        for(Map<String, Object> map : carInfoList){
            map.put("vin", vin);
        }
    }

}
