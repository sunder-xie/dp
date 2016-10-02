package 机油滤清器处理.处理后数据统计;

import dp.common.util.ObjectUtil;
import dp.common.util.excelutil.PoiUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class ExcelExporter {
    public static void exportGoodsInfo(String path, List<Map<String, String>> dataList){
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"品牌", "商品型号", "商品sn"};
        String[] fields = new String[]{"brand", "goodsFormat", "goodsSn"};

        try {
            poiUtil.exportXlsxWithMap("可用的机滤型号信息", path, heads, fields, dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportUnCoverCars(String path, List<Map<String, Object>> dataList){
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"id", "brand", "company", "series", "model", "power", "year", "name"};

        List<Map<String, String>> mapList = ObjectUtil.objToStrMapList(dataList);

        try {
            poiUtil.exportXlsxWithMap("机滤未覆盖的车款", path, heads, fields, mapList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
