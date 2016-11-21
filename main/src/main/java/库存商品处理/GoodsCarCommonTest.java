package 库存商品处理;

import base.BaseTest;
import dp.common.util.Print;
import org.junit.Test;
import 机油滤清器处理.生成sql脚本.GoodsCarSqlGen;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/10.
 */
public class GoodsCarCommonTest extends BaseTest {

    /**
     * TODO 生成db_goods_car的sql
     * 确保 db_goods_car_mini 中的数据只有最新的数据
     */
    @Test
    public void testGC() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        List<Map<String, Object>> addDataList = getAddDataList();
        Print.info(addDataList.size());

        List<String> modifyDataList = getModifyDataList();
        Print.info(modifyDataList.size());


        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);
        for(Map<String, Object> add : addDataList){
            Map<String, Object> carInfo = sqlGen.getCarInfo(add.get("car_id").toString());
            if(carInfo==null){
                Print.info("错误的车款id："+add);
            }else{
                carInfo.put("goods_id", add.get("goods_id"));
                goodsCarList.add(carInfo);
            }
        }

        sqlGen.handleSql("add_goods_car", goodsCarList);
        sqlGen.handleModifySql("modify_goods_car", modifyDataList);
    }

    public List<Map<String, Object>> getAddDataList(){
//        String sql = "select t1.goods_id,t1.car_id " +
//                "from " +
//                "db_goods_car_mini t1 " +
//                "left join " +
//                "(select id,goods_id,car_id from db_goods_car where `status`=1) t2 " +
//                "on t1.goods_id=t2.goods_id and t1.car_id=t2.car_id " +
//                "where t2.id is null";

        String sql = "select t3.car_id,t3.goods_id " +
                "from " +
                "(select t1.goods_id,t1.car_id " +
                "from db_goods_car_mini t1  " +
                "left join " +
                "(select id,goods_id,car_id from db_goods_car where `status`=1) t2 " +
                "on t1.goods_id=t2.goods_id and t1.car_id=t2.car_id " +
                "where t2.id is null) t3 " +
                "left join " +
                "(select t2.* " +
                "from db_goods_car_mini t1, " +
                "(select id,goods_id,car_id from db_goods_car where `status`!=1) t2 " +
                "where t1.goods_id=t2.goods_id and t1.car_id=t2.car_id) t4 " +
                "on t3.goods_id=t4.goods_id and t3.car_id=t4.car_id " +
                "where t4.id is null";

        return commonMapper.selectListBySql(sql);
    }
    public List<String> getModifyDataList(){
        String sql = "select t2.id " +
                "from " +
                "db_goods_car_mini t1, " +
                "(select id,goods_id,car_id from db_goods_car where `status`!=1) t2 " +
                "where t1.goods_id=t2.goods_id and t1.car_id=t2.car_id";

        return commonMapper.selectOneFieldBySql(sql);
    }

}
