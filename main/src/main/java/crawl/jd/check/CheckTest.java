package crawl.jd.check;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/15.
 */
public class CheckTest extends BaseTest {

    private static final String BRAND_SQL = "select distinct car_brand_id from temp_goods_car";

    private String getCarSql(String brandId){
        return "select distinct car_id from temp_goods_car where car_brand_id="+brandId;
    }

    private String getGoodsSql(String carId){
        return "select gc.car_brand,gc.car_series,gc.car_model,gc.car_power,gc.car_year,gc.car_name," +
                "g.goods_name,g.goods_format,b.brand_name,g.goods_id" +
                " from temp_goods_car gc,db_goods g,db_brand b" +
                " where gc.car_id="+carId+" and gc.goods_id=g.goods_id and g.brand_id=b.brand_id limit "+GOODS_NUM;
    }

    private static int BRAND_NUM = 5;
    private static int CAR_NUM = 6;
    private static final int GOODS_NUM = 10;

    // 5个品牌 6个车款 10个商品 = 300条记录
    @Test
    public void test(){
        List<String> brandIds = commonMapper.selectOneFieldBySql(BRAND_SQL);
        int size = brandIds.size();
        if(size==0){
            return;
        }
        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        if(size<BRAND_NUM) BRAND_NUM = size;
        for(int i=0; i<BRAND_NUM; i++){
            List<String> carIds = commonMapper.selectOneFieldBySql(getCarSql(brandIds.get(i)));
            size = carIds.size();
            if(size==0){
                continue;
            }
            if(size<CAR_NUM) CAR_NUM = size;
            for(int j=0; j<CAR_NUM; j++){
                List<Map<String, Object>> list = commonMapper.selectListBySql(getGoodsSql(carIds.get(j)));
                if(!list.isEmpty()){
                    goodsCarList.addAll(list);
                }
            }
        }

        Print.info("车型-商品关系数量："+goodsCarList.size());
        if(goodsCarList.isEmpty()){
            return;
        }

        String type = "机油";
        String path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/"+type+"/";

        String[] headList = new String[]{"车品牌", "车系", "车型", "排量", "年款", "车款", "商品名称", "规格型号", "品牌", "goods_id"};
        String[] fieldList = new String[]{"car_brand", "car_series", "car_model", "car_power", "car_year", "car_name",
                "goods_name", "goods_format", "brand_name", "goods_id"};

        PoiUtil util = new PoiUtil();
        try {
            util.exportXlsxWithMap(type+"数据抽查", path, headList, fieldList, PoiUtil.convert(goodsCarList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
