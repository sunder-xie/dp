package 机油滤清器处理.处理后数据统计;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/13.
 */
public class 数据统计Test extends BaseTest {
    private static Set<String> car_id_set = new HashSet<>();

    private void addCarIdSet(List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            car_id_set.add(data.get("carId"));
        }
        Print.info("覆盖车款id："+car_id_set.size());
    }

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("car_id", "carId");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path + "已覆盖车款信息.xls", attrMap.size());
        List<Map<String, String>> coverDataList = readerXLS.getDataList();
        Print.info(coverDataList.size());
        Print.info(coverDataList.get(0));

        addCarIdSet(coverDataList);

        String excel = path + "未覆盖车款数据/处理后的/可以补充的奥盛型号-20161010.xlsx";
        attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> aoDataList1 = readerXLSX.getDataList();
        Print.info(aoDataList1.size());
        Print.info(aoDataList1.get(0));

        addCarIdSet(aoDataList1);

        excel = path + "待处理的数据/奥盛机滤/奥盛可以补充的型号.xls";
        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(excel, attrMap.size());
        List<Map<String, String>> aoDataList2 = readerXLS.getDataList();
        Print.info(aoDataList2.size());
        Print.info(aoDataList2.get(0));

        addCarIdSet(aoDataList2);

        excel = path + "待处理的数据/箭冠补充数据/最终数据/箭冠可以补充的型号(修订后)-20161009.xlsx";
        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> jgDataList = readerXLSX.getDataList();
        Print.info(jgDataList.size());
        Print.info(jgDataList.get(0));

        addCarIdSet(jgDataList);

        List<Map<String, Object>> needCheckCarList = commonMapper.selectListBySql(StatisticConfig.needCheckCarsSql());
        Print.info(needCheckCarList.size());
        Print.info(needCheckCarList.get(0));

        List<Map<String, Object>> unCoverDataList = new ArrayList<>();
        for(Map<String, Object> car : needCheckCarList){
            String carId = car.get("id").toString();
            if(!car_id_set.contains(carId)){
                unCoverDataList.add(car);
            }
        }

        int size1 = car_id_set.size();
        int size2 = unCoverDataList.size();
        Print.info("机滤覆盖车款："+size1);
        Print.info("未覆盖车款："+size2);
        Print.info("覆盖率："+(size1*1.0/(size1+size2)));

        ExcelExporter.exportUnCoverCars(path, unCoverDataList);
    }
}
