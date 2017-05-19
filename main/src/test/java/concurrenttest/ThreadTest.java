package concurrenttest;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Created by huangzhangting on 17/5/2.
 */
public class ThreadTest {

    private final Object object = new Object();

    public static void main(String[] args){
//        Thread thread = new Thread();
//        thread.start();
//        thread.start();

        ThreadTest.get1();

        ThreadTest tt = new ThreadTest();
        tt.get1();
    }

    public void test1(){

        synchronized(object){
            System.out.println(123);
        }

    }

    public synchronized void get(){

    }

    public synchronized static void get1(){
        System.out.println("get11111");
    }
}
