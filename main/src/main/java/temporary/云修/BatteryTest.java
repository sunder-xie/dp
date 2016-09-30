package temporary.云修;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * 云修电瓶处理
 * Created by huangzhangting on 16/7/4.
 */
public class BatteryTest extends BaseTest {
    private List<Map<String, Object>> goodsCarList;
    private Set<String> goodsCarKeySet;

    @Test
    public void handleBattery() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云修电瓶处理/";
        String excel = path + "电瓶数据适配20160628修改版.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("淘汽厂商", "company");
        attrMap.put("淘汽品牌", "brand");
        attrMap.put("淘汽车系", "series");
        attrMap.put("淘汽车型", "model");
        attrMap.put("年款", "year");
        attrMap.put("排量", "power");
        attrMap.put("燃料", "fuel");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        //电瓶数据
        Map<String, String> goodsAttrMap = new HashMap<>();
        goodsAttrMap.put("goods_id", "goodsId");
        goodsAttrMap.put("new_goods_sn", "goodsSn");
        goodsAttrMap.put("goods_name", "goodsName");

        CommReaderXLS readerXLS = new CommReaderXLS(goodsAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云修电瓶.xls", goodsAttrMap.size());
        Print.info(readerXLS.getDataList().size());
        Map<String, String> goodsMap = new HashMap<>();
        for(Map<String, String> goods : readerXLS.getDataList()){
            goodsMap.put(goods.get("goodsSn"), goods.get("goodsId"));
        }
        Print.info(goodsMap);

        //车型数据
        String sql = "select brand,brand_id,company,series,series_id,model,model_id," +
                "year,year_id,power,power_id,car_models as car_name,car_models_id as car_id" +
                " from db_car_all group by car_models_id";
        List<Map<String, Object>> carList = commonMapper.selectListBySql(sql);
        Print.info(carList.size());

        goodsCarList = new ArrayList<>();
        goodsCarKeySet = new HashSet<>();
        for(Map<String, String> data : dataList){
            handleData(data, carList);
        }
        Print.info(goodsCarList.size());

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "insert_goods_car_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGoodsCar(goodsCarList, goodsMap);

        IoUtil.closeWriter(writer);
    }

    public void handleData(Map<String, String> data, List<Map<String, Object>> carList){
        String goodsSn = data.get("goodsSn");
        String company = data.get("company");
        String brand = data.get("brand");

        if("".equals(goodsSn)){
            Print.info("商品编码为空！！！");
            Print.info(data);
            return;
        }
        if("".equals(company) || "".equals(brand)){
            Print.info("品牌、厂家为空！！！");
            Print.info(data);
            return;
        }

        List<Map<String, Object>> matchedCarList = getMatchedCars(data, carList);
        if(matchedCarList.isEmpty()){
            Print.info("没有匹配的车型");
            Print.info(data);
        }else{
            //Print.info(matchedCarList.get(0));
            goodsCarList.addAll(matchedCarList);
        }
    }

    //车型数据比较
    public List<Map<String, Object>> getMatchedCars(Map<String, String> data, List<Map<String, Object>> carList){
        List<Map<String, Object>> matchCarList = new ArrayList<>();
        String company = data.get("company");
        String brand = data.get("brand");
        String series = data.get("series");
        String model = data.get("model");
        String years = data.get("year");
        String power = data.get("power");
        String fuel = data.get("fuel");

        for(Map<String, Object> car : carList){
            if(brand.equals(car.get("brand").toString())
                    && company.equals(car.get("company").toString())
                    && compareSeries(series, car.get("series").toString())
                    && compareModel(model, car.get("model").toString())
                    && compareYear(years, car.get("year").toString())
                    && comparePower(power, car.get("power").toString())){

                if("".equals(fuel)){
                    Map<String, Object> gc = getGoodsCar(car);
                    gc.put("goodsSn", data.get("goodsSn"));
                    matchCarList.add(gc);
                }else{
                    if(car.get("car_name").toString().contains(fuel)){
                        Map<String, Object> gc = getGoodsCar(car);
                        gc.put("goodsSn", data.get("goodsSn"));
                        matchCarList.add(gc);
                    }
                }
            }
        }

        return matchCarList;
    }

