package 机油滤清器处理.生成sql脚本;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.dao.mapper.CommonMapper;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/8.
 */
public class GoodsCarSqlGen extends BaseTest{
    public static String GOODS_CAR_TABLE = "db_goods_car";
    private List<Map<String, Object>> goodsCarList;
    private Set<String> unMatchGoodsFormats;
    private Set<String> matchGoodsIds;
    private List<Map<String, Object>> carInfoList;


    public GoodsCarSqlGen(String path, CommonMapper commonMapper) {
        this.path = path;
        this.commonMapper = commonMapper;

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
//        String sql = "select brand,brand_id,series,series_id,model,model_id," +
//                "power,power_id,year,year_id,car_models as car_name,car_models_id as car_id, company " +
//                "from db_car_all group by car_models_id";

        String sql = "select t6.id as car_id,t6.`name` as car_name,t6.company, " +
                "t5.`name` as year,t5.id as year_id,t4.`name` as power,t4.id as power_id, " +
                "t3.`name` as model,t3.id as model_id,t2.`name` as series,t2.id as series_id, " +
                "t1.`name` as brand,t1.id as brand_id " +
                "from " +
                "(select id,name,pid,company from db_car_category where level=6) t6, " +
                "(select id,name,pid from db_car_category where level=5) t5, " +
                "(select id,name,pid from db_car_category where level=4) t4, " +
                "(select id,name,pid from db_car_category where level=3) t3, " +
                "(select id,name,pid from db_car_category where level=2) t2, " +
                "(select id,name from db_car_category where level=1) t1 " +
                "where t1.id=t2.pid and t2.id=t3.pid and t3.id=t4.pid " +
                "and t4.id=t5.pid and t5.id=t6.pid ";

        return commonMapper.selectListBySql(sql);
    }
    public Map<String, Object> getCarInfo(String carId){
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
                    Map<String, Object> gc = getCarInfo(carId);
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
                Map<String, Object> gc = getCarInfo(carId);
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
        if(CollectionUtils.isEmpty(goodsCarList)){
            Print.info("没有需要新增的数据");
            return;
        }

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
        sqlSb.append("insert ignore into ");
        sqlSb.append(GOODS_CAR_TABLE);
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

    //处理更新数据
    public void handleModifySql(String sqlFileName, List<String> idList){
        if(CollectionUtils.isEmpty(idList)){
            Print.info("没有需要改动的数据");
            return;
        }
        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(sqlPath + sqlFileName + "_" + dateStr + ".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        int count = 50;
        int size = idList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            sql.append(idList.get(i));
            if((i+1)%count==0){
                writeModifySql(sql);
                sql.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeModifySql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }
    private void writeModifySql(StringBuilder sql){
        sql.insert(0, "update db_goods_car set status=1,gmt_modified=@nowTime where id in(");
        sql.append(");\n");

        IoUtil.writeFile(writer, sql.toString());
    }

}
