package commtest;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by huangzhangting on 17/4/23.
 */
public class StringTest {
    transient IOTest ioTest;

    public static void main(String[] args) throws Exception{
        String str = "hzt";
        System.out.println(str.hashCode());


//        Class c = str.getClass();
//        Field field = c.getDeclaredField("value");
//        field.setAccessible(true);
//        Object object = field.get(str);
//        char[] arr = (char[])object;
//
//        System.out.println(str);
//        arr[1] = ' ';
//        System.out.println(str);
//
//        List<Integer> list = Lists.newArrayList(1,2,3,56,90);
//        for(Integer i : list){
//            System.out.println(i);
//        }
//
//        str = "hzt";
//        test_switch(str);
    }


    public static void test_switch(String str){
        switch (str){
            case "h":
                System.out.println(1);
                break;
            case "z":
                System.out.println(2);
                break;
            case "t":
                System.out.println(3);
                break;
            default:
                System.out.println(0);
                break;
        }
    }

    public static void test_switch(Integer i){
        switch (i){
            case 10000:
                System.out.println(1);
                break;
            case 200000:
                System.out.println(2);
                break;
            default:
                System.out.println(0);
                break;
        }
    }
}
