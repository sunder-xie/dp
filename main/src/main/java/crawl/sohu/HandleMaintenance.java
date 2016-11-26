package crawl.sohu;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/6/1.
 */
public class HandleMaintenance extends BaseTest {
    private List<Map<String, String>> maintainRelationList;
    private StringBuilder insertSqlSb;

    @Test
    public void test() throws Exception{
        List<Map<String, Object>> carRelation =
                commonMapper.selectListBySql("select sh_car_id, tq_car_id from sohu_car_relation");

        Print.info(carRelation.size());

        Map<String, Set<String>> carIdMap = new HashMap<>();
        for(Map<String, Object> relation : carRelation){
            String carId = relation.get("tq_car_id").toString();
            Set<String> set = carIdMap.get(carId);
            if(set==null){
                set = new HashSet<>();
                carIdMap.put(carId, set);
            }
            set.add(relation.get("sh_car_id").toString());
        }

        Print.info(carIdMap.size());

        init();

        for(Map.Entry<String, Set<String>> entry : carIdMap.entrySet()){
            handleMaintain(entry.getKey(), grepShCarId(entry.getValue()));
        }

        //生成sql
        handleSql();

        IoUtil.closeWriter(writer);
    }

    public void init(){
        maintainRelationList = new ArrayList<>();

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/搜狐汽车/";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = path + "addMaintenance_"+dateStr+".sql";
        writer = IoUtil.getWriter(sqlFile);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        insertSqlSb = new StringBuilder();
        insertSqlSb.append("insert into db_model_maintain_relation(gmt_create,service_type,");
        insertSqlSb.append("model_id,car_brand_id,car_series_id,car_model_id,car_power_id,car_year_id,");
        insertSqlSb.append("mile,service_id) values");

    }

    public String grepShCarId(Set<String> shCarIds){
        String ids = shCarIds.toString().replace("[","").replace("]","");
        String sql = "select car_id from sohu_car_maintenance where car_id in("+ids+") group by car_id " +
                "order by count(id) desc limit 1";

        List<String> list = commonMapper.selectOneFieldBySql(sql);
        if(list.isEmpty()){
            return null;
        }

        return list.get(0);
    }

    public List<Map<String, Object>> getMaintenance(String shCarId){
        String sql = "select mileage,engine_oil,oil_filter,gasoline_filter,air_filter,air_conditioning_filter,spark_plug"
                + " from sohu_car_maintenance where car_id="+shCarId;
        return commonMapper.selectListBySql(sql);
    }

    public Map<String, Object> getCarInfo(String carId){
        String sql = "select brand_id,series_id,model_id,power_id,year_id,car_models_id from db_car_all where car_models_id="
                +carId+" limit 1";
        List<Map<String, Object>> list = commonMapper.selectListBySql(sql);
        if(list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    public void handleMaintain(String carId, String shCarId){
        if(shCarId==null){
            return;
        }
        Map<String, Object> carInfo = getCarInfo(carId);
        if(carInfo==null){
            Print.info("存在没有对应关系的车款id："+carId);
            return;
        }
        List<Map<String, Object>> maintenanceList = getMaintenance(shCarId);
        for(Map<String, Object> map : maintenanceList){
            String mileage = map.get("mileage").toString();
            //机油
            if(Integer.parseInt(map.get("engine_oil").toString())==1){
                addMaintainRelation(carInfo, mileage, 1);
            }
            //机油滤清器
            if(Integer.parseInt(map.get("oil_filter").toString())==1){
                addMaintainRelation(carInfo, mileage, 2);
            }
            //空气滤清器
            if(Integer.parseInt(map.get("air_filter").toString())==1){
                addMaintainRelation(carInfo, mileage, 3);
            }
            //空调滤清器
            if(Integer.parseInt(map.get("air_conditioning_filter").toString())==1){
                addMaintainRelation(carInfo, mileage, 4);
            }
            //燃油滤清器
            if(Integer.parseInt(map.get("gasoline_filter").toString())==1){
                addMaintainRelation(carInfo, mileage, 5);
            }
            //火花塞
            if(Integer.parseInt(map.get("spark_plug").toString())==1){
                addMaintainRelation(carInfo, mileage, 6);
            }
        }
    }

    public void addMaintainRelation(Map<String, Object> carInfo, String mileage, int itemId){
        Map<String, String> data = new HashMap<>();
        data.put("model_id", carInfo.get("car_models_id").toString());
        data.put("car_brand_id", carInfo.get("brand_id").toString());
        data.put("car_series_id", carInfo.get("series_id").toString());
        data.put("car_model_id", carInfo.get("model_id").toString());
        data.put("car_power_id", carInfo.get("power_id").toString());
        data.put("car_year_id", carInfo.get("year_id").toString());
        data.put("mile", mileage);
        data.put("service_id", itemId+"");

        maintainRelationList.add(data);
    }

    public void handleSql(){
        Print.info(maintainRelationList.size());

        int count = 1000;
        int size = maintainRelationList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            appendValue(sb, maintainRelationList.get(i));
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

    public void appendValue(StringBuilder sb, Map<String, String> data){
        sb.append("(@nowTime,1,");
        sb.append(data.get("model_id")).append(",");
        sb.append(data.get("car_brand_id")).append(",");
        sb.append(data.get("car_series_id")).append(",");
        sb.append(data.get("car_model_id")).append(",");
        sb.append(data.get("car_power_id")).append(",");
        sb.append(data.get("car_year_id")).append(",");
        sb.append(data.get("mile")).append(",");
        sb.append(data.get("service_id"));
        sb.append(")");
    }

    public void writeSql(StringBuilder sb){
        sb.insert(0, insertSqlSb);
        sb.append(";\n");
        IoUtil.writeFile(writer, sb.toString());
    }

}
