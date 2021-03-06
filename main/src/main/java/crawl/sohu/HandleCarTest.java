package crawl.sohu;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/5/31.
 */
public class HandleCarTest extends BaseTest {
    private Map<String, String> brandMap;
    private Set<String> spBrandSet;
    private List<Map<String, Object>> unMatchCarList;
    private List<Map<String, Object>> matchCarList;


    public List<Map<String, Object>> getCarList(){
        String sql = "select c.brand,c.company,c.import_info,c.series,c.model,c.power,c.year,c.name,c.id " +
                "from " +
                "(select * from db_car_category where level=6) c " +
                "left join " +
                "(select distinct model_id from db_model_maintain_relation) t1 " +
                "on c.id=t1.model_id " +
                "where t1.model_id is null";
        
        return commonMapper.selectListBySql(sql);
    }

    public List<Map<String, Object>> getAllCarList(){
        String sql = "select brand,company,import_info,series,model,power,year,name,id " +
                "from db_car_category where level=6";

        return commonMapper.selectListBySql(sql);
    }

    //匹配车型
    @Test
    public void carTest() throws Exception{
        List<Map<String, Object>> carList = getAllCarList();
        Print.info(carList.size());
        if(carList.isEmpty()){
            return;
        }

        path = "/Users/huangzhangting/Desktop/数据抓取/搜狐/";
        String excel = path + "搜狐车型数据.xls";

        Map<String,String> attrMap = new HashMap<>();
        attrMap.put("t1.car_brand", "brand");
        attrMap.put("t1.company", "company");
        attrMap.put("t1.car_model", "model");
        attrMap.put("t2.car_year", "year");
        attrMap.put("t2.id", "id");
        attrMap.put("t2.sale_name", "name");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, 6);
        List<Map<String, String>> shCarList = readerXLS.getDataList();
        Print.printList(shCarList);
        if(shCarList.isEmpty()){
            return;
        }

        init();

        for(Map<String, Object> car : carList){
            compareCar(car, shCarList, 1);
        }
        Print.info("匹配上的车型："+matchCarList.size());
        Print.info("第一次没匹配上的车型："+unMatchCarList.size());

        List<Map<String, Object>> unMatchList = new ArrayList<>(unMatchCarList);
        unMatchCarList = new ArrayList<>();
        for(Map<String, Object> umCar : unMatchList){
            compareCar(umCar, shCarList, 2);
        }
        Print.info("匹配上的车型："+matchCarList.size());


        IoUtil.closeWriter(writer);


