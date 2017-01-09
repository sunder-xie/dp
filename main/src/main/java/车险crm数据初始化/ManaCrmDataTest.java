package 车险crm数据初始化;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 17/1/9.
 */
public class ManaCrmDataTest extends BaseTest {

    /*
    * 买服务包送保险
    * 第一次支付（仅买服务包）
    * */
    private String getSql_1(){
        return "select t2.id,t2.insurance_company_id,t2.agent_type,t2.agent_id,t2.agent_name, " +
                "t2.vehicle_owner_name,t2.vehicle_owner_phone,t2.vehicle_owner_cert_type,t2.vehicle_owner_cert_code, " +
                "t2.vehicle_sn,t2.has_license,t2.car_config_type,t2.car_engine_sn,t2.car_frame_sn,t2.car_egister_date, " +
                "t2.insured_province,t2.insured_province_code,t2.insured_city,t2.insured_city_code, " +
                "t1.id as form_id,t1.gmt_create,t1.package_start_time,t1.package_end_time, " +
                "t1.cooperation_mode " +
                "from " +
                "(select * from insurance_virtual_form  " +
                "where is_deleted='N' and first_pay_id is not null and second_pay_id is null) t1, " +
                "insurance_virtual_basic t2 " +
                "where t1.insurance_virtual_basic_id=t2.id and t2.is_deleted='N' " +
                "order by t2.id desc,form_id desc";
    }
    // 第二次支付（买了保险）
    private String getSql_2(){
        return "select t2.id,t2.insurance_company_id,t2.agent_type,t2.agent_id,t2.agent_name, " +
                "t2.vehicle_owner_name,t2.vehicle_owner_phone,t2.vehicle_owner_cert_type,t2.vehicle_owner_cert_code, " +
                "t2.vehicle_sn,t2.has_license,t2.car_config_type,t2.car_engine_sn,t2.car_frame_sn,t2.car_egister_date, " +
                "t2.insured_province,t2.insured_province_code,t2.insured_city,t2.insured_city_code, " +
                "t1.id as form_id,t1.gmt_create,t1.package_start_time,t1.package_end_time, " +
                "t1.cooperation_mode,t1.virtualFormId " +
                "from " +
                "(select f.*,vf.id as virtualFormId " +
                "from " +
                "(select * from insurance_virtual_form  " +
                "where is_deleted='N' and second_pay_id is not null) vf, " +
                "insurance_form f " +
                "where f.outer_insurance_apply_no=vf.outer_insurance_apply_no) t1, " +
                "insurance_basic t2 " +
                "where t1.insurance_basic_id=t2.id and t2.is_deleted='N' " +
                "order by t2.id desc,form_id desc";
    }

    //其他模式，已缴费（需要过滤掉卖服务包送保险的表单）
    private String getSql_3(){
        return  "select t2.id,t2.insurance_company_id,t2.agent_type,t2.agent_id,t2.agent_name, " +
                "t2.vehicle_owner_name,t2.vehicle_owner_phone,t2.vehicle_owner_cert_type,t2.vehicle_owner_cert_code, " +
                "t2.vehicle_sn,t2.has_license,t2.car_config_type,t2.car_engine_sn,t2.car_frame_sn,t2.car_egister_date, " +
                "t2.insured_province,t2.insured_province_code,t2.insured_city,t2.insured_city_code, " +
                "t1.id as form_id,t1.gmt_create,t1.package_start_time,t1.package_end_time, " +
                "t1.cooperation_mode " +
                "from insurance_form t1,insurance_basic t2 " +
                "where t1.insurance_basic_id=t2.id and t2.is_deleted='N' and t1.is_deleted='N' " +
                "and t1.insure_status=3 " +
                "order by t2.id desc,form_id desc";
    }

