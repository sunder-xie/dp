package 库存商品处理;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
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
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/";

        List<Map<String, Object>> addDataList = getAddDataList();
        Print.info(addDataList.size());

        List<String> modifyDataList = getModifyDataList();
        Print.info(modifyDataList.size());

        //需要上线的商品id集合
        Set<String> goodsIdSet = new HashSet<>();

        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);
        for(Map<String, Object> add : addDataList){
            Map<String, Object> carInfo = sqlGen.getCarInfo(add.get("car_id").toString());
            if(carInfo==null){
                Print.info("错误的车款id："+add);
            }else{
                carInfo.put("goods_id", add.get("goods_id"));
                goodsCarList.add(carInfo);

                goodsIdSet.add(add.get("goods_id").toString());
            }
        }

        if(!modifyDataList.isEmpty()){
            goodsIdSet.addAll(getModifyGoodsIdList());
        }

        sqlGen.handleSql("dataserver_add_goods_car", goodsCarList);
        sqlGen.handleModifySql("dataserver_modify_goods_car", modifyDataList);


        if (goodsIdSet.isEmpty()) {
            Print.info("\n没有需要补充的商品车型关系数据\n");
        }else {
            String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
            writer = IoUtil.getWriter(path + "补充适配车型的商品id-"+dateStr+".txt");
            IoUtil.writeFile(writer, goodsIdSet.toString().replace("[","( ").replace("]", " )"));
            IoUtil.closeWriter(writer);
        }
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
    public List<String> getModifyGoodsIdList(){
        String sql = "select distinct t2.goods_id " +
                "from " +
                "db_goods_car_mini t1, " +
                "(select id,goods_id,car_id from db_goods_car where `status`!=1) t2 " +
                "where t1.goods_id=t2.goods_id and t1.car_id=t2.car_id";

        return commonMapper.selectOneFieldBySql(sql);
    }


    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/商品车型关系数据补充/";
        String excel = path + "云修号YO-6889数据补充-1220.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("car_id", "car_id");
        attrMap.put("goods_id", "goods_id");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);
        for(Map<String, String> add : dataList){
            Map<String, Object> carInfo = sqlGen.getCarInfo(add.get("car_id"));
            if(carInfo==null){
                Print.info("错误的车款id："+add);
            }else{
                carInfo.put("goods_id", add.get("goods_id"));
                goodsCarList.add(carInfo);
            }
        }

        sqlGen.handleSql("add_goods_car", goodsCarList);

    }

    @Test
    public void test_0216() throws Exception{
        path = "/Users/huangzhangting/Desktop/";

        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, String>(){{
            put("car_id", "60667");
            put("goods_id", "393198");
        }});
        dataList.add(new HashMap<String, String>(){{
            put("car_id", "60668");
            put("goods_id", "393198");
        }});

        Print.printList(dataList);


        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        GoodsCarSqlGen sqlGen = new GoodsCarSqlGen(path, commonMapper);
        for(Map<String, String> add : dataList){
            Map<String, Object> carInfo = sqlGen.getCarInfo(add.get("car_id"));
            if(carInfo==null){
                Print.info("错误的车款id："+add);
            }else{
                carInfo.put("goods_id", add.get("goods_id"));
                goodsCarList.add(carInfo);
            }
        }

        sqlGen.handleSql("add_goods_car", goodsCarList);

    }



}
