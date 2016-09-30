package temporary;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import org.junit.Test;

import java.io.File;
import java.io.Writer;
import java.util.*;

/**
 * Created by huangzhangting on 16/5/10.
 */
public class DsGoodsCarTest extends BaseTest{

    public String newCarSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_id from db_goods_car_new where id>"+initId+") t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=6) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    public String newYearSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_year_id as car_id from db_goods_car_new where id>"+
                initId+" group by goods_id,car_year_id) t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=5) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    public String newPowerSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_power_id as car_id from db_goods_car_new" +
                " where id>"+initId+" group by goods_id,car_power_id) t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=4) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    public String newModelSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_model_id as car_id from db_goods_car_new" +
                " where id>"+initId+" group by goods_id,car_model_id) t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=3) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    public String newSeriesSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_series_id as car_id from db_goods_car_new" +
                " where id>"+initId+" group by goods_id,car_series_id) t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=2) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    public String newBrandSql(int initId){
        return "select t1.goods_id,t1.car_id from" +
                " (select goods_id,car_brand_id as car_id from db_goods_car_new" +
                " where id>"+initId+" group by goods_id,car_brand_id) t1" +
                " left join" +
                " (select goods_id,car_model from db_goods_car where level=1) t2" +
                " on t1.goods_id=t2.goods_id and t1.car_id=t2.car_model" +
                " where t2.car_model is null";
    }

    private String path;
    private Writer writer;
    private Set<String> goodsIdSet;

    @Test
    public void test(){
        int initId = 1853198;

        path = "/Users/huangzhangting/Documents/数据处理/临时处理/电商goodsCar数据处理/";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path+"insertGoodsCar-DS-"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        goodsIdSet = new HashSet<>();

        //车款 level 6
        handleGoodsCar(commonMapper.selectListBySql(newCarSql(initId)), 6);

        //年款 level 5
        handleGoodsCar(commonMapper.selectListBySql(newYearSql(initId)), 5);

        //排量 level 4
        handleGoodsCar(commonMapper.selectListBySql(newPowerSql(initId)), 4);

        //车型 level 3
        handleGoodsCar(commonMapper.selectListBySql(newModelSql(initId)), 3);

        //车系 level 2
        handleGoodsCar(commonMapper.selectListBySql(newSeriesSql(initId)), 2);

        //品牌 level 1
        handleGoodsCar(commonMapper.selectListBySql(newBrandSql(initId)), 1);

        IoUtil.closeWriter(writer);
        recordGoodsIds();
    }

    public void handleGoodsCar(List<Map<String, Object>> dataList, int level){
        int size = dataList.size();
        Print.info("level-"+level+" 新增数据："+size);
        if(size==0){
            return;
        }

        int count = 1000;
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            goodsIdSet.add(dataList.get(i).get("goods_id").toString());

            appendValueSql(sb, level, dataList.get(i));

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

    public void appendValueSql(StringBuilder sb, int level, Map<String, Object> dataMap){
        sb.append("('','','','',@nowTime,@nowTime,");
        sb.append(level).append(",");
        sb.append(dataMap.get("car_id")).append(",");
        sb.append(dataMap.get("goods_id")).append(")");
    }
    public String insertSql(){
        return "insert ignore into db_goods_car(car_brand,car_series,car_power,car_year,time_created,time_updated," +
                "level,car_model,goods_id) values ";
    }
    public void writeSql(StringBuilder values){
        values.insert(0, insertSql());
        values.append(";\n");
        IoUtil.writeFile(writer, values.toString());
    }

    //记录涉及到的goodsId
    public void recordGoodsIds(){
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path+"insertGoodsIds-"+dateStr+".txt");
        IoUtil.writeFile(writer, goodsIdSet.toString().replace("[","(").replace("]",")"));
        IoUtil.closeWriter(writer);
    }
}