    public List<String> getAllMobile(){
        String sql = "select distinct vehicle_owner_phone from insurance_basic where is_deleted='N' " +
                "union " +
                "select distinct vehicle_owner_phone from insurance_virtual_basic where is_deleted='N' ";

        return commonMapper.selectOneFieldBySql(sql);
    }
    public List<Map<String, Object>> getAllLicencePlate(){
        String sql = "select vehicle_owner_phone,vehicle_sn from insurance_basic where is_deleted='N' " +
                "group by vehicle_owner_phone,vehicle_sn " +
                "union " +
                "select vehicle_owner_phone,vehicle_sn from insurance_virtual_basic where is_deleted='N' " +
                "group by vehicle_owner_phone,vehicle_sn";

        return commonMapper.selectListBySql(sql);
    }


    @Test
    public void testData() throws Exception{
        //第一次支付（仅买服务包）
        List<Map<String, Object>> dataList_1 = commonMapper.selectListBySql(getSql_1());
        Print.printList(dataList_1);

        //第二次支付（买了保险）
        List<Map<String, Object>> dataList_2 = commonMapper.selectListBySql(getSql_2());
        Print.printList(dataList_2);

        //其他模式（需要过滤掉卖服务包送保险的表单）
        List<Map<String, Object>> dataList_3 = commonMapper.selectListBySql(getSql_3());
        Print.printList(dataList_3);

        grepDataList_3(dataList_3, dataList_2);
        Print.printList(dataList_3);


        //全部手机号
        List<String> allMobileList = getAllMobile();
        Print.printList(allMobileList);
        Map<String, Map<String, Object>> mobileMap = new HashMap<>();
        packageMobileMapForList(mobileMap, allMobileList, dataList_1);
        packageMobileMapForList(mobileMap, allMobileList, dataList_2);
        packageMobileMapForList(mobileMap, allMobileList, dataList_3);

        Print.info("手机号数量："+mobileMap.size());
        Map<String, Map<String, String>> customerMap = packageCustomer(mobileMap);


        //全部的车牌号
        List<Map<String, Object>> allLicencePlateList = getAllLicencePlate();
        Print.printList(allLicencePlateList);
        Map<String, Map<String, Object>> licencePlateMap = new HashMap<>();
        packageLicencePlateForList(licencePlateMap, allLicencePlateList, dataList_1, null);
        packageLicencePlateForList(licencePlateMap, allLicencePlateList, dataList_2, 1);
        packageLicencePlateForList(licencePlateMap, allLicencePlateList, dataList_3, 1);

        Print.info("手机-车牌数量："+licencePlateMap.size());
        List<Map<String, String>> vehicleList = packageVehicle(customerMap, licencePlateMap);
        Print.printList(vehicleList);


        /* 处理sql */
        path = "/Users/huangzhangting/Desktop/保险项目/历史数据/";
        handleCustomerSql(customerMap.values());
        handleVehicleSql(vehicleList);
    }

    private Set<String> getFormIdSet_2(List<Map<String, Object>> dataList_2){
        Set<String> set = new HashSet<>();
        for(Map<String, Object> map : dataList_2){
            String id = map.get("form_id").toString();
            set.add(id);
        }
        return set;
    }

    private void grepDataList_3(List<Map<String, Object>> dataList_3, List<Map<String, Object>> dataList_2){
        Set<String> formIdSet_2 = getFormIdSet_2(dataList_2);
        if(formIdSet_2.isEmpty()){
            return;
        }
        int size = dataList_3.size();
        for(int i=0; i<size; i++){
            String id = dataList_3.get(i).get("form_id").toString();
            if(formIdSet_2.contains(id)){
                dataList_3.remove(i);
                i--;
                size--;
            }
        }
    }


    /* 组装手机数据 */
    private void packageMobileMapForList(Map<String, Map<String, Object>> mobileMap, List<String> mobileList,
                                         List<Map<String, Object>> dataList){
        for(String mobile : mobileList){
            packageMobileMap(mobileMap, mobile, dataList);
        }
    }
    private void packageMobileMap(Map<String, Map<String, Object>> mobileMap, String mobile,
                                  List<Map<String, Object>> dataList){
        for(Map<String, Object> data : dataList){
            String vehicle_owner_phone = data.get("vehicle_owner_phone").toString();
            if(mobile.equals(vehicle_owner_phone)){
                Map<String, Object> map = mobileMap.get(mobile);
                if(map==null){
                    mobileMap.put(mobile, data);
                }else {
                    String gmt_create = data.get("gmt_create").toString();
                    String oldTime = map.get("gmt_create").toString();
                    if(gmt_create.compareTo(oldTime) > 0){
                        mobileMap.put(mobile, data);
                    }
                }
            }
        }
    }


