package proxytest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangzhangting on 17/4/23.
 */
public class UserBizImpl implements UserBiz {

    public Object getUser(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "hzt");
        System.out.println("user ---> "+map);
        test_private();
        test_public();
        return map;
    }

    private void test_private(){
        System.out.println("===== test private =====");
    }

    public void test_public(){
        System.out.println("===== test public =====");
    }

}
