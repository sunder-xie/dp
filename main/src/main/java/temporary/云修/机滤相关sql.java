package temporary.云修;

/**
 * Created by huangzhangting on 16/9/27.
 */
public class 机滤相关sql {

    public static String getNewDataSql(int type, int gcId){
        String sql = "select cc2.* " +
                "from " +
                "(select * from db_car_category where level=6) cc1, " +
                "( " +
                "select tb2.* " +
                "from " +
                "(select c1.car_models_id " +
                "from " +
                "(select tt1.car_models_id,count(1) as num " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + type +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id having count(1)=1) c1 " +
                "left join  " +
                "(select distinct car_id from db_goods_car where id>"+ gcId +") c2 " +
                "on c1.car_models_id=c2.car_id " +
                "where c2.car_id is null) tb1, " +
                "( " +
                "select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + type +
                " group by t2.car_models_id,t1.goods_format " +
                ") tb2 " +
                "where tb1.car_models_id=tb2.car_models_id " +
                ") cc2 " +
                "where cc1.id=cc2.car_models_id " +
                "and cc1.power!='电动' and cc1.name not like '%柴油%' ";

        return sql;
    }

    public static String getNewDataSqlNeedCheck(int type, int gcId){
        String sql = "select cc2.*,cc1.brand,cc1.company,cc1.series,cc1.model, " +
                "cc1.power,cc1.year,cc1.name " +
                "from " +
                "(select * from db_car_category where level=6) cc1, " +
                "(select tb2.* " +
                "from " +
                "(select c1.car_models_id " +
                "from " +
                "(select tt1.car_models_id,count(1) as num " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + type +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id having count(1)>1) c1 " +
                "left join  " +
                "(select distinct car_id from db_goods_car where id>"+ gcId +") c2 " +
                "on c1.car_models_id=c2.car_id " +
                "where c2.car_id is null) tb1, " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + type +
                " group by t2.car_models_id,t1.goods_format) tb2 " +
                "where tb1.car_models_id=tb2.car_models_id) cc2 " +
                "where cc1.id=cc2.car_models_id " +
                "and cc1.power!='电动' and cc1.name not like '%柴油%' ";

        return sql;
    }
    
    public static String getCarListSql(){
        String sql = "select id,brand,company,series,model,power,`year`,`name` " +
                "from db_car_category where level=6  " +
                "and power!='电动' and `name` not like '%柴油%' and `year`>='2005'  " +
                "and brand not in( " +
                "'阿尔法-罗密欧','阿斯顿马丁','安驰','Alpina','巴博斯', " +
                "'宝龙','保斐利','宾利','布加迪','宝沃','大发','大宇', " +
                "'法拉利','富奇','GMC','光冈','海格','悍马','黑豹','华北', " +
                "'黄海','华阳','恒天','华颂','九龙','金程','卡尔森', " +
                "'科尼赛克','卡威','凯翼','兰博基尼','劳伦士','劳斯莱斯', " +
                "'路特斯','罗孚','玛莎拉蒂','迈巴赫','美亚','迈凯伦', " +
                "'帕加尼','庞蒂克','启腾','RUF','SPRINGO','Scion','萨博','世爵', " +
                "'赛宝','通田','特斯拉','威兹曼','西雅特','新凯','云雀','知豆' " +
                ") order by brand,company,series,model,power,`year`,`name` ";
        
        return sql;
    }

}
