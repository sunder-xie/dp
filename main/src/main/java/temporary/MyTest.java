package temporary;

import dp.common.util.Print;
import dp.common.util.StrUtil;
import org.junit.Test;

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

    }
}
