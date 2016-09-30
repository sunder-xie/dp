package vehicle;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.io.Writer;
import java.util.*;

/**
 * Created by huangzhangting on 16/5/23.
 */
public class VehicleTest extends BaseTest{
    private Writer writer;
    private String path;


    public List<Map<String, Object>> getVehicleList(){
        String sql = "select id,pid,vehicle_name,category_name,notice_number,fuel_type from db_vehicle where grade=3";
        return commonMapper.selectListBySql(sql);
    }

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Downloads/";
        String excel = path + "卡车车型第二期爬取数据.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("类型", "车辆类型");
        attrMap.put("总质量", "总质量");
        attrMap.put("整备质量", "整备质量");
        attrMap.put("公告型号", "公告型号");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info("记录数："+dataList.size());
        if(dataList.isEmpty()){
            return;
        }
        List<Map<String, Object>> vehicleList = getVehicleList();
        Print.info(vehicleList.size());

        init();

        for(Map<String, Object> vehicle : vehicleList){
            handleDataList(dataList, vehicle);
        }
    }

    public void init(){
        String sqlFile = path + "updateRTU.sql";
        writer = IoUtil.getWriter(sqlFile);
    }

    public void handleDataList(Collection<Map<String, String>> dataList, Map<String, Object> vehicle){
        String ntNo = vehicle.get("notice_number").toString();
        for(Map<String, String> data : dataList){
            if(ntNo.equals(data.get("公告型号"))){
                String totalWeight = data.get("总质量");
                String weight = data.get("整备质量");
                if(totalWeight==null || weight==null){
                    continue;
                }
                totalWeight = totalWeight.replace("，", ",").replace("/", ",").replace("'","").replace("kg", "").replace(" ", "");
                weight = weight.replace("，", ",").replace("/", ",").replace("'","").replace("kg", "").replace(" ", "");
                if("".equals(totalWeight) || "".equals(weight)){
                    continue;
                }

                handleRTU(vehicle, totalWeight, weight);
                break;
            }
        }
    }

    public void handleRTU(Map<String, Object> vehicle, String totalWeight, String weight){
        String[] tws = totalWeight.split(",");
        String[] ws = weight.split(",");
        StringBuilder sb = new StringBuilder();

        try {

            int zaiZhong = Integer.parseInt(tws[0]) - Integer.parseInt(ws[0]);

            String categoryName = vehicle.get("category_name").toString();
            if(categoryName.contains("校车") || categoryName.contains("客车")){
                sb.append("update db_vehicle set total_weight='").append(tws[0]);
                sb.append("', curb_weight='").append(ws[0]);
                sb.append("', carrying_capacity='").append(zaiZhong);
                sb.append("' where id=").append(vehicle.get("id"));
                sb.append(";\n");
                IoUtil.writeFile(writer, sb.toString());

                return;
            }

            sb.append("update db_vehicle set rtu='");
            sb.append(getRTU(vehicle.get("fuel_type").toString(), zaiZhong));
            sb.append("', total_weight='").append(tws[0]);
            sb.append("', curb_weight='").append(ws[0]);
            sb.append("', carrying_capacity='").append(zaiZhong);
            sb.append("' where id=").append(vehicle.get("id"));
            sb.append(";\n");
            IoUtil.writeFile(writer, sb.toString());

        }catch (Exception e){
            e.printStackTrace();
            Print.info("总质量:"+totalWeight+"  整备质量:"+weight);
        }
    }

    public String getRTU(String fuelType, int zaiZhong){
        if(zaiZhong<=750){
            return "P";
        }
        if(zaiZhong<=3500){
            if("汽油".equals(fuelType)){
                return "Q";
            }else if("柴油".equals(fuelType)){
                return "R";
            }else{
                return "";
            }
        }
        if(zaiZhong<8000){
            if("汽油".equals(fuelType)){
                return "S";
            }else if("柴油".equals(fuelType)){
                return "T";
            }else{
                return "";
            }
        }

        return "U";
    }


    @Test
    public void test2() throws Exception{
        path = "/Users/huangzhangting/Documents/vehicle-data-process/";
        String excel = path + "dataFile/属性补充-0530.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("tb1.id", "id");
        attrMap.put("ak.属性", "属性");
        attrMap.put("ca.属性值", "属性值");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, 6);
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());

        Map<String, Map<String, String>> idMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String id = data.get("id");
            Map<String, String> map = idMap.get(id);
            if(map==null){
                map = new HashMap<>();
                idMap.put(id, map);
            }
            if("整车公告".equals(data.get("属性"))){
                map.put("公告型号", data.get("属性值"));
            }
            if("整备质量".equals(data.get("属性"))){
                map.put("整备质量", data.get("属性值"));
            }
            if("总质量".equals(data.get("属性"))){
                map.put("总质量", data.get("属性值"));
            }
        }

        init();

        String sql = "select id,pid,vehicle_name,category_name,notice_number,fuel_type from db_vehicle where grade=3 and total_weight=''";
        List<Map<String, Object>> vehicleList = commonMapper.selectListBySql(sql);
        for(Map<String, Object> vehicle : vehicleList){
            handleDataList(idMap.values(), vehicle);
        }
    }

}
