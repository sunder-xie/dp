package interfaces;

import org.junit.Test;

/**
 * Created by huangzhangting on 17/4/27.
 */
public class TestIF {

    @Test
    public void test_11(){
        System.out.println(InterfaceTest.getName());
        InterfaceTest interfaceTest = new InterfaceImpl();
        System.out.println(interfaceTest.getId());


    }
}
