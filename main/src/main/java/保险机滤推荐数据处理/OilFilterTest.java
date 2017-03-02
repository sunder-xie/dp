package 保险机滤推荐数据处理;

import base.BaseTest;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 17/1/18.
 */
public class OilFilterTest extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤推荐/数据/";
        String excel = path + "vin码-车型-商品id.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.vin", "vin");
        attrMap.put("t1.car_models_id", "carId");
        attrMap.put("t2.goods_id", "goodsId");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        //机滤信息
        List<Map<String, Object>> oilFilterList = getOilFilterList();
        Print.printList(oilFilterList);

        //配置过的机滤sn
        List<String> oilFilterSnList = getOilFilterSnList();
        Print.printList(oilFilterSnList);

        //全部车款数据
        List<Map<String, Object>> carList = getAllCarList();


        List<Map<String, String>> matchGoodsList = new ArrayList<>();
        //数据匹配
        for(Map<String, String> data : dataList){
            String goodsId = data.get("goodsId");
            for(Map<String, Object> oilFilter : oilFilterList){
                if(goodsId.equals(oilFilter.get("goods_id").toString())){
                    data.put("goodsSn", oilFilter.get("new_goods_sn").toString());
                    data.put("goodsName", oilFilter.get("goods_name").toString());
                    Print.info("有适配的机滤："+data+" "+oilFilter);

                    setCarInfo(data, carList);
                    matchGoodsList.add(data);
                    break;
                }
            }
            String goodsSn = data.get("goodsSn");
            if(goodsSn != null){
                if(oilFilterSnList.contains(goodsSn)){
                    data.put("hasConfig", "是");
                    Print.info("机滤已配置："+data);
                }
            }
        }


        //设置记录创建时间
        setGoodsCarAddTime(matchGoodsList);


        //导出excel
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"vin码", "车款id", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款",
                "商品id", "商品编码", "商品名称", "保险系统是否已配置", "创建时间"};
        String[] fields = new String[]{"vin", "carId", "brand", "company", "series", "model", "power", "year", "name",
                "goodsId", "goodsSn", "goodsName", "hasConfig", "gmtCreate"};

        poiUtil.exportXlsxWithMap("vin码-机滤匹配情况", path, heads, fields, matchGoodsList);

    }

    private List<Map<String, Object>> getOilFilterList(){
        String sql = "select goods_id,new_goods_sn,goods_name from db_goods where cat_id=4343 and is_delete=0";
        return commonMapper.selectListBySql(sql);
    }

    private List<String> getOilFilterSnList(){
        String sql = "select distinct goods_sn from insurance_package_material_pool";
        return commonMapper.selectOneFieldBySql(sql);
    }

    private List<Map<String, Object>> getAllCarList(){
        String sql = "select * from db_car_category where level=6";
        return commonMapper.selectListBySql(sql);
    }

    private void setCarInfo(Map<String, String> data, List<Map<String, Object>> carList){
        String carId = data.get("carId");
        for(Map<String, Object> car : carList){
            if(carId.equals(car.get("id").toString())){
                data.put("brand", car.get("brand").toString());
                data.put("company", car.get("company").toString());
                data.put("series", car.get("series").toString());
                data.put("model", car.get("model").toString());
                data.put("power", car.get("power").toString());
                data.put("year", car.get("year").toString());
                data.put("name", car.get("name").toString());
                break;
            }
        }
    }

    private String getGmtCreate(String goodsId, String carId){
        String sql = "select gmt_create from db_goods_car where car_id="+carId+" and goods_id="+goodsId;
        List<String> list = commonMapper.selectOneFieldBySql(sql);
        if(list.isEmpty()){
            return "";
        }
        return list.get(0);
    }

    private void setGoodsCarAddTime(List<Map<String, String>> goodsCarList){
        for(Map<String, String> gc : goodsCarList){
            gc.put("gmtCreate", getGmtCreate(gc.get("goodsId"), gc.get("carId")));
        }
    }

}
