package temporary;

import base.BaseTest;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/10/17.
 */
public class 商品车型关系数据Test extends BaseTest {

    private String getSql(){
        String sql = "select t2.*,t3.new_goods_sn,t3.is_delete,t3.is_real " +
                "from " +
                "(select gc.goods_id,count(1) as num " +
                "from " +
                "(select goods_id,car_model_id " +
                "from db_goods_car where STATUS=1 group by goods_id,car_model_id) gc " +
                "group by gc.goods_id " +
                "order by num desc limit 1000) t1, " +
                "(select goods_id,car_model_id,car_brand,car_series,car_model " +
                "from db_goods_car where STATUS=1 group by goods_id,car_model_id) t2, " +
                "db_goods t3 " +
                "where t1.goods_id=t2.goods_id and t3.goods_id=t2.goods_id " +
                "order by goods_id,car_brand,car_series,car_model";

        return sql;
    }

    @Test
    public void test() throws Exception{
        List<Map<String, Object>> goodsCarList = commonMapper.selectListBySql(getSql());
        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        path = "/Users/huangzhangting/Desktop/";
        String[] heads = new String[]{"商品id", "商品编码", "车品牌", "车系", "车型", "商品.is_delete", "商品.is_real"};
        String[] fields = new String[]{"goods_id", "new_goods_sn", "car_brand", "car_series", "car_model", "is_delete", "is_real"};

        PoiUtil poiUtil = new PoiUtil();

        List<Map<String, String>> mapList = ObjectUtil.objToStrMapList(goodsCarList);

        poiUtil.exportXlsxWithMap("商品车型关系数据", path, heads, fields, mapList);
    }
}
