package dp.common.util;

import java.util.List;

/**
 * Created by huangzhangting on 16/3/16.
 */
public class Print {
    public static void info(Object msg){
        System.out.println(msg+"");
    }

    public static void printList(List<?> list){
        info(list.size());
        info(list.get(0));
    }
}
