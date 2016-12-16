package dp.common.util;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangzhangting on 16/2/25.
 */
public class StrUtil {
    public static String toUpCase(String str){
        if(str==null){
            return "";
        }
        return str.replace(" ", "").toUpperCase();
    }

    public static String strip(String str){
        if(str==null){
            return "";
        }
        return str.replace("\r\n", "").replace("\n", "").trim();
    }

    public static String rep(String str){
        if(str==null){
            return "";
        }
        return toUpCase(str.replace("-", "").replace("·", "").replace("/", "")).replace("・", "");
    }

    //除去非中文字符
    public static String repNotCN(String str){
        return str.replaceAll("[^\u4E00-\u9FA5]", "");
    }

    //除去中文字符
    public static String repCN(String str){
        return str.replaceAll("[\u4E00-\u9FA5]", "");
    }

    //去掉括号
    public static String repBrackets(String str){
        if(str==null){
            return "";
        }
        return str.replace("(","").replace(")","").replace("（", "").replace("）", "");
    }

    public static String[] convertList(List<String> list){
        int size = list.size();
        String[] strings = new String[size];
        for(int i=0; i<size; i++){
            strings[i] = list.get(i);
        }
        return strings;
    }

    //英文
    public static boolean isEn(String str){
        Pattern p = Pattern.compile("^[a-zA-Z]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    //处理oe码
    public static String handleOe(String oe){
        return oe.toUpperCase().replaceAll("[^0-9A-Z]", "");
    }


    public static byte[] strToBytes(String str){
        if(StringUtils.isEmpty(str)){
            return new byte[0];
        }
        String[] strings = str.split(",");
        int len = strings.length;
        byte[] bytes = new byte[len];
        for(int i=0; i<len; i++){
            bytes[i] = Byte.parseByte(strings[i]);
        }
        return bytes;
    }
}
