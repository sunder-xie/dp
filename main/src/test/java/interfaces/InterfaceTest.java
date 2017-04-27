package interfaces;

/**
 * Created by huangzhangting on 17/4/27.
 */
public interface InterfaceTest {
    abstract void test111();

    public default Integer getId(){
        return 1;
    }

    public static String getName(){
        return "hzt";
    }

}
