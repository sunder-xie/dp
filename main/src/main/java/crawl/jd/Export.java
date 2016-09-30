package crawl.jd;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/13.
 */
public class Export {
    private String exportPath;
    private String type;

    public Export(String path, String type) {
        this.type = type;

        exportPath = path + type + "/";
        File file = new File(exportPath);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public void exportExcel(List<Map<String, String>> unMatchGoods, Map<String, List<Map<String, String>>> matchGoodsMap,
                            List<Map<String, String>> dsUnMatchGoods, String[] headList, String[] headList2){

        Excel excel = new Excel();

        try {
            excel.exportXlsxWithMap("京东没匹配上的" + type, exportPath, headList, unMatchGoods);

            excel.exportXlsx("京东匹配上的" + type, exportPath, headList, headList2, matchGoodsMap);

            excel.exportXlsxWithMap("电商没匹配上的" + type, exportPath, headList2, dsUnMatchGoods);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
