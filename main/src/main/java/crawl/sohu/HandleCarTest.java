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

        for(Map<String, String> shCar : shCarList){
            compareCar(shCar, carList);
        }

        IoUtil.closeWriter(writer);
    }

    public void init(){
        brandMap = initBrandMap();
        spBrandSet = initSpBrandSet();

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

    public void compareCar(Map<String, String> shCar, List<Map<String, Object>> carList){
        String shBrand = handleBrand(StrUtil.toUpCase(shCar.get("brand")));
        if(spBrandSet.contains(shBrand)){
            compareCarSp(shCar, carList);
            return;
        }
        String name = shCar.get("name");

        Set<String> matchCarIds = new HashSet<>();
        for(Map<String, Object> car : carList){
            if(shBrand.equals(car.get("brand").toString()) && compareModel(shCar, car)){
                if(shCar.get("year").equals(car.get("year").toString())
                        && comparePower(car, name)){

                    matchCarIds.add(car.get("id").toString());
                }
            }
        }
        if(matchCarIds.isEmpty()){
            //Print.info("没有匹配上的："+shCar);
        }else{
            //Print.info(shCar.get("id") + "  " + matchCarIds);
            writeSql(shCar.get("id"), matchCarIds);
        }
    }

    private boolean comparePower(Map<String, Object> car, String saleName){
        String power = car.get("power").toString().replace("T", "").replace("L", "");
        if(saleName.contains(power)){
            return !(saleName.contains("柴油") ^ car.get("name").toString().contains("柴油"));
        }
        return false;
    }

    public boolean compareModel(Map<String, String> shCar, Map<String, Object> car){
        String importInfo = car.get("import_info").toString();
        String shCompany = shCar.get("company");
        if("进口".equals(importInfo) ^ shCompany.contains("进口")){
            return false;
        }

        String shModel = StrUtil.toUpCase(shCar.get("model"));
        String model = StrUtil.toUpCase(car.get("model").toString());
        if(shModel.equals(model)){
            return true;
        }

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

    //处理奔驰、宝马
    public void compareCarSp(Map<String, String> shCar, List<Map<String, Object>> carList){
        String shBrand = shCar.get("brand");
        String shCompany = shCar.get("company");
        String name = shCar.get("name");

        Set<String> matchCarIds = new HashSet<>();
        for(Map<String, Object> car : carList){
            if(shBrand.equals(car.get("brand").toString()) && compareModelSp(shCar, car)){
                if(shCar.get("year").equals(car.get("year")) &&
                        comparePower(car, name)){

                    matchCarIds.add(car.get("id").toString());
                }
            }
        }
        if(matchCarIds.isEmpty()){
            //Print.info("sp没有匹配上的："+shCar);
        }else{
            //Print.info(shCar.get("id") + "  " + matchCarIds);
            writeSql(shCar.get("id"), matchCarIds);
        }
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


    //临时处理
    public void compareBrand(List<Map<String, String>> shCarList, List<Map<String, Object>> carList){
        Set<String> set = new HashSet<>();
        for(Map<String, String> shCar : shCarList){
            String shBrand = handleBrand(StrUtil.toUpCase(shCar.get("brand")));
            boolean flag = true;
            for(Map<String, Object> car : carList){
                if(shBrand.equals(StrUtil.toUpCase(car.get("brand").toString()))){
                    flag = false;
                    break;
                }
            }
            if(flag){
                set.add(shBrand);
            }
        }

        Print.info(set);
    }


    //处理需要删除的对应关系
    @Test
    public void testDel() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/搜狐汽车/";
        String excel = path + "需要删除的关系.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.id", "sh_car_id");
        attrMap.put("t3.id", "tq_car_id");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        Print.info(readerXLSX.getDataList().size());

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path+"delCarRelation-"+dateStr+".sql");

        StringBuilder sb = new StringBuilder();
        for(Map<String, String> data : readerXLSX.getDataList()){
            sb.append("delete from sohu_car_relation where sh_car_id=");
            sb.append(data.get("sh_car_id"));
            sb.append(" and tq_car_id=").append(data.get("tq_car_id"));
            sb.append(";\n");

            IoUtil.writeFile(writer, sb.toString());
            sb.setLength(0);
        }

        IoUtil.closeWriter(writer);
    }

}
