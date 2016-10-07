package 机油滤清器处理.生成sql脚本;

import dp.common.util.IoUtil;
import dp.common.util.excelutil.PoiUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class ExcelExporter {

    public static void exportCoverCars(String path, List<Map<String, Object>> dataList){
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"car_id", "goods_format", "brand", "company", "series", "model", "power", "year", "car_name"};

        List<Map<String, String>> mapList = PoiUtil.convert(dataList);

        Collections.sort(mapList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("brand") + o1.get("company") + o1.get("model") + o1.get("year") + o1.get("car_name");
                String str2 = o2.get("brand") + o2.get("company") + o2.get("model") + o2.get("year") + o2.get("car_name");
                return str1.compareTo(str2);
            }
        });

        String filePath = path + "覆盖车款数据/";
        IoUtil.mkdirsIfNotExist(filePath);

        try {
            poiUtil.exportXlsxWithMap("机滤覆盖的车款信息", filePath, heads, fields, mapList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
