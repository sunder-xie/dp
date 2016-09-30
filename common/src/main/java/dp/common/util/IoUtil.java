package dp.common.util;

import java.io.*;

/**
 * Created by huangzhangting on 16/1/12.
 */
public class IoUtil {
    public static Writer getWriter(String fileName){
        return getWriter(fileName, "UTF-8");
    }

    public static Writer getWriter(String fileName, String code){
        try {
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file, true);//true：追加到文件尾部
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, code);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            return writer;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void writeFile(Writer writer, String str){
        try {
            if(writer==null){
                return;
            }
            writer.write(str);

            writer.flush();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void closeWriter(Writer writer){
        try {
            if(writer==null){
                return;
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileExists(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            return true;
        }
        Print.info("文件不存在："+fileName);
        return false;
    }

    public static boolean fileNotExists(String fileName){
        return !fileExists(fileName);
    }

    public static void mkdirsIfNotExist(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdirs();
        }
    }
}
