package crawl.jd;

/**
 * Created by huangzhangting on 16/4/13.
 */
public class Sql {
    public static String getGcSql(String jdGoodsId){
        StringBuilder sb = new StringBuilder();
        sb.append("select gc.goods_id,ca.car_models_id,ca.car_models,ca.brand,ca.brand_id,ca.series,ca.series_id,ca.model,ca.model_id,ca.power,ca.power_id,ca.year,ca.year_id");
        sb.append(" from db_car_all ca,(select * from ly_id_goods where goods_id=");
        sb.append(jdGoodsId);
        sb.append(") gc where gc.ly_id=ca.new_l_id group by ca.car_models_id");

        return sb.toString();
    }

    public static String selectNewGcSql(){
        StringBuilder sb = new StringBuilder();
        sb.append("select t1.goods_id,t1.car_id,t1.car_name,t1.car_brand_id,t1.car_brand,t1.car_series_id,t1.car_series,t1.car_model_id,t1.car_model,t1.car_power_id,t1.car_power,t1.car_year_id,t1.car_year");
        sb.append(" from temp_goods_car t1 left join db_goods_car t2");
        sb.append(" on t1.car_id=t2.car_id and t1.goods_id=t2.goods_id where t2.id is null");

        return sb.toString();
    }

    public static String selectGcSql(Integer dsGoodsId){
        StringBuilder sb = new StringBuilder();
        sb.append("select goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year");
        sb.append(" from temp_goods_car where goods_id=").append(dsGoodsId);

        return sb.toString();
    }

}
