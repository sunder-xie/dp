package commtest;

/**
 * Created by huangzhangting on 17/4/20.
 */
public class InnerClassTest {

    public static void main(String[] args){
        OuterClass outerClass = new OuterClass();
        OuterClass.InnerClass innerClass = outerClass.new InnerClass();
        System.out.println(innerClass);
    }

}
