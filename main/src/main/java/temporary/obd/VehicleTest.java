package temporary.obd;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/7/17.
 */
public class VehicleTest extends BaseTest {

    /**
     * 处理obd车型数据
     */
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/obd车型数据/";
        String excel = path + "车型数据.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("车型ID", "id");
        attrMap.put("车型名称", "name");
        attrMap.put("车型父级ID", "pid");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Set<String> idSet = new HashSet<>();
        for(Map<String, String> data : dataList){
            if(idSet.add(data.get("id"))){
                continue;
            }
            Print.info("重复数据："+data);
        }

        writer = IoUtil.getWriter(path + "insert_obd_vehicle.sql");

        for(Map<String, String> data : dataList){
            writeSql(data);
        }

        IoUtil.closeWriter(writer);
    }

    public void writeSql(Map<String, String> data){
        String pid = data.get("pid");
        int grade;
        if(StringUtils.isEmpty(pid)){
            pid = "0";
            grade = 1;
        }else{
            grade = 2;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("insert into db_obd_vehicle(id,p_id,vehicle_grade,vehicle_name) value(");
        sb.append(data.get("id")).append(",");
        sb.append(pid).append(",");
        sb.append(grade).append(",'");
        sb.append(data.get("name").trim()).append("');\n");

        IoUtil.writeFile(writer, sb.toString());
    }




    public List<Map<String, Object>> getVehicles(){
        String sql = "select t2.id,t1.vehicle_name as brand,t2.vehicle_name as series,t2.company_name as company" +
                " from" +
                " (select * from db_vehicle where vehicle_grade=1) t1," +
                " (select * from db_vehicle where vehicle_grade=2) t2" +
                " where t1.id=t2.p_id" +
                " order by t1.vehicle_name,t2.vehicle_name";

        return commonMapper.selectListBySql(sql);
    }

    public List<Map<String, Object>> getObdVehicles(){
        String sql = "select t3.id,t1.vehicle_name as brand,t2.vehicle_name as series,t3.vehicle_name model " +
                "from " +
                "(select * from db_obd_vehicle where vehicle_grade=1) t1, " +
                "(select * from db_obd_vehicle where vehicle_grade=2) t2, " +
                "(select * from db_obd_vehicle where vehicle_grade=3) t3 " +
                "where t1.id=t2.p_id and t2.id=t3.p_id " +
                "order by t1.vehicle_name,t2.vehicle_name,t3.vehicle_name";

        return commonMapper.selectListBySql(sql);
    }


    /**
     * 通过程序匹配车型数据
     * */
    @Test
    public void handleVehicle(){
        path = "/Users/huangzhangting/Desktop/obd车型数据/";

        List<Map<String, Object>> vehicleList = getVehicles();
        Print.info(vehicleList.size());
        Print.info(vehicleList.get(0));

        List<Map<String, Object>> obdVehicleList = getObdVehicles();
        Print.info(obdVehicleList.size());
        Print.info(obdVehicleList.get(0));


        writer = IoUtil.getWriter(path + "insert_obd_vehicle_rel.sql");

        for(Map<String, Object> vehicle : vehicleList){
            compareVehicle(vehicle, obdVehicleList);
        }

        IoUtil.closeWriter(writer);
    }

    public void compareVehicle(Map<String, Object> vehicle, List<Map<String, Object>> obdVehicleList){
        String vBrand = vehicle.get("brand").toString();
        String vSeries = vehicle.get("series").toString();

        String obdModelId = null;
        for(Map<String, Object> obdVehicle : obdVehicleList){
            String obdModel = obdVehicle.get("model").toString();
            if(vBrand.equals(obdVehicle.get("brand").toString())){
                if(compareModel(vSeries, obdModel)){
                    Print.info(vSeries+"    "+obdModel);
                    obdModelId = obdVehicle.get("id").toString();
                    break;
                }
            }
        }

        if(obdModelId != null){
            String vehicleId = vehicle.get("id").toString();
            Print.info(vehicleId+"    "+obdModelId);

            List<String> idList = getModelIds(vehicleId);
            for(String id : idList){
                writeRelSql(id, obdModelId);
            }
        }
    }

    public boolean compareModel(String vSeries, String obdModel){
        if(vSeries.contains(obdModel)){
            return true;
        }
        return obdModel.contains(vSeries);
    }

    public List<String> getModelIds(String vehicleId){
        String sql = "select id from db_vehicle where p_id="+vehicleId;
        return commonMapper.selectOneFieldBySql(sql);
    }

    public void writeRelSql(String vehicleId, String obdVehicleId){
        String sql = "insert into db_vehicle_obd_vehicle_rel(vehicle_id,obd_vehicle_id) value("
                +vehicleId+","+obdVehicleId+");\n";

        IoUtil.writeFile(writer, sql);
    }


    /**
     * 处理人工匹配后的车型数据
     * */
    @Test
    public void dataTest() throws Exception{
        path = "/Users/huangzhangting/Desktop/obd车型数据/";
        String excel = path + "淘汽商用车数据-0720.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车辆类型", "type");
        attrMap.put("OBD车型ID", "model_id");
        attrMap.put("OBD车系ID", "series_id");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        List<Map<String, Object>> allVehicles = getAllVehicles();
        Print.info(allVehicles.size());
        Print.info(allVehicles.get(0));

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlPath = path + "sql/";
        writer = IoUtil.getWriter(sqlPath + "insert_rel_"+dateStr+".sql");

        for(Map<String, String> data : dataList){
            handleData(data, allVehicles);
        }

        IoUtil.closeWriter(writer);
    }

    public List<Map<String, Object>> getAllVehicles(){
        String sql = "select id,brand_name,company_name,series_name,category_name from db_vehicle where vehicle_grade=3";
        return commonMapper.selectListBySql(sql);
    }

    public List<String> getVehicleIds(Map<String, String> data, List<Map<String, Object>> allVehicles){
        String brand = data.get("brand");
        String company = data.get("company");
        String series = data.get("series");
        String type = data.get("type");

        List<String> ids = new ArrayList<>();
        for(Map<String, Object> v : allVehicles){
            if(brand.equals(v.get("brand_name").toString())
                    && company.equals(v.get("company_name").toString())
                    && series.equals(v.get("series_name").toString())
                    && type.equals(v.get("category_name").toString())){

                ids.add(v.get("id").toString());
            }
        }

        return ids;
    }

    public void handleData(Map<String, String> data, List<Map<String, Object>> allVehicles){
        String modelId = data.get("model_id");
        String seriesId = data.get("series_id");
        if(StringUtils.isEmpty(modelId) && StringUtils.isEmpty(seriesId)){
            return;
        }

        List<String> vehicleIds = getVehicleIds(data, allVehicles);
        if(vehicleIds.isEmpty()){
            Print.info("查不到车型数据："+data);
            return;
        }

        if(StringUtils.isEmpty(modelId)){
            handleSql(vehicleIds, seriesId);
        }else{
            handleSql(vehicleIds, modelId);
        }
    }

    public void handleSql(List<String> vehicleIds, String obdId){
        for(String vid : vehicleIds){
            writeRelSql(vid, obdId);
        }
    }

}
