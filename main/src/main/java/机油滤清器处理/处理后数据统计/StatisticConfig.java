package 机油滤清器处理.处理后数据统计;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class StatisticConfig {

    //商品-车型，一对一数据sql
    public static String oneToOneDataSql(int code){
        String sql = "select tb2.car_models_id,tb2.goods_format " +
                "from " +
                "(select tt1.car_models_id,count(1) " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + code +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id  " +
                "having count(1)=1) tb1, " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + code +
                " group by t2.car_models_id,t1.goods_format) tb2, db_car_category c " +
                "where tb1.car_models_id=tb2.car_models_id " +
                "and tb2.car_models_id=c.id and c.power!='电动' and c.name not like '%柴油%' ";

        return sql;
    }

    //商品型号
    public static String goodsFormatSql(int code){
        return "select distinct goods_format from temp_goods_lyid_rel where brand_code="+code;
    }

    //机油车款
    public static String oilCarSql(){
        String sql = "select id,brand,company,series,model,power,`year`,`name` " +
                "from db_car_category where level=6 " +
                "and power!='电动' and `name` not like '%柴油%' ";

        return sql;
    }

    //需要验证的车款数据sql
    public static String needCheckCarsSql(){
        String sql = "select id,brand,company,series,model,power,`year`,`name` " +
                "from db_car_category where level=6 " +
                "and power!='电动' and `name` not like '%柴油%' and `year`>='2005' " +
                "and brand not in(" +
                notInBrandSql() +
                ") order by brand,company,series,model,power,`year`,`name` ";

        return sql;
    }



    //排除的品牌
    public static String notInBrandSql(){
        Set<String> set = new HashSet<>();
        set.add("阿尔法-罗密欧");
        set.add("阿斯顿马丁");
        set.add("安驰");
        set.add("Alpina");
        set.add("巴博斯");
        set.add("宝龙");
        set.add("保斐利");
        set.add("宾利");
        set.add("布加迪");
        set.add("宝沃");
        set.add("大发");
        set.add("大宇");
        set.add("法拉利");
        set.add("富奇");
        set.add("GMC");
        set.add("光冈");
        set.add("海格");
        set.add("悍马");
        set.add("黑豹");
        set.add("华北");
        set.add("黄海");
        set.add("华阳");
        set.add("恒天");
        set.add("华颂");
        set.add("九龙");
        set.add("金程");
        set.add("卡尔森");
        set.add("科尼赛克");
        set.add("卡威");
        set.add("凯翼");
        set.add("兰博基尼");
        set.add("劳伦士");
        set.add("劳斯莱斯");
        set.add("路特斯");
        set.add("罗孚");
        set.add("玛莎拉蒂");
        set.add("迈巴赫");
        set.add("美亚");
        set.add("迈凯伦");
        set.add("帕加尼");
        set.add("庞蒂克");
        set.add("启腾");
        set.add("RUF");
        set.add("SPRINGO");
        set.add("Scion");
        set.add("萨博");
        set.add("世爵");
        set.add("赛宝");
        set.add("通田");
        set.add("特斯拉");
        set.add("威兹曼");
        set.add("西雅特");
        set.add("新凯");
        set.add("云雀");
        set.add("知豆");

        StringBuilder sql = new StringBuilder();
        for(String brand : set){
            sql.append(",").append("'").append(brand).append("'");
        }
        sql.deleteCharAt(0);

        return sql.toString();
    }

}
