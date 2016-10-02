package temporary;

import dp.common.util.Print;
import dp.common.util.StrUtil;
import org.junit.Test;
import 机油滤清器处理.处理后数据统计.StatisticConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by huangzhangting on 16/4/14.
 */
public class MyTest<E> {

    @Test
    public void test() throws Exception{
        String s = "发侬滖闭辄轫";
        String str = new String(s.getBytes("UTF-8"), "GBK");

        String s1 = "C 200 Tourer\\C 350 Coupe\\C 350 Tourer\\C 350\\C 280\\C 280 Tourer\\C 350 CDI\\C 350 CDI Tourer\\C 300 CDI\\\\C 300 Tourer\\C 300\\C 200 K Coupe\\C 230\\C 230 TourerC 220 CDI Coupe\\C 250 CDI Tourer\\C 250 CDI Coupe\\C 220 CDI Tourer\\C 200 CDI Tourer\\C 180 CDI Tourer\\C 180 CDI\\C 200 CDI Coupe\\C 250 CDI\\C 200\\C 180 Tourer\\C 180 Coupe\\C 180\\C 180 K Coupe\\C 180 K\\C 200 K Tourer\\C 180 K Tourer\\C 250 Tourer\\C 250 Coupe\\C 200 CGI Tourer\\C 200 K\\C 250C 260 CGI\\C 200 CGI";

        Print.info(s1.replaceAll("\\\\", "/"));

        String years = "2015-";
        Print.info("2015".compareTo(years.substring(0, years.length() - 1)));

        years = "12312- ";
        int idx = years.indexOf("-");
        Print.info(years.substring(0, idx));
        Print.info(years.substring(idx+1));

        String[] ys = years.split("-");
        Print.info(ys.length);


        Map<String, String> map = new TreeMap<>();
        map.put("大众", "dz");
        map.put("风神", "dz");
        map.put("现代", "xd");
        map.put("丰田", "ft");
        map.put("阿斯顿马丁", "md");
        map.put("大发", "md");
        map.put("风行", "md");
        map.put("A-c", "md");

        Print.info(map);
        for(Map.Entry<String, String> entry : map.entrySet()){
            Print.info(entry.getKey()+" : "+entry.getValue());
        }
    }


    @Test
    public void test11() {
        String str = "手自一体变速器(IMT)";
        System.out.println(str.replaceAll("[^A-Z]", ""));

        double d1 = 56.8;
        double d2 = 56.78;
        System.out.println(d1-d2);
        System.out.println(Math.abs(d1-d2));
        System.out.println(Math.abs(d1-d2)<=1.5);


        str = "'阿尔法-罗密欧','阿斯顿马丁','安驰','Alpina','巴博斯'," +
                "'宝龙','保斐利','宾利','布加迪','宝沃','大发','大宇'," +
                "'法拉利','富奇','GMC','光冈','海格','悍马','黑豹','华北'," +
                "'黄海','华阳','恒天','华颂','九龙','金程','卡尔森'," +
                "'科尼赛克','卡威','凯翼','兰博基尼','劳伦士','劳斯莱斯'," +
                "'路特斯','罗孚','玛莎拉蒂','迈巴赫','美亚','迈凯伦'," +
                "'帕加尼','庞蒂克','启腾','RUF','SPRINGO','Scion','萨博','世爵'," +
                "'赛宝','通田','特斯拉','威兹曼','西雅特','新凯','云雀','知豆'";

        String[] brands = str.split(",");
//        for(String b : brands){
//            System.out.println("set.add(\""+b.replace("'", "")+"\");");
//        }

        String sql = StatisticConfig.notInBrandSql();
        System.out.println(sql);
    }
}