    //比较车系
    public boolean compareSeries(String series, String carSeries){
        if("".equals(series)){
            return true;
        }
        return series.equals(carSeries);
    }

    //比较车型
    public boolean compareModel(String model, String carModel){
        if("".equals(model)){
            return true;
        }
        return model.equals(carModel);
    }

    //比较年款
    public boolean compareYear(String years, String carYear){
        if("".equals(years)){
            return true;
        }
        if(years.startsWith("-")){
            return carYear.compareTo(years.substring(1))<=0;
        }
        if(years.endsWith("-")){
            return carYear.compareTo(years.substring(0, years.length()-1))>=0;
        }
        int idx = years.indexOf("-");
        if(idx==-1){
            return years.equals(carYear);
        }
        String startYear = years.substring(0, idx);
        String endYear = years.substring(idx+1);

        return (carYear.compareTo(startYear)>=0 && carYear.compareTo(endYear)<=0);
    }

    //比较排量
    public boolean comparePower(String power, String carPower){
        if("".equals(power)){
            return true;
        }
        return carPower.contains(power);
    }


    //拷贝车型
    public Map<String, Object> getGoodsCar(Map<String, Object> car){
        Map<String, Object> gc = new HashMap<>();
        for(Map.Entry<String, Object> entry : car.entrySet()){
            gc.put(entry.getKey(), entry.getValue());
        }
        return gc;
    }

    public void handleGoodsCar(List<Map<String, Object>> goodsCarList, Map<String, String> goodsMap){
        List<Map<String, Object>> list = new ArrayList<>();

        for(Map<String, Object> gc : goodsCarList){
            String goodsId = goodsMap.get(gc.get("goodsSn").toString());
            if(goodsId==null){
                Print.info("错误的goods sn："+gc);
                continue;
            }
            String key = goodsId + "_" + gc.get("car_id");
            if(goodsCarKeySet.add(key) && checkFromDB(goodsId, gc.get("car_id").toString())){
                gc.put("goodsId", goodsId);
                list.add(gc);
            }
        }

        Print.info(list.size());
        handleSql(list);
    }

    public boolean checkFromDB(String goodsId, String carId){
        String sql = "select id from db_goods_car where goods_id="+goodsId+" and car_id="+carId;
        List<String> list = commonMapper.selectOneFieldBySql(sql);
        return list.isEmpty();
    }

    //处理sql
    public void handleSql(List<Map<String, Object>> goodsCarList){
        int count = 500;
        int size = goodsCarList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addGcVal(sb, goodsCarList.get(i));
            if((i+1)%count==0){
                writeGcSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIdx==i){
                writeGcSql(sb);
            }
            sb.append(",");
        }
    }

    public void addGcVal(StringBuilder sb, Map<String, Object> data){
        sb.append("(");
        sb.append(data.get("goodsId")).append(",");
        sb.append(data.get("car_id")).append(",'");
        sb.append(data.get("car_name")).append("',");
        sb.append(data.get("brand_id")).append(",'");
        sb.append(data.get("brand")).append("',");
        sb.append(data.get("series_id")).append(",'");
        sb.append(data.get("series")).append("',");
        sb.append(data.get("model_id")).append(",'");
        sb.append(data.get("model")).append("',");
        sb.append(data.get("power_id")).append(",'");
        sb.append(data.get("power")).append("',");
        sb.append(data.get("year_id")).append(",'");
        sb.append(data.get("year")).append("',1,@nowTime)");

    }
    public void writeGcSql(StringBuilder sb){
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("insert ignore into db_goods_car");
        sqlSb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sqlSb.append(" values ").append(sb).append(";\n");

        IoUtil.writeFile(writer, sqlSb.toString());
    }

}
