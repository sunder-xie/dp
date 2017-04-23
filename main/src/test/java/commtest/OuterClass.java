package commtest;

/**
 * Created by huangzhangting on 17/4/20.
 */
public class OuterClass {
    private Integer id;

    class InnerClass{
        private Integer icId;

        public void m1(String name){
            System.out.println(id);
            System.out.println(name);
        }
    }

}
