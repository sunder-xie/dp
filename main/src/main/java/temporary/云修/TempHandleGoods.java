package temporary.云修;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/7/28.
 */
public class TempHandleGoods extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云修商品数据处理/";
        String excel1 = path + "车型匹配.xlsx";

        //读取待处理数据
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("项目(车主/门店）", "name");
        attrMap.put("型号", "format");
        attrMap.put("物料档口价/门店成本价", "price");
        attrMap.put("型号订正", "newFormat");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel1, 1);
        List<Map<String, String>> dataList1 = readerXLSX.getDataList();
        Print.info(dataList1.size());
        Print.info(dataList1.get(0));

        Set<String> goodsFormats = new HashSet<>();
        boolean checkFlag = true;
        for(Map<String, String> data : dataList1){
            if(!goodsFormats.add(data.get("format"))){
                Print.info("存在重复的规格型号："+data);
                checkFlag = false;
            }
        }
        Print.info(goodsFormats.size());
        if(!checkFlag){
            return;
        }

        //读取商品数据
        attrMap = new HashMap<>();
        attrMap.put("goods_id", "id");
        attrMap.put("goods_name", "name");
        attrMap.put("goods_format", "format");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path+"云修商品.xls", attrMap.size());
        List<Map<String, String>> dataList2 = readerXLS.getDataList();
        Print.info(dataList2.size());
        Print.info(dataList2.get(0));

        Print.info("\n开始处理商品\n");
        for(Map<String, String> data : dataList1){
            handleGoods(data, dataList2);
        }

        Print.info("\n开始匹配车型\n");
        handleGoodsCar(dataList1);
    }

    public void handleGoods(Map<String, String> data, List<Map<String, String>> goodsList){
        for(Map<String, String> goods : goodsList){
            if (data.get("format").equals(goods.get("format"))
                    || data.get("newFormat").equals(goods.get("format"))){

                data.put("id", goods.get("id"));
                break;
            }
        }
        if(data.get("id")==null){
            //Print.info("错误的规格型号："+data);
            loop1:
            for(Map<String, String> goods : goodsList){
                String[] fs = data.get("format").split("/");
                for(String f : fs){
                    if(f.equals(goods.get("format"))){
                        data.put("id", goods.get("id"));
                        break loop1;
                    }
                }
            }
        }

        if(data.get("id")==null) {
            Print.info("错误的规格型号："+data);
        }
    }

    public void handleGoodsCar(List<Map<String, String>> goodsList){
        Set<String> ids = new HashSet<>();
        boolean checkFlag = true;
        for(Map<String, String> goods : goodsList){
            if(!ids.add(goods.get("id"))){
                Print.info("存在相同的商品id："+goods);
                checkFlag = false;
            }
        }
        if(!checkFlag){
            return;
        }

        List<Map<String, String>> unMatchGoods = new ArrayList<>();
        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            List<Map<String, Object>> carList = getCars(goods.get("id"));
            if(carList.isEmpty()){
                Print.info("没有对应关系车型："+goods);
                unMatchGoods.add(goods);
                continue;
            }
            for(Map<String, Object> car : carList){
                car.put("name", goods.get("name"));
                car.put("format", goods.get("format"));
                car.put("price", goods.get("price"));
                goodsCarList.add(car);
            }
        }

        Print.info("没有匹配上的商品："+unMatchGoods.size());
        Print.info("对应关系："+goodsCarList.size());

        PoiUtil poiUtil = new PoiUtil();
        try {
            String[] heads = new String[]{"id", "项目(车主/门店)", "型号", "物料档口价/门店成本价"};
            String[] fields = new String[]{"id", "name", "format", "price"};

            poiUtil.exportXlsxWithMap("没有车型对应关系的商品", path, heads, fields, unMatchGoods);

            List<Map<String, String>> gcList = PoiUtil.convert(goodsCarList);
            heads = new String[]{"项目(车主/门店)", "型号", "物料档口价/门店成本价", "车品牌", "车品牌id",
                    "车系", "车系id", "车型", "车型id", "排量", "排量id", "年款", "年款id", "车款", "车款id"};
            fields = new String[]{"name", "format", "price", "car_brand", "car_brand_id", "car_series", "car_series_id",
                    "car_model", "car_model_id", "car_power", "car_power_id", "car_year", "car_year_id", "car_name", "car_id"};

            poiUtil.exportXlsxWithMap("商品车型匹配-处理后", path, heads, fields, gcList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getCars(String goodsId){
        String sql = "select * from db_goods_car where goods_id="+goodsId;
        return commonMapper.selectListBySql(sql);
    }

}
