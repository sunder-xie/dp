package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.dao.mapper.CommonMapper;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/10.
 */
public class GoodsCarCommon extends BaseTest {
    public GoodsCarCommon() {
    }

    public GoodsCarCommon(CommonMapper commonMapper, String path) {
        this.commonMapper = commonMapper;
        this.path = path;
    }

    public void handleCarIdGoodsSnList(Collection<Map<String, String>> mapCollection){
        List<Map<String, String>> carIdGoodsSnList = new ArrayList<>(mapCollection);
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
        Print.info(path);
        writer = IoUtil.getWriter(path + "db_goods_car_mini.sql");
        IoUtil.writeFile(writer, "truncate table db_goods_car_mini;\n");

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

}
