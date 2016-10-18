package 机油滤清器处理.临时处理;

import base.BaseTest;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import 机油滤清器处理.处理后数据统计.ExcelExporter;
import 机油滤清器处理.处理后数据统计.StatisticConfig;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/18.
 */
public class 商品适配车型匹配 extends BaseTest{

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.car_id", "carId");
        attrMap.put("t1.goods_format", "goodsFormat");
        attrMap.put("t1.brand_name", "goodsBrand");
        attrMap.put("t2.brand", "brand");
        attrMap.put("t2.company", "company");
        attrMap.put("t2.series", "series");
        attrMap.put("t2.model", "model");
        attrMap.put("t2.power", "power");
        attrMap.put("t2.year", "year");
        attrMap.put("t2.name", "carName");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(path + "车型商品关系数据-1018.xls", attrMap.size());
        List<Map<String, String>> coverDataList = readerXLS.getDataList();
        Print.info(coverDataList.size());
        Print.info(coverDataList.get(0));

        String excel = path + "未覆盖车款数据/处理后的/可以补充的奥盛型号-20161010.xlsx";
        attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("品牌", "brand");
        attrMap.put("厂家", "company");
        attrMap.put("车系", "series");
        attrMap.put("车型", "model");
        attrMap.put("排量", "power");
        attrMap.put("年款", "year");
        attrMap.put("车款", "carName");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> aoDataList1 = readerXLSX.getDataList();
        Print.info(aoDataList1.size());
        Print.info(aoDataList1.get(0));

        excel = path + "待处理的数据/奥盛机滤/奥盛可以补充的型号.xls";
        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(excel, attrMap.size());
        List<Map<String, String>> aoDataList2 = readerXLS.getDataList();
        Print.info(aoDataList2.size());
        Print.info(aoDataList2.get(0));

        excel = path + "待处理的数据/箭冠补充数据/最终数据/箭冠可以补充的型号(修订后)-20161009.xlsx";
        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> jgDataList = readerXLSX.getDataList();
        Print.info(jgDataList.size());
        Print.info(jgDataList.get(0));


        attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("完成情况", "status");
        attrMap.put("来源", "source");
        attrMap.put("备注", "remark");

        readerXLSX = new CommReaderXLSX(attrMap);
        excel = path + "临时商品车型关系数据匹配/机油滤清器商品数据与车型关系20161017.xlsx";
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> goodsList = readerXLSX.getDataList();
        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        //奥胜机滤特殊处理
        List<Map<String, String>> asGoodsList = new ArrayList<>();
        //最终车型商品关系数据
        List<Map<String, String>> goodsCarList = new ArrayList<>();
        //没有匹配上的商品
        List<Map<String, String>> goodsList1 = new ArrayList<>();

        List<Map<String, String>> matchGoodsCarList;

        for(Map<String, String> goods : goodsList){
            if("OK".equals(goods.get("status"))){

                if("奥胜".equals(goods.get("goodsBrand"))){
                    asGoodsList.add(goods);
                    continue;
                }

                String goodsFormat = goods.get("goodsFormat");
                matchGoodsCarList = getMatchGoodsCarList(goodsFormat, coverDataList);
                if (matchGoodsCarList.isEmpty()){
                    goodsList1.add(goods);
                }else{
                    //Print.info("匹配上的商品："+goods);
                    handleMatchGoodsCarList(goods, matchGoodsCarList);
                    goodsCarList.addAll(matchGoodsCarList);
                }
            }
        }

        Print.info("匹配上的数据："+goodsCarList.size());
        Print.info("没有匹配上的商品："+goodsList1.size());
        Print.info("奥胜商品："+asGoodsList.size());

        //奥胜型号车型关系数据
        List<Map<String, String>> asGoodsCarList = new ArrayList<>();
        asGoodsCarList.addAll(aoDataList1);
        asGoodsCarList.addAll(aoDataList2);

        //没有匹配上的奥胜商品
        List<Map<String, String>> asGoodsList1 = new ArrayList<>();
        Map<String, String> aoYunRelMap = getAoYunRelMap();
        for(Map<String, String> ag : asGoodsList){
            String goodsFormat = ag.get("goodsFormat");
            String yxFormat = aoYunRelMap.get(goodsFormat);
            if(yxFormat==null){
                //Print.info("没有对应的云修号："+goodsFormat);
                matchGoodsCarList = getMatchGoodsCarList(goodsFormat, asGoodsCarList);
            } else {
                //Print.info("对应云修号："+yxFormat+"  奥胜号："+goodsFormat);
                matchGoodsCarList = getMatchGoodsCarList(yxFormat, coverDataList);
            }
            if(matchGoodsCarList.isEmpty()){
                asGoodsList1.add(ag);
            }else{
                handleMatchGoodsCarList(ag, matchGoodsCarList);
                goodsCarList.addAll(matchGoodsCarList);
            }
        }

