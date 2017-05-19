package commtest;

import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import interfaces.InterfaceTest;
import lombok.NonNull;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by huangzhangting on 17/4/27.
 */
public class CommTest {

    @Test
    public void test_Lambda(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("new Runnable");
            }
        });
        thread.run();

        thread = new Thread(() -> {
            System.out.println("use Lambda");
        });
        thread.run();
    }

    @Test
    public void test_Stream(){
        List<Integer> list = Lists.newArrayList(190,20,3,4);
        long l = list.stream().sequential().sorted().count();
        System.out.println(l);

        test_annotation(null);
    }
    private void test_annotation(@NotNull Integer id){
        System.out.println(id);
    }

    @Test
    public void test_time(){
        LocalDate date = LocalDate.now();
        System.out.println(date.toString());

        LocalTime time = LocalTime.now();
        System.out.println(time.toString());

        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.toString());

    }

    @Test
    public void test_11(){
        test_switch(2);

        System.out.println(Runtime.getRuntime().availableProcessors());
    }

    public void test_switch(int i){
        int result = 0;
        switch (i){
            case 1:
                result += 1;
            case 3:
                result += 3;
            case 2:
                result += 2;
        }
        System.out.println(result);
    }

}