    /* 组装车牌数据 */
    private void packageLicencePlateForList(Map<String, Map<String, Object>> licencePlateMap, List<Map<String, Object>> licencePlateList,
                                     List<Map<String, Object>> dataList, Integer hasInsured){
        for(Map<String, Object> licencePlate : licencePlateList){
            packageLicencePlate(licencePlateMap, licencePlate, dataList, hasInsured);
        }
    }
    private void packageLicencePlate(Map<String, Map<String, Object>> licencePlateMap, Map<String, Object> licencePlate,
                                     List<Map<String, Object>> dataList, Integer hasInsured){

        for(Map<String, Object> data : dataList){
            data.put("hasInsured", hasInsured); //是否投保

            String vehicle_owner_phone = data.get("vehicle_owner_phone").toString();
            String vehicle_sn = data.get("vehicle_sn").toString();

            if(vehicle_owner_phone.equals(licencePlate.get("vehicle_owner_phone").toString())
                    && vehicle_sn.equals(licencePlate.get("vehicle_sn").toString())){

                String key = vehicle_owner_phone + "_" + vehicle_sn;
                Map<String, Object> map = licencePlateMap.get(key);
                if(map==null){
                    licencePlateMap.put(key, data);
                }else {
                    String gmt_create = data.get("gmt_create").toString();
                    String oldTime = map.get("gmt_create").toString();
                    if(gmt_create.compareTo(oldTime) > 0){
                        licencePlateMap.put(key, data);
                    }
                }
            }
        }
    }


    /* 组装客户 */
    private Map<String, Map<String, String>> packageCustomer(Map<String, Map<String, Object>> mobileMap){
        Map<String, Map<String, String>> customerMap = new HashMap<>();
        int id = 1;
        for(Map.Entry<String, Map<String, Object>> entry : mobileMap.entrySet()){
            Map<String, Object> data = entry.getValue();
            Map<String, String> map = new HashMap<>();
            map.put("id", id+"");
            map.put("customer_mobile", data.get("vehicle_owner_phone").toString());
            map.put("customer_name", data.get("vehicle_owner_name").toString());
            map.put("certificate_type", data.get("vehicle_owner_cert_type").toString());
            map.put("certificate_no", data.get("vehicle_owner_cert_code").toString());
            customerMap.put(entry.getKey(), map);
            id++;
        }

        return customerMap;
    }

    /* 组装车辆 */
    private List<Map<String, String>> packageVehicle(Map<String, Map<String, String>> customerMap,
                                                     Map<String, Map<String, Object>> licencePlateMap){

        List<Map<String, String>> vehicleList = new ArrayList<>();
        int id = 1;
        for(Map.Entry<String, Map<String, Object>> entry : licencePlateMap.entrySet()){
            String mobile = entry.getKey().substring(0, entry.getKey().indexOf("_"));
            Map<String, String> customer = customerMap.get(mobile);
            if(customer != null){
                Map<String, Object> data = entry.getValue();
                Map<String, String> map = new HashMap<>();
                map.put("id", id+"");
                map.put("customer_id", customer.get("id"));

                map.put("licence_plate", data.get("vehicle_sn").toString());
                map.put("has_licence_plate", data.get("has_license").toString());
                map.put("insure_province", data.get("insured_province").toString());
                map.put("insure_province_code", data.get("insured_province_code").toString());
                map.put("insure_city", data.get("insured_city").toString());
                map.put("insure_city_code", data.get("insured_city_code").toString());
                map.put("insure_vehicle_type", data.get("car_config_type").toString());
                map.put("engine_no", data.get("car_engine_sn").toString());
                map.put("vin_no", data.get("car_frame_sn").toString());
                map.put("vehicle_reg_date", data.get("car_egister_date").toString());
                map.put("cooperation_mode", data.get("cooperation_mode").toString());
                map.put("agent_type", data.get("agent_type")==null?"0":data.get("agent_type").toString());
                map.put("agent_id", data.get("agent_id").toString());
                map.put("agent_name", data.get("agent_name").toString());

                Object hasInsured = data.get("hasInsured");
                if(hasInsured != null){
                    map.put("insurance_form_id", data.get("form_id").toString());
                    map.put("insure_status", "1");
                    map.put("quit_insure_status", "1");
                    map.put("insure_intention", "4");
                    map.put("insure_start_date", data.get("package_start_time").toString());
                    map.put("insure_end_date", data.get("package_end_time").toString());
                    map.put("insure_company_id", data.get("insurance_company_id").toString());

                    map.put("hasInsured", hasInsured.toString());
                }

                vehicleList.add(map);
                id++;
            }
        }

        return vehicleList;
    }


