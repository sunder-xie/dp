package commtest;

import java.lang.reflect.Field;

/**
 * Created by huangzhangting on 17/4/23.
 */
public class StringTest {

    public static void main(String[] args) throws Exception{
        String str = "hzt";
        Class c = str.getClass();
        Field field = c.getDeclaredField("value");
        field.setAccessible(true);
        Object object = field.get(str);
        char[] arr = (char[])object;

        System.out.println(str);
        arr[1] = ' ';
        System.out.println(str);
    }
}