        //todo 临时处理，和人工处理的数据作比较
        //compare_his_data();
    }

    public void init(){
        brandMap = initBrandMap();
        spBrandSet = initSpBrandSet();
        unMatchCarList = new ArrayList<>();
        matchCarList = new ArrayList<>();

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = path + "addCarRelation-"+dateStr+".sql";
        writer = IoUtil.getWriter(sqlFile);
        IoUtil.writeFile(writer, "truncate table sohu_car_relation;\n");
    }

    public Map<String, String> initBrandMap(){
        Map<String, String> map = new HashMap<>();
        map.put("北汽幻速", "幻速");
        map.put("北汽绅宝", "绅宝");
        map.put("北汽威旺", "威旺");

        map.put("东风风神", "风神");
        map.put("东风风行", "风行");

        map.put("广汽传祺", "传祺");

        return map;
    }

    public Set<String> initSpBrandSet(){
        Set<String> set = new HashSet<>();
        set.add("奔驰");
        set.add("宝马");

        return set;
    }

    public void compareCar(Map<String, Object> car, List<Map<String, String>> shCarList, int compareTime){
        String brand = car.get("brand").toString();
        String year = car.get("year").toString();
        String name = car.get("name").toString();
        String shCarId = null;
        Set<String> shCarIdSet = new HashSet<>();
        for(Map<String, String> shCar : shCarList){
            String shBrand = handleBrand(StrUtil.toUpCase(shCar.get("brand")));
            String shName = shCar.get("name");
            if(brand.equals(shBrand) && comparePower(car, shName)
                    && year.equals(shCar.get("year")) && compareTransmissionType(name, shName)){
                if(spBrandSet.contains(shBrand)){
                    if(compareModelSp(shCar, car)){
//                        shCarId = shCar.get("id");
//                        break;

                        shCarIdSet.add(shCar.get("id"));
                    }
                }else{
                    if(compareModel(shCar, car, compareTime)){
//                        shCarId = shCar.get("id");
//                        break;

                        shCarIdSet.add(shCar.get("id"));
                    }
                }
            }
        }

        //如果没有匹配上
        if(shCarIdSet.isEmpty()){
            unMatchCarList.add(car);
        }else{
            for(String id : shCarIdSet){
                Map<String, Object> map = ObjectUtil.copyMap(car);
                map.put("shCarId", id);
                matchCarList.add(map);
            }
        }
    }

    public boolean compareModel(Map<String, String> shCar, Map<String, Object> car, int compareTime){
        String importInfo = car.get("import_info").toString();
        String shCompany = shCar.get("company");
        if("进口".equals(importInfo) ^ shCompany.contains("进口")){
            return false;
        }
        String shModel = StrUtil.toUpCase(shCar.get("model"));
        String model = StrUtil.toUpCase(car.get("model").toString());

        if(1==compareTime) {
            if (shModel.equals(model)) {
                return true;
            }
            shModel = shModel.replace("双门", "COUPE").replace("掀背", "SPORTBACK");
            return shModel.equals(model);
        }else{
            if(shModel.contains(model) || model.contains(shModel)){
                return true;
            }
            model = StrUtil.rep(model);
            if(shModel.contains(model) || model.contains(shModel)){
                return true;
            }
            shModel = shModel.replace("双门", "COUPE").replace("掀背", "SPORTBACK");
            return shModel.contains(model) || model.contains(shModel);
        }
    }

    public boolean compareModelSp(Map<String, String> shCar, Map<String, Object> car){
        String importInfo = car.get("import_info").toString();
        String shCompany = shCar.get("company");
        if("进口".equals(importInfo) ^ shCompany.contains("进口")){
            return false;
        }

        String shSeries = StrUtil.toUpCase(shCar.get("model"));
        String shModel = StrUtil.repBrackets(StrUtil.repCN(shCar.get("name")));
        shModel = StrUtil.toUpCase(shModel);

        String series = StrUtil.toUpCase(car.get("series").toString());
        String model = StrUtil.toUpCase(car.get("model").toString());

        if(shSeries.contains(series) || series.contains(shSeries)){
            return shModel.contains(model) || model.contains(shModel);
        }

        return false;
    }

    private boolean comparePower(Map<String, Object> car, String saleName){
        String power = car.get("power").toString().replace("T", "").replace("L", "");
        if(saleName.contains(power)){
            return !(saleName.contains("柴油") ^ car.get("name").toString().contains("柴油"));
        }
        return false;
    }

    public boolean compareTransmissionType(String tqCarName, String shCarName){
        String tqName = StrUtil.toUpCase(tqCarName);
        String shName = StrUtil.toUpCase(shCarName);

        if(shName.contains("DSG")){
            return tqName.contains("双离合");
        }
        if(shName.contains("CVT")){
            return tqName.contains("无级");
        }
        if(shName.contains("手自一体") || shName.contains("自动") || shName.contains("AT")){
            return tqName.contains("自动");
        }
        if(shName.contains("手动") || shName.contains("MT")){
            return tqName.contains("手动");
        }

        return true;
    }

    public String handleBrand(String shBrand){
        String brand = brandMap.get(shBrand);
        if(brand!=null){
            return brand;
        }
        return shBrand;
    }

    public void writeSql(String shId, Set<String> idSet){
        StringBuilder sb = new StringBuilder();
        for(String id : idSet){
            sb.append("insert ignore into sohu_car_relation(sh_car_id,tq_car_id) value (");
            sb.append(shId).append(",");
            sb.append(id).append(");\n");
            IoUtil.writeFile(writer, sb.toString());
            sb.setLength(0);
        }
    }



    public void compare_his_data() throws Exception{
        String excel = path + "搜狐-淘汽车型匹配验证-20161128.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("tq_car", "tq_car");
        attrMap.put("sh_car", "sh_car");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        writer = IoUtil.getWriter(path + "add_car_rel_1.sql");
        List<Map<String, String>> unMatchList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            if(!check_exist(data)){
                unMatchList.add(data);

                writeRelSql(data.get("tq_car"), data.get("sh_car"));
            }
        }
        Print.info("人工匹配多余的数据："+unMatchList.size());


        writer = IoUtil.getWriter(path + "add_car_rel_2.sql");
        List<Map<String, Object>> unMatchCarList = new ArrayList<>();
        for(Map<String, Object> car : matchCarList){
            if(!check_car(car, dataList)){
                unMatchCarList.add(car);

                writeRelSql(car.get("id").toString(), car.get("shCarId").toString());
            }
        }
        Print.info("程序匹配多余的数据："+unMatchCarList.size());


    }
    private boolean check_exist(Map<String, String> data){
        for(Map<String, Object> map : matchCarList){
            String tqId = map.get("id").toString();
            String shId = map.get("shCarId").toString();
            if(tqId.equals(data.get("tq_car")) && shId.equals(data.get("sh_car"))){
                return true;
            }
        }
        return false;
    }
    private boolean check_car(Map<String, Object> car, List<Map<String, String>> list){
        String tqId = car.get("id").toString();
        String shId = car.get("shCarId").toString();
        for(Map<String, String> data : list){
            if(tqId.equals(data.get("tq_car")) && shId.equals(data.get("sh_car"))){
                return true;
            }
        }
        return false;
    }
    private void writeRelSql(String tqCarId, String shCarId){
        StringBuilder sql = new StringBuilder();
        sql.append("insert ignore into sohu_car_relation(tq_car_id, sh_car_id) value(");
        sql.append(tqCarId);
        sql.append(",");
        sql.append(shCarId);
        sql.append(");\n");
        IoUtil.writeFile(writer, sql.toString());
    }

}
