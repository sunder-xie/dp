package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/21.
 */
public class GoodsTest extends BaseTest {
    @Test
    public void justTest() throws Exception{
        Collection<Map<String, String>> collection = getAoShengGoodsCarList();
        Print.info(collection.size());
    }

    //奥胜商品型号-车型关系数据
    private Collection<Map<String, String>> getAoShengGoodsCarList() throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/库存商品数据处理/奥胜商品数据/";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");
        attrMap.put("排量", "power");
        attrMap.put("年款", "year");
        attrMap.put("车款", "carName");

        String excel = filePath + "奥胜机滤可以补充的型号.xls";
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(excel, attrMap.size());
        List<Map<String, String>> mapList1 = readerXLS.getDataList();
        Print.printList(mapList1);

        excel = filePath + "可以补充的奥盛机滤-20161010.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> mapList2 = readerXLSX.getDataList();
        Print.printList(mapList2);

        excel = filePath + "机滤-车款适配关系-20161019.xlsx";
        attrMap.put("规格型号", "goodsFormat");
        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> mapList3 = readerXLSX.getDataList();
        Print.printList(mapList3);


        //开始处理重复的数据
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.addAll(mapList1);
        dataList.addAll(mapList2);
        dataList.addAll(mapList3);

        Map<String, Map<String, String>> keyMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String key = data.get("carId")+"_"+data.get("goodsFormat");
            Map<String, String> map = keyMap.get(key);
            if(map==null){
                keyMap.put(key, data);
            }
        }

        return keyMap.values();
    }

    private Collection<Map<String, String>> getAsMatchGoodsCarList(String asNumStr, String goodsFormat) throws Exception{
        Collection<Map<String, String>> collection = new ArrayList<>();
        if(StringUtils.isEmpty(asNumStr)){
            return collection;
        }
        String[] asNumArray = asNumStr.split("/");
        Set<String> set = new HashSet<>();
        for(String num : asNumArray){
            set.add(num);
        }
        Collection<Map<String, String>> asGoodsCarList = getAoShengGoodsCarList();
        for(Map<String, String> gc : asGoodsCarList){
            if(set.contains(gc.get("goodsFormat"))){
                Map<String, String> map = ObjectUtil.copyStrMap(gc);
                map.put("goodsFormat", goodsFormat);
                collection.add(map);
            }
        }

        Print.info("匹配奥胜数据："+asNumStr+"  "+collection.size());

        return collection;
    }


    //力洋id，车型关系数据
    private List<Map<String, Object>> getLyCarRelList(){
        String sql = "select new_l_id,brand,company,series,model,power,`year`,car_models,car_models_id from db_car_all";
        return commonMapper.selectListBySql(sql);
    }

    private Set<String> getLyIdSet(String goodsFormat, List<Map<String, String>> lyCarGoodsList){
        Set<String> set = new HashSet<>();
        for(Map<String, String> cg : lyCarGoodsList){
            if(StrUtil.toUpCase(goodsFormat).equals(StrUtil.toUpCase(cg.get("goodsFormat")))){
                set.add(cg.get("lyId"));
            }
        }
        return set;
    }

    private Collection<Map<String, String>> getMatchGoodsCarList(Set<String> lyIdSet, List<Map<String, Object>> lyCarRelList){
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

    private void handleMatchGoodsCarList(Map<String, String> goods, Collection<Map<String, String>> matchGoodsCarList){
        for(Map<String, String> gc : matchGoodsCarList){
            gc.put("goodsFormat", goods.get("goodsFormat"));
            gc.put("goodsSn", goods.get("goodsSn"));
            gc.put("goodsBrand", goods.get("goodsBrand"));
            gc.put("goodsName", goods.get("goodsName"));
        }
    }


    //力洋id，车型关系数据
    List<Map<String, Object>> lyIdCarRelList;

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";

        List<Map<String, String>> airFilterList = getGoodsList(2); //空气滤清器
        List<Map<String, String>> airConditionerList = getGoodsList(3); //空调滤清器
        List<Map<String, String>> fuelFilterList = getGoodsList(4); //燃油滤清器

        //商品库力洋id数据
        List<Map<String, String>> airFilterLyIdList = getLyCarGoodsList("力洋id空气滤清器数据.xls");
        List<Map<String, String>> airConditionerLyIdList = getLyCarGoodsList("力洋id空调滤清器数据.xls");
        List<Map<String, String>> fuelFilterLyIdList = getLyCarGoodsList("力洋id燃油滤清器数据.xls");

        //人工匹配：力洋id
        List<Map<String, String>> airFilterLyIdList2 = getLyIdFormatList(1);
        airFilterLyIdList.addAll(airFilterLyIdList2);

        List<Map<String, String>> airConditionerLyIdList2 = getLyIdFormatList(2);
        airConditionerLyIdList.addAll(airConditionerLyIdList2);

        List<Map<String, String>> fuelFilterLyIdList2 = getLyIdFormatList(3);
        fuelFilterLyIdList.addAll(fuelFilterLyIdList2);


        //力洋id，车型关系数据
        lyIdCarRelList = getLyCarRelList();

        //开始处理数据
        compareGoods("空气滤清器", airFilterList, airFilterLyIdList);

        compareGoods("空调滤清器", airConditionerList, airConditionerLyIdList);

        compareGoods("燃油滤清器", fuelFilterList, fuelFilterLyIdList);
    }

    private void compareGoods(String goodsType, List<Map<String, String>> goodsList, List<Map<String, String>> lyIdGoodsList) throws Exception{
        Print.info("\n开始处理========== "+goodsType);

        List<Map<String, String>> unMatchGoodsList = new ArrayList<>();
        List<Map<String, String>> goodsCarList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            if("OK".equals(goods.get("status"))){
                String source = goods.get("source");
                String goodsFormat = goods.get("goodsFormat");
                if("保险".equals(source)){
                    String aoShengNum = goods.get("aoShengNum"); //奥胜商品号
                    Collection<Map<String, String>> matchGoodsCarList = getAsMatchGoodsCarList(aoShengNum, goodsFormat);
                    if(matchGoodsCarList.isEmpty()){
                        unMatchGoodsList.add(goods);
                    }else{
                        handleMatchGoodsCarList(goods, matchGoodsCarList);
                        goodsCarList.addAll(matchGoodsCarList);
                    }
                }else{
                    Set<String> lyIdSet = getLyIdSet(goodsFormat, lyIdGoodsList);
                    if(lyIdSet.isEmpty()){
                        unMatchGoodsList.add(goods);
                    }else{
                        Collection<Map<String, String>> matchGoodsCarList = getMatchGoodsCarList(lyIdSet, lyIdCarRelList);
                        handleMatchGoodsCarList(goods, matchGoodsCarList);
                        goodsCarList.addAll(matchGoodsCarList);
                    }
                }
            }
        }
        Print.info("ok状态下没有匹配上的数据："+unMatchGoodsList.size());
        Print.info("车型商品关系数据："+goodsCarList.size());

        Collections.sort(goodsCarList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("goodsBrand")+o1.get("brand")+o1.get("company")+o1.get("model")+o1.get("year")+o1.get("carName");
                String str2 = o2.get("goodsBrand")+o2.get("brand")+o2.get("company")+o2.get("model")+o2.get("year")+o2.get("carName");
                return str1.compareTo(str2);
            }
        });

        //处理一对多的数据
        Set<String> set = new HashSet<>();
        Set<String> repeatSet = new HashSet<>();
        for(Map<String, String> gc : goodsCarList){
            String key = gc.get("carId")+"_"+gc.get("goodsBrand");
            if(!set.add(key)){
                repeatSet.add(key);
            }
        }

        List<Map<String, String>> oneGoodsCarList = new ArrayList<>();
        List<Map<String, String>> multiGoodsCarList = new ArrayList<>();
        for(Map<String, String> gc : goodsCarList){
            String key = gc.get("carId")+"_"+gc.get("goodsBrand");
            if(repeatSet.contains(key)){
                multiGoodsCarList.add(gc);
            }else{
                oneGoodsCarList.add(gc);
            }
        }


        //导出OK状态下没有匹配上的商品
        String[] heads = new String[]{"产品编码", "产品品牌", "产品名称", "规格型号", "完成情况", "来源", "备注", "奥盛号码"};
        String[] fields = new String[]{"goodsSn", "goodsBrand", "goodsName", "goodsFormat", "status", "source", "remark", "aoShengNum"};

        String excelPath = path + "处理后/"+goodsType+"/";
        IoUtil.mkdirsIfNotExist(excelPath);

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap(goodsType+"OK状态下没有匹配上的商品", excelPath, heads, fields, unMatchGoodsList);


        //导出商品车型关系数据
        heads = new String[]{"产品编码", "产品品牌", "产品名称", "规格型号", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款", "id"};
        fields = new String[]{"goodsSn", "goodsBrand", "goodsName", "goodsFormat", "brand", "company", "series", "model", "power", "year", "carName", "carId"};

        poiUtil.exportXlsxWithMap(goodsType+"-车款适配关系", excelPath, heads, fields, oneGoodsCarList);

        poiUtil.exportXlsxWithMap(goodsType+"-车款适配关系(一对多)", excelPath, heads, fields, multiGoodsCarList);

    }

    private List<Map<String, String>> getGoodsList(int sheet) throws Exception{
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("商品规格", "goodsFormat");
        attrMap.put("完成情况", "status");
        attrMap.put("备注", "remark");
        attrMap.put("来源", "source");
        attrMap.put("奥盛号码", "aoShengNum");

        String excel = path + "0级别商品整理表整理表20161010.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, sheet);
        List<Map<String, String>> mapList = readerXLSX.getDataList();
        Print.printList(mapList);

        return mapList;
    }

    private List<Map<String, String>> getLyIdFormatList(int sheet) throws Exception{
        String excel = path + "库存商品与力洋ID关系.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("id", "lyId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, sheet);
        List<Map<String, String>> mapList = readerXLSX.getDataList();
        Print.printList(mapList);

        return mapList;
    }

    private List<Map<String, String>> getLyCarGoodsList(String fileName) throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/库存商品数据处理/monkey商品库/";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("gc.liyang_Id", "lyId");
        attrMap.put("g.goods_format", "goodsFormat");
        attrMap.put("g.goods_name", "goodsName");
        attrMap.put("g.brand_name", "goodsBrand");
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());
        List<Map<String, String>> mapList = readerXLS.getDataList();
        Print.printList(mapList);

        return mapList;
    }

}
