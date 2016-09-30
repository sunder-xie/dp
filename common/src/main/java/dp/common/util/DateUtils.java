package dp.common.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by huangzhangting on 15/9/2.
 */
public class DateUtils {
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static String dateToString(Date date, String dateFormat){
        DateTime dt = new DateTime(date);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);

        return dt.toString(formatter);
    }

    public static Date stringToDate(String str, String dateFormat){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
        DateTime dt = DateTime.parse(str, formatter);

        return dt.toDate();
    }

    public static long getTimestamp(){
        return System.currentTimeMillis();
    }


}
