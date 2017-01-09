package temporary;

import base.BaseTest;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/12/28.
 */
public class GoodsCarSumTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/电商商品数据/";
        String excel = path + "刹车片-蹄数据.xls";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("g.goods_id", "goodsId");
        attrMap.put("g.goods_name", "goodsName");
        attrMap.put("g.goods_format", "goodsFormat");
        attrMap.put("b.brand_name", "brandName");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.printList(goodsList);


        List<Map<String, Object>> mapList = new ArrayList<>();

        for(Map<String, String> goods : goodsList){
            String goodsId = goods.get("goodsId");
            List<Map<String, Object>> carInfos = getCarInfoByGoodsId(goodsId);
            if(carInfos.isEmpty()){
                continue;
            }

            setGoodsInfo(goods, carInfos);

            mapList.addAll(carInfos);
        }
        Print.printList(mapList);


        String[] heads = new String[]{"goodsId", "brandName", "goodsName", "goodsFormat", "brand", "company",
                "series", "model", "power", "year", "name"};

        List<Map<String, String>> list = ObjectUtil.objToStrMapList(mapList);

        PoiUtil poiUtil = new PoiUtil();

        poiUtil.exportXlsxWithMap("刹车片蹄-适配车型", path, heads, list);

    }

    public List<Map<String, Object>> getCarInfoByGoodsId(String goodsId){
        String sql = "select c.* from db_goods_car gc, db_car_category c " +
                "where gc.car_id=c.id and goods_id="+goodsId;

        return commonMapper.selectListBySql(sql);
    }

    private void setGoodsInfo(Map<String, String> goods, List<Map<String, Object>> carInfos){
        for(Map<String, Object> car : carInfos){
            car.put("goodsId", goods.get("goodsId"));
            car.put("goodsName", goods.get("goodsName"));
            car.put("goodsFormat", goods.get("goodsFormat"));
            car.put("brandName", goods.get("brandName"));
        }
    }


    /* 商品数据处理 */
    @Test
    public void test_goods() throws Exception{
        path = "/Users/huangzhangting/Desktop/电商商品数据/";
        String excel = path + "刹车片-蹄数据.xls";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "goodsId");
        attrMap.put("商品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("商品品牌", "brandName");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.printList(goodsList);

        attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        excel = path + "在售的goodsId.xls";
        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> idMapList = readerXLS.getDataList();
        Print.printList(idMapList);

        Set<String> onSaleGoodsIds = new HashSet<>();
        for(Map<String, String> idMap : idMapList){
            onSaleGoodsIds.add(idMap.get("goodsId"));
        }

        for(Map<String, String> goods : goodsList){
            if(onSaleGoodsIds.contains(goods.get("goodsId"))){
                goods.put("isOnSale", "是");
            }else{
                goods.put("isOnSale", "否");
            }
        }

        String[] heads = new String[]{"id", "商品名称", "规格型号", "商品品牌", "是否在售"};
        String[] fields = new String[]{"goodsId", "goodsName", "goodsFormat", "brandName", "isOnSale"};

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("刹车片-蹄数据", path, heads, fields, goodsList);

    }

}