        Print.info("匹配上的数据："+goodsCarList.size());
        Print.info("没有匹配上的奥胜商品："+asGoodsList1.size());


        //匹配monkey库导出来的数据
        excel = path + "临时商品车型关系数据匹配/monkey机滤车型关系数据.xls";
        attrMap = new HashMap<>();
        attrMap.put("gc.liyang_Id", "lyId");
        attrMap.put("g.goods_format", "goodsFormat");

        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> lyCarGoodsList = readerXLS.getDataList();
        Print.info(lyCarGoodsList.size());
        Print.info(lyCarGoodsList.get(0));

        //力洋-电商车型关系数据
        List<Map<String, Object>> lyCarRelList = commonMapper.selectListBySql("select new_l_id,brand,company,series,model,power,`year`,car_models,car_models_id from db_car_all");
        Print.info(lyCarRelList.size());
        Print.info(lyCarRelList.get(0));

        //没有匹配上的商品
        List<Map<String, String>> goodsList2 = new ArrayList<>();

        for(Map<String, String> goods : goodsList1){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIds = getLyIdSet(goodsFormat, lyCarGoodsList);
            if(lyIds.isEmpty()){
                goodsList2.add(goods);
            }else{
                Print.info(goodsFormat + "  "+lyIds.size());
                Collection<Map<String, String>> mapCollection = getMatchGoodsCarList(lyIds, lyCarRelList);
                handleMatchGoodsCarList(goods, mapCollection);
                goodsCarList.addAll(mapCollection);
            }
        }

        Print.info("匹配上的数据："+goodsCarList.size());
        Print.info("没有匹配上的商品："+goodsList2.size());


