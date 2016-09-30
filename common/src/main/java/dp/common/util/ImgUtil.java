package dp.common.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by huangzhangting on 16/3/28.
 */
public class ImgUtil {

    private static String handleUrl(String urlString){
        return urlString.replace("%","%25").replace(" ","%20").replace("#","%23").replace("+","%2B");
    }

    public static boolean downloadImg(String urlString, String fileName) {
        try {
            // 构造URL
            URL url = new URL(handleUrl(urlString));
            // 打开连接
            URLConnection con = url.openConnection();
            // 输入流
            InputStream is = con.getInputStream();
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            OutputStream os = new FileOutputStream(fileName);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            // 完毕，关闭所有链接
            os.close();
            is.close();

            return true;

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
