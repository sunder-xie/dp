package temporary.epc;

import dp.common.util.DateUtils;
import dp.common.util.LocalConfig;
import dp.common.util.UpYunClient;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Created by huangzhangting on 15/7/5.
 */
public class PicUploader {
    // 运行前先设置好以下三个参数
    private static final String BUCKET_NAME = LocalConfig.UPYUN_BUCKET_NAME;
    private static final String OPERATOR_NAME = LocalConfig.UPYUN_OPERATOR_NAME;
    private static final String OPERATOR_PWD = LocalConfig.UPYUN_OPERATOR_PWD;

    private static UpYunClient upyun;

    public static final String uploadPath = "images/cloudepc/part/";

    static {
        upyun = new UpYunClient(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        upyun.setTimeout(30);
    }

    public static boolean uploadImg(String filePath, File file) {
        // 上传文件，并自动创建父级目录（最多10级）
        try {
            return upyun.writeFile(filePath, file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String newFileName(String fileType){
        return uploadPath + DateUtils.getTimestamp() + fileType.toLowerCase().trim();
    }


    @Test
    public void test_readDir() throws Exception {
        //System.out.println(upyun);
        List<UpYunClient.FolderItem> list = upyun.readDir(uploadPath);
        System.out.println(list.size());
        for(UpYunClient.FolderItem folderItem : list){
            System.out.println(folderItem.toString());
        }
    }

}
