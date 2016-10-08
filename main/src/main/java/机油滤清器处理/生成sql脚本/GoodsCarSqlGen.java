package 机油滤清器处理.生成sql脚本;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/8.
 */
public class GoodsCarSqlGen extends BaseTest{
    private List<Map<String, Object>> goodsCarList;
    private Set<String> unMatchGoodsFormats;
    private Set<String> matchGoodsIds;
    private List<Map<String, Object>> carInfoList;


    public GoodsCarSqlGen(String path) {
        this.path = path;

        init();
    }
    private void init(){
        goodsCarList = new ArrayList<>();
        matchGoodsIds = new HashSet<>();
        unMatchGoodsFormats = new HashSet<>();
        carInfoList = getCarInfoList();
    }

    public List<Map<String, Object>> getGoodsCarList() {
        return goodsCarList;
    }

    public Set<String> getUnMatchGoodsFormats() {
        return unMatchGoodsFormats;
    }

    public Set<String> getMatchGoodsIds() {
        return matchGoodsIds;
    }

    //车型信息相关
    public List<Map<String, Object>> getCarInfoList(){
        String sql = "select brand,brand_id,series,series_id,model,model_id," +
                "power,power_id,year,year_id,car_models as car_name,car_models_id as car_id, company " +
                "from db_car_all group by car_models_id";

        return commonMapper.selectListBySql(sql);
    }
    private Map<String, Object> getCarInfo(String carId, List<Map<String, Object>> carInfoList){
        for(Map<String, Object> car : carInfoList){
            if(carId.equals(car.get("car_id").toString())){
                return ObjectUtil.copyMap(car);
            }
        }
        return null;
    }


    //处理关系数据
    @Deprecated
    public void handleGoodsCarList(List<Map<String, String>> goodsList, List<Map<String, Object>> carList){

        for(Map<String, Object> car : carList){
            String goodsFormat = car.get("goods_format").toString();
            String carId = car.get("car_models_id").toString();

            boolean flag = false;

            for(Map<String, String> goods : goodsList){
                String format = goods.get("goodsFormat");
                if(goodsFormat.replace(" ", "").equals(format.replace(" ", ""))){
                    Map<String, Object> gc = getCarInfo(carId, carInfoList);
                    if(gc!=null){
                        gc.put("goods_id", goods.get("goodsId"));
                        gc.put("goods_format", format);
                        goodsCarList.add(gc);

                        matchGoodsIds.add(goods.get("goodsId"));

                    }else{
                        Print.info("错误的车款id："+car);
                    }

                    flag = true;
                }
            }

            if(!flag){
                unMatchGoodsFormats.add(goodsFormat);
            }
        }

    }
    public void handleGoodsCar(List<Map<String, String>> goodsList, String carId, String goodsFormat){

        boolean flag = false;

        for(Map<String, String> goods : goodsList){
            String format = goods.get("goodsFormat");
            if(goodsFormat.replace(" ", "").equals(format.replace(" ", ""))){
                Map<String, Object> gc = getCarInfo(carId, carInfoList);
                if(gc!=null){
                    gc.put("goods_id", goods.get("goodsId"));
                    gc.put("goods_format", format);
                    goodsCarList.add(gc);

                    matchGoodsIds.add(goods.get("goodsId"));

                }else{
                    Print.info("错误的车款id，carId="+carId+" goodsFormat="+goodsFormat);
                }

                flag = true;
            }
        }

        if(!flag){
            unMatchGoodsFormats.add(goodsFormat);
        }

    }


    //处理sql
    public void handleSql(String sqlFileName){
        this.handleSql(sqlFileName, this.goodsCarList);
    }
    public void handleSql(String sqlFileName, List<Map<String, Object>> goodsCarList){
        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(sqlPath + sqlFileName + "_" + dateStr + ".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

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
                break;
            }
            sb.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    private void addGcVal(StringBuilder sb, Map<String, Object> data){
        sb.append("(");
        sb.append(data.get("goods_id")).append(",");
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
    private void writeGcSql(StringBuilder sb){
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("insert ignore into db_goods_car");
        sqlSb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sqlSb.append(" values ").append(sb).append(";\n");

        IoUtil.writeFile(writer, sqlSb.toString());
    }


    //处理需要删除的数据
    public void handleDeleteSql(String sqlFileName){
        if(matchGoodsIds.isEmpty()){
            return;
        }
        List<String> goodsIdList = new ArrayList<>(matchGoodsIds);

        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(sqlPath + sqlFileName + "_" + dateStr + ".sql");

        int count = 50;
        int size = goodsIdList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            sql.append(goodsIdList.get(i));
            if((i+1)%count==0){
                writeDeleteSql(sql);
                sql.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeDeleteSql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }
    private void writeDeleteSql(StringBuilder sql){
        sql.insert(0, "delete from db_goods_car where goods_id in(");
        sql.append(");\n");

        IoUtil.writeFile(writer, sql.toString());
    }

}
