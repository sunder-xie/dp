package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import 机油滤清器处理.生成sql脚本.GoodsCarSqlGen;

import java.io.File;
import java.util.*;

/**
 * Created by huangzhangting on 16/11/10.
 */
public class GoodsCarTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        String excelPath = path + "关系数据/";
        File directory = new File(excelPath);

        String[] fileNames = directory.list();
        Print.info(fileNames.length);
        Map<String, Map<String, String>> dataMap = new HashMap<>();
        for(String name : fileNames){
//            Print.info(excelPath + name);
            List<Map<String, String>> dataList = getGoodsCarList(excelPath + name);
            handleDataList(dataMap, dataList);
        }

        /**/
        String excel = path + "2级别商品与电商车型20161104.xlsx";
        for(int j=1; j<7; j++){
            List<Map<String, String>> dataList = getGoodsCarList(excel, j);
            handleDataList(dataMap, dataList);
        }

        List<Map<String, String>> carIdGoodsSnList = new ArrayList<>(dataMap.values());
        Print.printList(carIdGoodsSnList);

        List<Map<String, Object>> goodsList = getGoodsList();
        Print.printList(goodsList);

        Print.info("==========开始处理goods id==========");
        int size = carIdGoodsSnList.size();
        for(int i=0; i<size; i++){
            Map<String, String> carSn = carIdGoodsSnList.get(i);
            String goodsId = getGoodsId(carSn.get("goodsSn"), goodsList);
            if(goodsId==null){
                Print.info("存在错误的goodsSn："+carSn);
                carIdGoodsSnList.remove(i);
                i--;
                size--;
            }else{
                carSn.put("goodsId", goodsId);
            }
        }

        Print.printList(carIdGoodsSnList);

        //批量插入 db_goods_car_mini
        batchInsertGoodsCarMini(carIdGoodsSnList);

    }

    public List<Map<String, String>> getGoodsCarList(String excel) throws Exception{
        return getGoodsCarList(excel, 1);
    }
    public List<Map<String, String>> getGoodsCarList(String excel, int sheet) throws Exception{
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("id", "carId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, sheet);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        return dataList;
    }

    public void handleDataList(Map<String, Map<String, String>> dataMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String key = data.get("carId")+"_"+data.get("goodsSn");
            Map<String, String> map = dataMap.get(key);
            if(map==null){
                dataMap.put(key, data);
            }
        }
    }

    public List<Map<String, Object>> getGoodsList(){
        String sql = "select goods_id,new_goods_sn from db_goods where is_delete=0";
        return commonMapper.selectListBySql(sql);
    }

    public String getGoodsId(String goodsSn, List<Map<String, Object>> goodsList){
        for(Map<String, Object> goods : goodsList){
            String sn = goods.get("new_goods_sn").toString();
            if(goodsSn.equals(sn)){
                return goods.get("goods_id").toString();
            }
        }
        return null;
    }

    public void batchInsertGoodsCarMini(List<Map<String, String>> dataList){
        writer = IoUtil.getWriter(path + "db_goods_car_mini.sql");
        int size = dataList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            appendGoodsCarMiniVal(sql, dataList.get(i));
            if((i+1)%1000==0){
                writeGoodsCarMiniSql(sql);
                sql.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeGoodsCarMiniSql(sql);
                break;
            }
            sql.append(",");
        }
    }
    public void appendGoodsCarMiniVal(StringBuilder sql, Map<String, String> data){
        sql.append("(");
        sql.append(data.get("goodsId"));
        sql.append(",");
        sql.append(data.get("carId"));
        sql.append(")");
    }
    public void writeGoodsCarMiniSql(StringBuilder sql){
        sql.insert(0, "insert ignore into db_goods_car_mini(goods_id, car_id) values");
        sql.append(";\n");
        IoUtil.writeFile(writer, sql.toString());
    }


    //生成db_goods_car的sql
    @Test
    public void testGC() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/检查完成后的结果/";

        List<Map<String, Object>> addDataList = getAddDataList();
        Print.printList(addDataList);

        List<String> modifyDataList = getModifyDataList();
        Print.printList(modifyDataList);


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
