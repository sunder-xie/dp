import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangzhangting on 17/3/7.
 */
public class JevalTest {

    @Test
    public void test11(){
        String exp = "a-g+u*t";
        formatExp(exp);
    }

    //变量只能是英文字符
    private String formatExp(String exp){
        String reg = "([a-zA-Z]+)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(exp);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
//            System.out.println(matcher.group(1));
            matcher.appendReplacement(sb, "#{" + matcher.group(1) + "}");
        }

//        System.out.println();
//        System.out.println(exp);
//        System.out.println(sb.toString());

        matcher.appendTail(sb);
//        System.out.println(sb.toString());

        return sb.toString();
    }

    @Test
    public void test() {
        String exp = "(x - c) * r + c";
        exp = formatExp(exp);

        Evaluator evaluator = new Evaluator();
        evaluator.putVariable("x", "1000.98");
        evaluator.putVariable("c", "500");
        evaluator.putVariable("r", "0.08");
        evaluator.putVariable("hzt", "5000");

        try {
            String fun = evaluator.replaceVariables(exp);

            String result = evaluator.evaluate(exp);

            System.out.println(fun+" = "+result);

        } catch (EvaluationException e) {
            e.printStackTrace();
        }

    }

}