        //处理人工匹配的力洋数据
        excel = path + "临时商品车型关系数据匹配/机油滤清器商品数据与车型关系20161017.xlsx";
        attrMap = new HashMap<>();
        attrMap.put("机油滤型号1", "goodsFormat");
        attrMap.put("id", "lyId");
        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 2);
        List<Map<String, String>> lyIdGoodsList = readerXLSX.getDataList();
        Print.info(lyIdGoodsList.size());
        Print.info(lyIdGoodsList.get(0));

        //没有匹配上的商品
        List<Map<String, String>> goodsList3 = new ArrayList<>();

        for(Map<String, String> goods: goodsList2){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIdSet = getLyIdSet(goodsFormat, lyIdGoodsList);
            if(lyIdSet.isEmpty()){
                goodsList3.add(goods);
            }else{
                Print.info(goodsFormat + "  "+lyIdSet.size());
                Collection<Map<String, String>> mapCollection = getMatchGoodsCarList(lyIdSet, lyCarRelList);
                handleMatchGoodsCarList(goods, mapCollection);
                goodsCarList.addAll(mapCollection);
            }
        }

        Print.info("匹配上的数据："+goodsCarList.size());
        Print.info("没有匹配上的商品："+goodsList3.size());


        //处理没有匹配上的奥胜商品
        for(Map<String, String> goods: asGoodsList1){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIdSet = getLyIdSet(goodsFormat, lyIdGoodsList);
            if(lyIdSet.isEmpty()){
                goodsList3.add(goods);
            }else{
                Print.info(goodsFormat + "  "+lyIdSet.size());
                Collection<Map<String, String>> mapCollection = getMatchGoodsCarList(lyIdSet, lyCarRelList);
                handleMatchGoodsCarList(goods, mapCollection);
                goodsCarList.addAll(mapCollection);
            }
        }

        Print.info("匹配上的数据："+goodsCarList.size());
        Print.info("没有匹配上的商品："+goodsList3.size());

        Print.info(goodsCarList.get(0));


        //导出OK状态下没有匹配上的商品
        String[] heads = new String[]{"产品编码", "品牌", "产品名称", "规格型号", "完成情况", "来源", "备注"};
        String[] fields = new String[]{"goodsSn", "goodsBrand", "goodsName", "goodsFormat", "status", "source", "remark"};

        String excelPath = path + "临时商品车型关系数据匹配/";
        PoiUtil poiUtil = new PoiUtil();
        //poiUtil.exportXlsxWithMap("OK状态下没有匹配上的商品", excelPath, heads, fields, goodsList3);


        heads = new String[]{"产品编码", "品牌", "产品名称", "规格型号", "车品牌", "厂家", "车系", "车型", "排量", "年款", "车款", "id"};
        fields = new String[]{"goodsSn", "goodsBrand", "goodsName", "goodsFormat", "brand", "company", "series", "model", "power", "year", "carName", "carId"};

        Collections.sort(goodsCarList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("goodsBrand")+o1.get("brand")+o1.get("company")+o1.get("model")+o1.get("year")+o1.get("carName");
                String str2 = o2.get("goodsBrand")+o2.get("brand")+o2.get("company")+o2.get("model")+o2.get("year")+o2.get("carName");
                return str1.compareTo(str2);
            }
        });
        poiUtil.exportXlsxWithMap("机滤-车款适配关系", excelPath, heads, fields, goodsCarList);


        /** 验证奥胜数据 */
        /*
        checkAoSheng(asGoodsCarList, getGoodsList("奥胜机滤.xls"));

        checkAoSheng(asGoodsCarList, asGoodsList);

        List<Map<String, String>> needCheckAsGoodsList = new ArrayList<>();

        checkAoSheng(asGoodsCarList, needCheckAsGoodsList);
        */
    }

    private Map<String, String> getAoYunRelMap() throws Exception{
        String excel = "/Users/huangzhangting/Desktop/机滤数据处理/云修机滤奥盛号与云修号对应关系.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("厂家编码", "format");
        attrMap.put("云修号", "goodsFormat");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> relationList = readerXLSX.getDataList();
        Print.info(relationList.size());
        Print.info(relationList.get(0));

        Map<String, String> relMap = new HashMap<>();
        for(Map<String, String> r : relationList){
            String format = r.get("format");
            String goodsFormat = r.get("goodsFormat");
            String str = relMap.get(format);
            if(str==null){
                relMap.put(format, goodsFormat);
            }else{
                Print.info("有疑问的型号："+format+"  "+str+"  "+goodsFormat);
            }
        }
        Print.info(relMap.size());

        return relMap;
    }

    private List<Map<String, String>> getGoodsList(String fileName) throws Exception{
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("new_goods_sn", "goodsSn");
        attrMap.put("goods_name", "goodsName");
        attrMap.put("goods_format", "goodsFormat");

        String excel = path + "电商机滤商品数据/" + fileName;

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.processFirstSheet(excel, attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();

        return goodsList;
    }

    private void checkAoSheng(List<Map<String, String>> goodsCarList, List<Map<String, String>> goodsList) throws Exception{
        Print.info(goodsCarList.size());

        Print.info(goodsList.size());
        Print.info(goodsList.get(0));

        Set<String> newAoFormatSet = new HashSet<>();
        Set<String> oldAoFormatSet = new HashSet<>();

        for(Map<String, String> goodsCar : goodsCarList){
            String goodsFormat = goodsCar.get("goodsFormat");
            boolean flag = true;
            for(Map<String, String> goods : goodsList){
                if(goodsFormat.equals(goods.get("goodsFormat"))){
                    flag = false;
                    break;
                }
            }
            if(flag){
                newAoFormatSet.add(goodsFormat);
            }else{
                oldAoFormatSet.add(goodsFormat);
            }
        }
        Print.info("新奥胜号："+newAoFormatSet.size());
        Print.info(newAoFormatSet);
        Print.info("已上架的奥胜号："+oldAoFormatSet.size());
    }

    //根据型号，匹配车型商品关系数据
    private List<Map<String, String>> getMatchGoodsCarList(String goodsFormat, List<Map<String, String>> goodsCarList){
        List<Map<String, String>> matchGoodsCarList = new ArrayList<>();
        for(Map<String, String> gc : goodsCarList){
            if(StrUtil.toUpCase(goodsFormat).equals(StrUtil.toUpCase(gc.get("goodsFormat")))){
                matchGoodsCarList.add(gc);
            }
        }

        return matchGoodsCarList;
    }

    private void handleMatchGoodsCarList(Map<String, String> goods, Collection<Map<String, String>> matchGoodsCarList){
        for(Map<String, String> gc : matchGoodsCarList){
            gc.put("goodsFormat", goods.get("goodsFormat"));
            gc.put("goodsSn", goods.get("goodsSn"));
            gc.put("goodsBrand", goods.get("goodsBrand"));
            gc.put("goodsName", goods.get("goodsName"));
        }
    }

    //力洋数据相关处理
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



}
