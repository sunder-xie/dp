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
 * 云修雨刷处理
 * Created by huangzhangting on 16/6/27.
 */
public class WiperTest extends BaseTest{
    Set<String> goodsCarKeySet;

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云修雨刷处理/";
        String excel = path + "雨刮车型匹配表-0620更新-20160629.xlsx";

        //关系数据
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("雨刷接口", "interface");
        attrMap.put("尺寸主驾/副驾", "size");
        attrMap.put("淘气厂商", "company");
        attrMap.put("淘气品牌", "brand");
        attrMap.put("淘气车系", "series");
        attrMap.put("淘气车型", "model");
        attrMap.put("年款", "year");
        attrMap.put("排量", "power");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        CommReaderXLS reader = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        reader.process(path + "（雨刷）有问题的车型(2).xls", attrMap.size());
        Print.info(reader.getDataList().get(0));
        dataList.addAll(reader.getDataList());
        Print.info(dataList.size());

        //雨刷数据
        Map<String, String> goodsAttrMap = new HashMap<>();
        goodsAttrMap.put("goods_id", "goodsId");
        goodsAttrMap.put("名称", "goodsName");

        CommReaderXLS readerXLS = new CommReaderXLS(goodsAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云修雨刮商品.xls", 2);
        Print.info(readerXLS.getDataList());

        //车型数据
        String sql = "select brand,brand_id,company,series,series_id,model,model_id," +
                "year,year_id,power,power_id,car_models as car_name,car_models_id as car_id" +
                " from db_car_all group by car_models_id";
        List<Map<String, Object>> carList = commonMapper.selectListBySql(sql);
        Print.info(carList.size());

        goodsCarKeySet = new HashSet<>();
        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            handleData(data, readerXLS.getDataList(), carList, goodsCarList);
        }

        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = path+"insert_goods_car_"+dateStr+".sql";
        writer = IoUtil.getWriter(sqlFile);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleSql(goodsCarList);

        IoUtil.closeWriter(writer);
    }

    public void handleData(Map<String, String> data, List<Map<String, String>> goodsList,
                           List<Map<String, Object>> carList, List<Map<String, Object>> goodsCarList){
        String size = data.get("size");
        if ("".equals(size)){
            return;
        }
        String company = data.get("company");
        if("".equals(company) || "NO".equals(company.toUpperCase())){
            return;
        }
        String brand = data.get("brand");
        String series = data.get("series");
        if("".equals(brand) || "".equals(series)){
            return;
        }

        Set<Integer> goodsIds = getGoodsIds(data, goodsList);
        if(goodsIds.isEmpty()){
            return;
        }

        List<Map<String, Object>> matchCarList = getMatchedCars(data, carList);
        if(matchCarList.isEmpty()){
            return;
        }

        for(Integer gId : goodsIds){
            for(Map<String, Object> car : matchCarList){
                String gcKey = gId+"_"+car.get("car_id").toString();
                if(goodsCarKeySet.add(gcKey)) {
                    Map<String, Object> gc = getGoodsCar(car);
                    gc.put("goodsId", gId);
                    goodsCarList.add(gc);
                }
            }
        }
    }

    //获取goods id集合
    public Set<Integer> getGoodsIds(Map<String, String> data, List<Map<String, String>> goodsList){
        Set<Integer> set = new HashSet<>();
        String sizeStr = data.get("size");
        String interfaceStr = data.get("interface");

        String[] ss = sizeStr.split("/");
        for(Map<String, String> goods : goodsList){
            String goodsName = goods.get("goodsName");
            for(String si : ss){
                if(goodsName.contains(interfaceStr) && goodsName.contains(si+"寸")){
                    set.add(Integer.valueOf(goods.get("goodsId")));
                }
            }
        }

        return set;
    }

    //车型数据比较
    public List<Map<String, Object>> getMatchedCars(Map<String, String> data, List<Map<String, Object>> carList){
        List<Map<String, Object>> matchCarList = new ArrayList<>();
        String company = data.get("company");
        String brand = data.get("brand");
        String series = data.get("series");
        String models = data.get("model")==null?"":data.get("model");
        String years = data.get("year")==null?"":data.get("year");
        String power = data.get("power")==null?"":data.get("power");

        for(Map<String, Object> car : carList){
            if(brand.equals(car.get("brand").toString())
                    && company.equals(car.get("company").toString())
                    && series.equals(car.get("series").toString())
                    && compareModel(models, car.get("model").toString())
                    && compareYear(years, car.get("year").toString())){

                if("".equals(power)){
                    matchCarList.add(getGoodsCar(car));
                }else{
                    if(car.get("power").toString().contains(power)){
                        matchCarList.add(getGoodsCar(car));
                    }
                }
            }
        }


        return matchCarList;
    }

    //比较车型
    public boolean compareModel(String models, String carModel){
        if("".equals(models)){
            return true;
        }
        models = models.replaceAll("\\\\", "/");
        String[] ss = models.split("/");
        for(String s : ss){
            if(s.equals(carModel)){
                return true;
            }
        }

        return false;
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

    //拷贝车型
    public Map<String, Object> getGoodsCar(Map<String, Object> car){
        Map<String, Object> gc = new HashMap<>();
        for(Map.Entry<String, Object> entry : car.entrySet()){
            gc.put(entry.getKey(), entry.getValue());
        }
        return gc;
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