    private void handleCustomerSql(Collection<Map<String, String>> customers){
        writer = IoUtil.getWriter(path + "add_customer.sql");
        for(Map<String, String> map : customers){
            StringBuilder sql = new StringBuilder();
            sql.append("insert into mana_customer(gmt_create,creator,has_sync,customer_source,id,customer_mobile,customer_name,certificate_type,certificate_no)");
            sql.append("value(now(),'system',1,1,");
            sql.append(map.get("id")).append(",'");
            sql.append(map.get("customer_mobile")).append("','");
            sql.append(map.get("customer_name")).append("','");
            sql.append(map.get("certificate_type")).append("','");
            sql.append(map.get("certificate_no")).append("')");
            sql.append(";\n");

            IoUtil.writeFile(writer, sql.toString());
        }
    }

    private void handleVehicleSql(List<Map<String, String>> vehicleList){
        writer = IoUtil.getWriter(path + "add_vehicle.sql");
        for(Map<String, String> map : vehicleList){
            StringBuilder sql = new StringBuilder();
            sql.append("insert into mana_customer_vehicle(gmt_create,creator,has_sync,id,customer_id,");
            sql.append("licence_plate,has_licence_plate,insure_province,insure_province_code,insure_city,insure_city_code,");
            sql.append("insure_vehicle_type,engine_no,vin_no,vehicle_reg_date,");
            sql.append("cooperation_mode,agent_type,agent_id,agent_name");

            if(map.get("hasInsured") != null){
                sql.append(",insurance_form_id,insure_status,quit_insure_status,insure_intention");
                sql.append(",insure_start_date,insure_end_date,insure_company_id");
            }
            sql.append(") value(now(),'system',1,");
            sql.append(map.get("id")).append(",");
            sql.append(map.get("customer_id")).append(",'");
            sql.append(map.get("licence_plate")).append("',");
            sql.append(map.get("has_licence_plate")).append(",'");
            sql.append(map.get("insure_province")).append("','");
            sql.append(map.get("insure_province_code")).append("','");
            sql.append(map.get("insure_city")).append("','");
            sql.append(map.get("insure_city_code")).append("','");
            sql.append(map.get("insure_vehicle_type")).append("','");
            sql.append(map.get("engine_no")).append("','");
            sql.append(map.get("vin_no")).append("','");
            sql.append(map.get("vehicle_reg_date")).append("',");
            sql.append(map.get("cooperation_mode")).append(",");
            sql.append(map.get("agent_type")).append(",");
            sql.append(map.get("agent_id")).append(",'");
            sql.append(map.get("agent_name")).append("'");

            if(map.get("hasInsured") != null){
                sql.append(",").append(map.get("insurance_form_id"));
                sql.append(",").append(map.get("insure_status"));
                sql.append(",").append(map.get("quit_insure_status"));
                sql.append(",").append(map.get("insure_intention"));
                sql.append(",'").append(map.get("insure_start_date"));
                sql.append("','").append(map.get("insure_end_date"));
                sql.append("',").append(map.get("insure_company_id"));
            }
            sql.append(");\n");

            IoUtil.writeFile(writer, sql.toString());
        }
    }

}
