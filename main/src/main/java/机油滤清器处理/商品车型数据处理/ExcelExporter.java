package 机油滤清器处理.商品车型数据处理;

import dp.common.util.IoUtil;
import dp.common.util.excelutil.PoiUtil;
import 机油滤清器处理.BrandEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class ExcelExporter {

    //TODO 导出没有匹配上的数据excel
    public static void exportUnMatchData(String path, List<Map<String, String>> dataList, int type){
        PoiUtil poiUtil = new PoiUtil();

        String[] heads = null;
        String[] fields = null;
        String fileName = null;

        if(type== BrandEnum.YUN_XIU.getCode()){ //云修机滤
            fileName = "云修机滤没有匹配上的数据";

            heads = new String[]{"序号", "车系（原）", "车型（原）", "年款（年/月）", "发动机", "功率(KW)", "机油滤清器", "尺寸",
                    "品牌", "厂家", "车系", "车型", "年款", "排量", "进气形式", "最大功率", "燃料类型", "商品编码"};

            fields = new String[]{"index", "car_series", "car_model", "car_year", "car_engine", "car_power", "oil_filter", "goods_size",
                    "brand", "company", "series", "model", "year", "power", "inletType", "maxPower", "fuelType", "goodsFormat"};

        }else if(type==BrandEnum.BO_SHI.getCode()){ //博世机滤
            fileName = "博世机滤没有匹配上的数据";

            heads = new String[]{"序号", "尺寸", "商品编码",
                    "品牌", "厂家", "车系", "车型", "年款", "排量", "进气形式", "生产年份"};

            fields = new String[]{"index", "goods_size", "goodsFormat",
                    "brand", "company", "series", "model", "year", "power", "inletType", "createYear"};

        }else if(type==BrandEnum.AC_DE_KE.getCode()){ //AC德科机滤
            fileName = "AC德科机滤没有匹配上的数据";

            heads = new String[]{"序号", "商品编码",
                    "品牌", "厂家", "车系", "车型", "排量", "进气形式", "生产年份"};

            fields = new String[]{"index", "goodsFormat",
                    "brand", "company", "series", "model", "power", "inletType", "createYear"};

        }else if(type==BrandEnum.JIAN_GUAN.getCode()){ //箭冠滤清器
            fileName = "箭冠滤清器没有匹配上的数据";

            heads = new String[]{"序号", "商品编码",
                    "品牌", "厂家", "车系", "车型", "排量", "年款", "进气形式", "变速器类型"};

            fields = new String[]{"index", "goodsFormat",
                    "brand", "company", "series", "model", "power", "year", "inletType", "transmissionType"};

        }else if(type==BrandEnum.HAI_YE.getCode()){ //海业滤清器
            fileName = "海业滤清器没有匹配上的数据";

            heads = new String[]{"序号", "商品编码", "产量名称",
                    "品牌", "厂家", "车系", "车型", "排量", "年款", "进气形式", "最大功率"};

            fields = new String[]{"index", "goodsFormat", "goods_name",
                    "brand", "company", "series", "model", "power", "year", "inletType", "maxPower"};

        } else {
            dataList = new ArrayList<>();
        }

        path += "数据处理后/";
        IoUtil.mkdirsIfNotExist(path);

        try {
            poiUtil.exportXlsxWithMap(fileName, path, heads, fields, dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
