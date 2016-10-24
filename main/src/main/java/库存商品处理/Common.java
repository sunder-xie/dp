package 库存商品处理;

import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.PoiUtil;
import dp.dao.mapper.CommonMapper;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/21.
 */
public class Common {
    private CommonMapper commonMapper;
    private List<Map<String, Object>> lyCarRelList;

    public Common(CommonMapper commonMapper) {
        this.commonMapper = commonMapper;
        lyCarRelList = getLyCarRelList();
    }

    //力洋id，车型关系数据
    public List<Map<String, Object>> getLyCarRelList(){
        String sql = "select new_l_id,brand,company,series,model,power,`year`,car_models,car_models_id from db_car_all";
        return commonMapper.selectListBySql(sql);
    }

    public boolean compareGoodsFormat(String goodsFormatStr, String goodsFormat){
        goodsFormatStr = StrUtil.toUpCase(goodsFormatStr);
        goodsFormat = StrUtil.toUpCase(goodsFormat);

        if(goodsFormatStr.equals(goodsFormat)){
            return true;
        }
        String[] gfs = goodsFormatStr.split("/");
        for(String str : gfs){
            if(str.equals(goodsFormat)){
                return true;
            }
        }
        return false;
    }

    public Set<String> getLyIdSet(String goodsFormat, List<Map<String, String>> lyCarGoodsList){
        Set<String> set = new HashSet<>();
        for(Map<String, String> cg : lyCarGoodsList){
            if(compareGoodsFormat(goodsFormat, cg.get("goodsFormat"))){
                set.add(cg.get("lyId"));
            }
        }
        return set;
    }

    public Collection<Map<String, String>> getMatchGoodsCarList(Set<String> lyIdSet){
        Map<String, Map<String, String>> carIdMap = new HashMap<>();
        for(Map<String, Object> car : lyCarRelList){
            String lyId = car.get("new_l_id").toString();
            if(lyIdSet.contains(lyId)){
                String carId = car.get("car_models_id").toString();
                Map<String, String> carMap = carIdMap.get(carId);
                if(carMap==null){
                    carMap = ObjectUtil.objToStrMap(car);
                    carMap.put("carId", carMap.get("car_models_id"));
                    carMap.put("carName", carMap.get("car_models"));
                    carIdMap.put(carId, carMap);
                }
            }

        }

        return carIdMap.values();
    }

    public void handleMatchGoodsCarList(Map<String, String> goods, Collection<Map<String, String>> matchGoodsCarList){
        for(Map<String, String> gc : matchGoodsCarList){
            gc.put("goodsFormat", goods.get("goodsFormat"));
            gc.put("goodsSn", goods.get("goodsSn"));
            gc.put("goodsBrand", goods.get("goodsBrand"));
            gc.put("goodsName", goods.get("goodsName"));
        }
    }

    //数据导出相关
    public void sortGoodsCarList(List<Map<String, String>> goodsCarList){
        Collections.sort(goodsCarList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("goodsBrand")+o1.get("brand")+o1.get("company")+o1.get("model")+o1.get("year")+o1.get("carName");
                String str2 = o2.get("goodsBrand")+o2.get("brand")+o2.get("company")+o2.get("model")+o2.get("year")+o2.get("carName");
                return str1.compareTo(str2);
            }
        });
    }
    public String[] getExcelHeads(){
        return new String[]{"产品编码", "产品品牌", "产品名称", "规格型号", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款", "id"};
    }
    public String[] getDataFields(){
        return new String[]{"goodsSn", "goodsBrand", "goodsName", "goodsFormat", "brand", "company", "series", "model", "power", "year", "carName", "carId"};
    }

    public void exportGoodsCarExcel(String fileName, String filePath, List<Map<String, String>> goodsCarList){
        sortGoodsCarList(goodsCarList);
        String[] heads = getExcelHeads();
        String[] fields =getDataFields();
        PoiUtil poiUtil = new PoiUtil();
        try {
            poiUtil.exportXlsxWithMap(fileName, filePath, heads, fields, goodsCarList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关系数据匹配
    public void handleGoodsCar(List<Map<String, String>> goodsList, List<Map<String, String>> lyIdGoodsList,
                             String fileName, String filePath){
        List<Map<String, String>> goodsCarList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIdSet = getLyIdSet(goodsFormat, lyIdGoodsList);
            if(!lyIdSet.isEmpty()){
                Collection<Map<String, String>> matchGoodsCarList = getMatchGoodsCarList(lyIdSet);
                handleMatchGoodsCarList(goods, matchGoodsCarList);
                goodsCarList.addAll(matchGoodsCarList);
            }
        }
        Print.info("\n========== 需要处理的数据 ==========");
        Print.printList(goodsCarList);
        Print.info("");

        exportGoodsCarExcel(fileName, filePath, goodsCarList);
    }

}
