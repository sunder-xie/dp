package temporary.云修;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/9/22.
 */
@Deprecated
public class 机滤数据处理 extends BaseTest {
    private static final int YX_CODE = 1; //云修机滤
    private static final int BS_CODE = 2; //博世机滤
    private static final int AC_CODE = 3; //AC德科机滤
    private static final int JG_CODE = 4; //箭冠机滤


    private Set<String> CAR_ID_SET;


    @Test
    public void just_test() throws Exception{
        handleAsGoods();
    }


    private String getSql(int code){
        String sql = "select tb2.car_models_id,tb2.goods_format " +
                "from " +
                "(select tt1.car_models_id,count(1) " +
                "from " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + code +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id  " +
                "having count(1)=1) tb1, " +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + code +
                " group by t2.car_models_id,t1.goods_format) tb2, db_car_category c " +
                "where tb1.car_models_id=tb2.car_models_id " +
                "and tb2.car_models_id=c.id and c.power!='电动' and c.name not like '%柴油%' ";
        
        return sql;
    }


    private List<String> getGoodsFormat(int code){
        String sql = "select distinct goods_format from temp_goods_lyid_rel where brand_code="+code;
        return commonMapper.selectOneFieldBySql(sql);
    }


    //添加云修商品型号
    private void addYxGoods(List<String> yxGoodsFormats, List<Map<String, String>> yxAddDataList){
        for(Map<String, String> data : yxAddDataList){
            String goodsFormat = data.get("goods_format");
            if(!yxGoodsFormats.contains(goodsFormat)){
                yxGoodsFormats.add(goodsFormat);
            }
        }
    }

    //修订后的数据
    private List<Map<String, String>> getCoverCarGoodsList() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("备注", "remark");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(path + "机滤覆盖车型信息处理汇总表.xlsx", 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        List<Map<String, String>> resultList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            if(!StringUtils.isEmpty(data.get("remark"))){
                resultList.add(data);
            }
        }
        Print.info(resultList.size());
        Print.info(resultList.get(0));

        return resultList;
    }

    private List<Map<String, Object>> grepDataList(List<Map<String, String>> checkedList, List<Map<String, Object>> dataList){
        List<Map<String, Object>> list = new ArrayList<>();
        for(Map<String, Object> data : dataList){
            String carId = data.get("car_models_id").toString();
            String goodsFormat = data.get("goods_format").toString();
            boolean flag = true;
            for(Map<String, String> cd : checkedList){
                if(carId.equals(cd.get("carId")) && goodsFormat.equals(cd.get("goodsFormat"))){
                    flag = false;
                    break;
                }
            }
            if(flag){
                list.add(data);
            }
        }

        return list;
    }


    @Test
    public void test() throws Exception{

        CAR_ID_SET = new HashSet<>();

        path = "/Users/huangzhangting/Desktop/机滤数据处理/商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "car_models_id");
        attrMap.put("商品编码", "goods_format");
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(path + "机滤覆盖的车款信息-增加-20160929.xlsx", 1);
        List<Map<String, String>> yxAddDataList = readerXLSX.getDataList();
        Print.info(yxAddDataList.size());
        Print.info(yxAddDataList.get(0));

        List<String> yxGoodsFormat = getGoodsFormat(YX_CODE);
        Print.info(yxGoodsFormat.size());
        addYxGoods(yxGoodsFormat, yxAddDataList);
        Print.info(yxGoodsFormat.size());

        //验证过的数据
        List<Map<String, String>> checkedList = getCoverCarGoodsList();

        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> yxDataList = commonMapper.selectListBySql(getSql(YX_CODE)); //云修机滤
        Print.info(yxDataList.size());
        yxDataList.addAll(ObjectUtil.strToObjMapList(yxAddDataList));
        Print.info(yxDataList.size());

        yxDataList = grepDataList(checkedList, yxDataList);
        Print.info("验证后的云修覆盖量："+yxDataList.size());

        List<Map<String, Object>> bsDataList = commonMapper.selectListBySql(getSql(BS_CODE)); //博世机滤
        Print.info(bsDataList.size());
        bsDataList = grepDataList(checkedList, bsDataList);
        Print.info("验证后的博世覆盖量："+bsDataList.size());

        List<Map<String, Object>> acDataList = commonMapper.selectListBySql(getSql(AC_CODE)); //AC德科机滤
        Print.info(acDataList.size());
        acDataList = grepDataList(checkedList, acDataList);
        Print.info("验证后的AC德科覆盖量："+acDataList.size());

        dataList.addAll(yxDataList);
        dataList.addAll(bsDataList);
        dataList.addAll(acDataList);

        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Map<Integer, List<String>> carIdMap = new HashMap<>();
        for(Map<String, Object> data : dataList){
            Integer carId = Integer.valueOf(data.get("car_models_id").toString());
            List<String> gfList = carIdMap.get(carId);
            if(gfList==null){
                gfList = new ArrayList<>();
                carIdMap.put(carId, gfList);
            }
            gfList.add(data.get("goods_format").toString());

            //统计覆盖的车款
            CAR_ID_SET.add(carId.toString());
        }
        //Print.info(carIdMap.size());

        List<Map<String, Object>> list = new ArrayList<>();
        List<Integer> carIdList = new ArrayList<>();
        Set<String> goodsFormatSet = new HashSet<>();
        for(Map.Entry<Integer, List<String>> entry : carIdMap.entrySet()){
            if(entry.getValue().size()>1){
                //Print.info(entry.getKey()+"  "+entry.getValue());
                checkGoodsFormat(yxGoodsFormat, entry.getValue(), yxDataList, bsDataList, acDataList,
                        goodsFormatSet);

//                Map<String, Object> car = getCar(entry.getKey());
//                if(car!=null){
//                    for(String str : entry.getValue()){
//                        Map<String, Object> map = ObjectUtil.copyMap(car);
//                        map.put("goodsFormat", str);
//                        list.add(map);
//                    }
//                    carIdList.add(entry.getKey());
//                }
            }
        }

        Print.info("最终可以删除的非云修机滤："+goodsFormatSet);

//        Print.info("车款id："+carIdList.size());
//        Print.info("关系数据："+list.size());
//        Print.info(list.get(0));
//        Print.info(list.get(1));

        //导出excel
//        PoiUtil poiUtil = new PoiUtil();
//        String[] heads = new String[]{"车款id", "商品编号", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
//        String[] fields = new String[]{"id", "goodsFormat", "brand", "company", "series", "model", "power", "year", "name"};
//
//        path = "/Users/huangzhangting/Desktop/安心保险数据对接/云修机滤数据处理/数据校验/";
//
//        poiUtil.exportXlsxWithMap("对应多个不同品牌号的车款", path, heads, fields, poiUtil.convert(list));


        Print.info("覆盖车款id："+CAR_ID_SET.size());


        Set<String> bsFormat = new HashSet<>();
        for(Map<String, Object> data : bsDataList){
            String format = data.get("goods_format").toString();
            if(!goodsFormatSet.contains(format)){
                bsFormat.add(format);
            }
        }
        Print.info("博世型号："+bsFormat);

        Set<String> acFormat = new HashSet<>();
        for(Map<String, Object> data : acDataList){
            String format = data.get("goods_format").toString();
            if(!goodsFormatSet.contains(format)){
                acFormat.add(format);
            }
        }
        Print.info("AC德科型号："+acFormat);

        Set<String> bwFormat = getGoodsFormatSet("豹王机滤可补充的数据.xls");
        Set<String> mlFormat = getGoodsFormatSet("马勒机滤可补充的数据.xls");
        Set<String> hyFormat = getGoodsFormatSet("海业机滤可补充的数据.xls");

        List<Map<String, String>> yxGoodsList = getGoodsDataList("云修机油滤清器.xls");
        List<Map<String, String>> bsGoodsList = getGoodsDataList("博世机油滤清器.xls");
        List<Map<String, String>> acGoodsList = getGoodsDataList("AC德科机油滤清器.xls");
        List<Map<String, String>> bwGoodsList = getGoodsDataList("豹王机滤.xls");
        List<Map<String, String>> mlGoodsList = getGoodsDataList("马勒机滤.xls");
        List<Map<String, String>> hyGoodsList = getGoodsDataList("海业机滤.xls");

        //最终的数据
        List<Map<String, String>> goodsDataList = new ArrayList<>();

        Set<String> yxFormat = new HashSet<>(yxGoodsFormat);

        handleGoodsDataList(yxFormat, yxGoodsList, "云修", goodsDataList);
        Print.info(goodsDataList.size());

        handleGoodsDataList(bsFormat, bsGoodsList, "博世", goodsDataList);
        Print.info(goodsDataList.size());

        handleGoodsDataList(acFormat, acGoodsList, "AC德科", goodsDataList);
        Print.info(goodsDataList.size());

        handleGoodsDataList(bwFormat, bwGoodsList, "豹王", goodsDataList);
        Print.info(goodsDataList.size());

        handleGoodsDataList(mlFormat, mlGoodsList, "马勒", goodsDataList);
        Print.info(goodsDataList.size());

        handleGoodsDataList(hyFormat, hyGoodsList, "海业", goodsDataList);
        Print.info(goodsDataList.size());


        Print.info("覆盖车款id："+CAR_ID_SET.size());

        //处理箭冠数据
        goodsDataList.addAll(handleJgGoods());
        Print.info(goodsDataList.size());

        //处理奥盛数据
        goodsDataList.addAll(handleAsGoods());
        Print.info(goodsDataList.size());


        path = "/Users/huangzhangting/Desktop/机滤数据处理/";
        //导出excel
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"品牌", "商品型号", "商品sn"};
        String[] fields = new String[]{"brand", "goodsFormat", "goodsSn"};

        poiUtil.exportXlsxWithMap("可用的机滤型号信息", path, heads, fields, goodsDataList);


        //统计未覆盖车款信息
        List<Map<String, Object>> needCheckCars = getNeedCheckCars();
        Print.info(needCheckCars.size());
        Print.info(needCheckCars.get(0));

        List<Map<String, Object>> needCheckCarsTrue = new ArrayList<>();
        for(Map<String, Object> car : needCheckCars){
            String id = car.get("id").toString();
            if(!CAR_ID_SET.contains(id)){
                needCheckCarsTrue.add(car);
            }
        }

        int size1 = CAR_ID_SET.size();
        Print.info("覆盖车款统计："+size1);

        int size2 = needCheckCarsTrue.size();
        Print.info("未覆盖车款统计："+size2);

        Print.info("覆盖率："+(size1*1.0)/(size1+size2));

        path += "未覆盖车款数据/";

        poiUtil = new PoiUtil();
        heads = new String[]{"id", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        fields = new String[]{"id", "brand", "company", "series", "model", "power", "year", "name"};

        List<Map<String, String>> mapList = ObjectUtil.objToStrMapList(needCheckCarsTrue);

        poiUtil.exportXlsxWithMap("机滤未覆盖的车款", path, heads, fields, mapList);

    }

    //统计未覆盖车款信息
    private List<Map<String, Object>> getNeedCheckCars(){
        String sql = "select id,brand,company,series,model,power,`year`,`name` " +
                "from db_car_category where level=6 " +
                "and power!='电动' and `name` not like '%柴油%' and `year`>='2005' " +
                "and brand not in( " +
                "'阿尔法-罗密欧','阿斯顿马丁','安驰','Alpina','巴博斯', " +
                "'宝龙','保斐利','宾利','布加迪','宝沃','大发','大宇', " +
                "'法拉利','富奇','GMC','光冈','海格','悍马','黑豹','华北', " +
                "'黄海','华阳','恒天','华颂','九龙','金程','卡尔森', " +
                "'科尼赛克','卡威','凯翼','兰博基尼','劳伦士','劳斯莱斯', " +
                "'路特斯','罗孚','玛莎拉蒂','迈巴赫','美亚','迈凯伦', " +
                "'帕加尼','庞蒂克','启腾','RUF','SPRINGO','Scion','萨博','世爵', " +
                "'赛宝','通田','特斯拉','威兹曼','西雅特','新凯','云雀','知豆' " +
                ") order by brand,company,series,model,power,`year`,`name` ";

        return commonMapper.selectListBySql(sql);
    }

    //处理验证后的箭冠数据
    private void checkJgCarIds(Set<String> carIdSet) throws Exception{

        path = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("错误说明", "errorDesc");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path+"箭冠可以补充的机滤(一个车款一个机滤)修改汇总.xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        for(Map<String, String> data : oToDataList){
            String errorDesc = data.get("errorDesc");
            if(!StringUtils.isEmpty(errorDesc)){
                String carId = data.get("carId");
                carIdSet.remove(carId);
            }
        }
    }

    private List<Map<String, String>> handleJgGoods() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/箭冠补充数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path+"箭冠可以补充的机滤(一对一).xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(path+"箭冠可以补充的机滤(一对多)-处理后.xlsx", 1);
        List<Map<String, String>> oTmDataList = readerXLSX.getDataList();
        Print.info(oTmDataList.size());
        Print.info(oTmDataList.get(0));

        Set<String> carIdSet = new HashSet<>();
        Set<String> repeatCarIds = new HashSet<>();
        for(Map<String, String> oData : oToDataList){
            String carId = oData.get("carId");
            if(!carIdSet.add(carId)){
                //Print.info("重复的车款id："+carId);

                repeatCarIds.add(carId);
            }
        }
        Print.info("覆盖车款id："+carIdSet.size());

        Print.info("===== 开始处理一对多数据 =====");
        for(Map<String, String> mData : oTmDataList){
            String carId = mData.get("carId");
            if(!carIdSet.add(carId)){
                //Print.info("重复的车款id："+carId);

                repeatCarIds.add(carId);
            }
        }

        carIdSet.removeAll(repeatCarIds);

        Print.info("覆盖车款id："+carIdSet.size());

        //去除掉已覆盖的车款id
        carIdSet.removeAll(CAR_ID_SET);

        Print.info("覆盖车款id："+carIdSet.size());
        checkJgCarIds(carIdSet);
        Print.info("箭冠覆盖车款id："+carIdSet.size());

        //统计全部覆盖车款
        CAR_ID_SET.addAll(carIdSet);


        Set<String> goodsFormats = new HashSet<>();
        for(Map<String, String> oData : oToDataList){
            String carId = oData.get("carId");
            if(carIdSet.contains(carId)){
                goodsFormats.add(oData.get("goodsFormat"));

            }
        }
        for(Map<String, String> mData : oTmDataList){
            String carId = mData.get("carId");
            if(carIdSet.contains(carId)){
                goodsFormats.add(mData.get("goodsFormat"));

            }
        }

        List<Map<String, String>> goodsDataList = new ArrayList<>();
        for(String gf : goodsFormats){
            Map<String, String> map = new HashMap<>();
            map.put("brand", "箭冠");
            map.put("goodsFormat", gf);
            map.put("goodsSn", "待补充");

            goodsDataList.add(map);
        }

        return goodsDataList;
    }

    //处理奥盛数据
    private List<Map<String, String>> handleAsGoods() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/奥盛机滤/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path+"奥盛可以补充的型号.xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        Set<String> carIdSet = new HashSet<>();
        //Set<String> repeatCarIds = new HashSet<>();
        for(Map<String, String> oData : oToDataList){
            String carId = oData.get("carId");
            carIdSet.add(carId);

//            if(!carIdSet.add(carId)){
//                Print.info("重复的车款id："+carId);
//
//                repeatCarIds.add(carId);
//            }
        }
        Print.info("覆盖车款id："+carIdSet.size());

        carIdSet.removeAll(CAR_ID_SET);

        Print.info("覆盖车款id："+carIdSet.size());


        //统计全部覆盖车款
        CAR_ID_SET.addAll(carIdSet);


        Set<String> goodsFormats = new HashSet<>();
        for(Map<String, String> oData : oToDataList){
            String carId = oData.get("carId");
            if(carIdSet.contains(carId)){
                goodsFormats.add(oData.get("goodsFormat"));

            }
        }

        List<Map<String, String>> goodsDataList = new ArrayList<>();
        for(String gf : goodsFormats){
            Map<String, String> map = new HashMap<>();
            map.put("brand", "奥盛");
            map.put("goodsFormat", gf);
            map.put("goodsSn", "待补充");

            goodsDataList.add(map);
        }

        return goodsDataList;
    }


    private void handleGoodsDataList(Set<String> formatSet, List<Map<String, String>> goodsList,
                                     String brand, List<Map<String, String>> goodsDataList){

        for(String format : formatSet){
            boolean flag = true;
            for(Map<String, String> goods : goodsList){
                String gf = goods.get("goodsFormat");
                if(format.replace(" ", "").equals(gf.replace(" ", ""))){
                    Map<String, String> map = new HashMap<>();
                    map.put("brand", brand);
                    map.put("goodsFormat", gf);
                    map.put("goodsSn", goods.get("goodsSn"));
                    goodsDataList.add(map);

                    flag = false;
                    break;
                }
            }
            if(flag){
                Print.info(brand+"，没有goodsSn的型号："+format);
            }
        }

    }

    private List<Map<String, String>> getGoodsDataList(String fileName) throws Exception{

        path = "/Users/huangzhangting/Desktop/机滤数据处理/电商机滤商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("new_goods_sn", "goodsSn");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }

    private Set<String> getGoodsFormatSet(String fileName) throws Exception{
        List<Map<String, String>> goodsCarList = getGoodsCarDataList(fileName);
        Set<String> formatSet = new HashSet<>();
        for(Map<String, String> data : goodsCarList){
            formatSet.add(data.get("goodsFormat"));

            //统计覆盖的车款
            CAR_ID_SET.add(data.get("carId"));
        }
        return formatSet;
    }

    private List<Map<String, String>> getGoodsCarDataList(String fileName) throws Exception{

        path = "/Users/huangzhangting/Desktop/机滤数据处理/补充数据/待处理数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("gc2.car_models_id", "carId");
        attrMap.put("gc2.goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }



    //根据商品编号，获取覆盖车款id
    private List<String> getCarIds(String goodsFormat, List<Map<String, Object>> dataList){
        List<String> carIds = new ArrayList<>();
        for(Map<String, Object> data : dataList){
            if(goodsFormat.equals(data.get("goods_format"))){
                carIds.add(data.get("car_models_id").toString());
            }
        }
        return carIds;
    }

    /**
     * 1、非云修机滤，覆盖车款id
     * 2、只要有一个车款id，没有对应的云修机滤，即可保留
     * */
    private boolean checkCar(List<Map<String, Object>> yxDataList, List<String> carIdList){
        for (String carId : carIdList){
            boolean flag = false;
            //在云修数据中查找
            for(Map<String, Object> data : yxDataList){
                if(carId.equals(data.get("car_models_id").toString())){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return true;
            }
        }
        return false;
    }

    //验证非云修机滤的覆盖的车型，是否是有效车型
    private void checkGoodsFormat(List<String> yxGoodsFormat, List<String> formatList, List<Map<String, Object>> yxDataList,
                                  List<Map<String, Object>> bsDataList, List<Map<String, Object>> acDataList,
                                  Set<String> goodsFormatSet){
//        Print.info(yxDataList.size());
//        Print.info(bsDataList.size());
//        Print.info(acDataList.size());

        String gf = formatList.get(0);
        if(!yxGoodsFormat.contains(gf)){
            Print.info("没有云修号："+formatList);
            return;
        }

        /**
         * 1、非云修机滤，覆盖车款id
         * 2、只要有一个车款id，没有对应的云修机滤，即可保留
         *
         * */
        //第一个是云修机滤
        for(int i=1; i<formatList.size(); i++){
            String goodsFormat = formatList.get(i);
            List<String> carIdList = getCarIds(goodsFormat, bsDataList);
            if(carIdList.isEmpty()){
                carIdList = getCarIds(goodsFormat, acDataList);
            }

            if(!checkCar(yxDataList, carIdList)){
//                Print.info("可以删除的非云修机滤："+goodsFormat);
                goodsFormatSet.add(goodsFormat);
            }
        }

    }



    // TODO db_goods_car数据处理
    private Set<String> unMatchGoodsFormats;
    private Set<String> matchGoodsIds;
    private List<Map<String, Object>> needDeleteGoodsCars; //需要删除的关系数据

    @Test
    public void test_goods_car() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/商品数据/";

        unMatchGoodsFormats = new HashSet<>();
        matchGoodsIds = new HashSet<>();
        needDeleteGoodsCars = new ArrayList<>();

        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        //全部的车款信息
        List<Map<String, Object>> carInfoList = getCarInfoList();
        Print.info(carInfoList.size());
        Print.info(carInfoList.get(0));

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云修机油滤清器.xls", attrMap.size());
        List<Map<String, String>> yxGoodsList = readerXLS.getDataList();
        Print.info(yxGoodsList.size());
        Print.info(yxGoodsList.get(0));
        List<Map<String, Object>> yxCarList = commonMapper.selectListBySql(getSql(YX_CODE));
        Print.info(yxCarList.size());
        Print.info(yxCarList.get(0));

        handleGoodsCarList(yxGoodsList, yxCarList, carInfoList, goodsCarList);


        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "博世机油滤清器.xls", attrMap.size());
        List<Map<String, String>> bsGoodsList = readerXLS.getDataList();
        Print.info(bsGoodsList.size());
        Print.info(bsGoodsList.get(0));
        List<Map<String, Object>> bsCarList = commonMapper.selectListBySql(getSql(BS_CODE));
        Print.info(bsCarList.size());
        Print.info(bsCarList.get(0));

        handleGoodsCarList(bsGoodsList, bsCarList, carInfoList, goodsCarList);


        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "AC德科机油滤清器.xls", attrMap.size());
        List<Map<String, String>> acGoodsList = readerXLS.getDataList();
        Print.info(acGoodsList.size());
        Print.info(acGoodsList.get(0));
        List<Map<String, Object>> acCarList = commonMapper.selectListBySql(getSql(AC_CODE));
        Print.info(acCarList.size());
        Print.info(acCarList.get(0));

        handleGoodsCarList(acGoodsList, acCarList, carInfoList, goodsCarList);

        Print.info("不存在的商品编码："+unMatchGoodsFormats);
        Print.info("匹配上的商品id："+matchGoodsIds.size());


        //处理goods car数据（）
//        for(String goodsId : matchGoodsIds){
//            List<Map<String, Object>> oldGoodsCarList = getGoodsCarByGoodsId(goodsId);
//            grepGoodsCar(oldGoodsCarList, goodsCarList);
//        }
//
//        Print.info("需要删除的goodsCar数据："+needDeleteGoodsCars.size());
//        Print.info("新增goodsCar数量："+goodsCarList.size());

        //处理sql
        handleDeleteSql();

        handleSql(goodsCarList);


        //导出excel数据
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"car_id", "goods_format", "brand", "company", "series", "model", "power", "year", "car_name"};

        List<Map<String, String>> mapList = PoiUtil.convert(goodsCarList);

        Collections.sort(mapList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("brand")+o1.get("company")+o1.get("model")+o1.get("year")+o1.get("car_name");
                String str2 = o2.get("brand")+o2.get("company")+o2.get("model")+o2.get("year")+o2.get("car_name");
                return str1.compareTo(str2);
            }
        });

        poiUtil.exportXlsxWithMap("机滤覆盖的车款信息", path, heads, fields, mapList);
    }

    private List<Map<String, Object>> getCarInfoList(){
        String sql = "select brand,brand_id,series,series_id,model,model_id," +
                "power,power_id,year,year_id,car_models as car_name,car_models_id as car_id, company " +
                "from db_car_all group by car_models_id";

        return commonMapper.selectListBySql(sql);
    }
    private Map<String, Object> getCarInfo(String carId, List<Map<String, Object>> carInfoList){
        for(Map<String, Object> car : carInfoList){
            if(carId.equals(car.get("car_id").toString())){
                return ObjectUtil.copyMap(car);
            }
        }
        return null;
    }

    private void handleGoodsCarList(List<Map<String, String>> goodsList, List<Map<String, Object>> carList,
                                    List<Map<String, Object>> carInfoList, List<Map<String, Object>> goodsCarList){

        for(Map<String, Object> car : carList){
            String goodsFormat = car.get("goods_format").toString();
            String carId = car.get("car_models_id").toString();

            boolean flag = false;

            for(Map<String, String> goods : goodsList){
                if(goodsFormat.equals(goods.get("goodsFormat"))){
                    Map<String, Object> gc = getCarInfo(carId, carInfoList);
                    if(gc!=null){
                        gc.put("goods_id", goods.get("goodsId"));
                        gc.put("goods_format", goodsFormat);
                        goodsCarList.add(gc);

                        matchGoodsIds.add(goods.get("goodsId"));

                    }else{
                        Print.info("错误的车款id："+car);
                    }

                    flag = true;
                }
            }

            if(!flag){
                unMatchGoodsFormats.add(goodsFormat);
            }
        }

    }

    private List<Map<String, Object>> getGoodsCarByGoodsId(String goodsId){
        String sql = "select goods_id,car_id,status from db_goods_car where goods_id="+goodsId;

        return commonMapper.selectListBySql(sql);
    }

    private void grepGoodsCar(List<Map<String, Object>> oldGoodsCarList, List<Map<String, Object>> goodsCarList){
        if(oldGoodsCarList.isEmpty()){
            return;
        }
        for(Map<String, Object> oldGc : oldGoodsCarList){
            if("1".equals(oldGc.get("status").toString())){
                String oldGid = oldGc.get("goods_id").toString();
                String oldCid = oldGc.get("car_id").toString();

                int size = goodsCarList.size();
                for(int i=0; i<size; i++){
                    Map<String, Object> newGc = goodsCarList.get(i);
                    if(oldGid.equals(newGc.get("goods_id").toString()) && oldCid.equals(newGc.get("car_id").toString())){
                        goodsCarList.remove(newGc);
                        break;
                    }
                }

                if(size == goodsCarList.size()){
                    needDeleteGoodsCars.add(oldGc);
                }
            }else{
                needDeleteGoodsCars.add(oldGc);
            }
        }
    }


    //处理需要删除的数据
    private void handleDeleteSql(){
        if(matchGoodsIds.isEmpty()){
            return;
        }
        List<String> goodsIdList = new ArrayList<>(matchGoodsIds);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "sql/delete_car_oil_filter_" + dateStr + ".sql");

        int count = 50;
        int size = goodsIdList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            sql.append(goodsIdList.get(i));
            if((i+1)%count==0){
                writeDeleteSql(sql);
                sql.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeDeleteSql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }
    private void writeDeleteSql(StringBuilder sql){
        sql.insert(0, "delete from db_goods_car where goods_id in(");
        sql.append(");\n");

        IoUtil.writeFile(writer, sql.toString());
    }


    //处理sql
    public void handleSql(List<Map<String, Object>> goodsCarList){
        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "sql/add_car_oil_filter_" + dateStr + ".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        int count = 500;
        int size = goodsCarList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addGcVal(sb, goodsCarList.get(i));
            if((i+1)%count==0){
                writeGcSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIdx==i){
                writeGcSql(sb);
                break;
            }
            sb.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    public void addGcVal(StringBuilder sb, Map<String, Object> data){
        sb.append("(");
        sb.append(data.get("goods_id")).append(",");
        sb.append(data.get("car_id")).append(",'");
        sb.append(data.get("car_name")).append("',");
        sb.append(data.get("brand_id")).append(",'");
        sb.append(data.get("brand")).append("',");
        sb.append(data.get("series_id")).append(",'");
        sb.append(data.get("series")).append("',");
        sb.append(data.get("model_id")).append(",'");
        sb.append(data.get("model")).append("',");
        sb.append(data.get("power_id")).append(",'");
        sb.append(data.get("power")).append("',");
        sb.append(data.get("year_id")).append(",'");
        sb.append(data.get("year")).append("',1,@nowTime)");

    }
    public void writeGcSql(StringBuilder sb){
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("insert ignore into db_goods_car");
        sqlSb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sqlSb.append(" values ").append(sb).append(";\n");

        IoUtil.writeFile(writer, sqlSb.toString());
    }



    //TODO 后续补充数据（只补充，未覆盖的数据）

    private List<Map<String, String>> getJgNeedCheckDataList(int goodsCarId){
        List<Map<String, Object>> mapList = commonMapper.selectListBySql(机滤相关sql.getNewDataSqlNeedCheck(JG_CODE, goodsCarId));
        return PoiUtil.convert(mapList);
    }
    private List<Map<String, String>> getJdCheckedData() throws Exception{
        String excel = path + "箭冠可以补充的机滤处理(一对多)-修订后.xls";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("cc2.car_models_id", "car_models_id");
        attrMap.put("cc2.goods_format", "goods_format");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());

        return readerXLS.getDataList();
    }
    private List<Map<String, String>> getJdCheckedDataList() throws Exception{
        String excel = path + "可用待处理数据/箭冠可以补充的机滤(一对多)-修订后-20160928.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "car_models_id");
        attrMap.put("商品编码", "goods_format");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);

        return readerXLSX.getDataList();
    }

    @Test
    public void test_new_data() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/";

        int goodsCarId = 3070300;

        List<Map<String, Object>> jgDataList = commonMapper.selectListBySql(机滤相关sql.getNewDataSql(JG_CODE, goodsCarId));
        Print.info(jgDataList.size());
        Print.info(jgDataList.get(0));

//        List<Map<String, String>> jgNeedCheckDataList = getJgNeedCheckDataList(goodsCarId);
        List<Map<String, String>> jgNeedCheckDataList = getJdCheckedDataList();
        Print.info(jgNeedCheckDataList.size());
        Print.info(jgNeedCheckDataList.get(0));

        if(true){
//            return;
        }

        Set<String> carIdSet = new HashSet<>();
        Set<String> goodsFormatSet = new HashSet<>();
        for(Map<String, Object> data : jgDataList){
            goodsFormatSet.add(data.get("goods_format").toString());

            carIdSet.add(data.get("car_models_id").toString());
        }
        Print.info(goodsFormatSet.size());

        Set<String> newGfSet = new HashSet<>();
        for(Map<String, String> data : jgNeedCheckDataList){
            String gf = data.get("goods_format");
            if(goodsFormatSet.add(gf)){
                newGfSet.add(gf);
            }

            carIdSet.add(data.get("car_models_id"));
        }
        Print.info(newGfSet.size());
        Print.info(goodsFormatSet.size());

        Print.info(carIdSet.size());
        countCoverage(carIdSet);

        //导出有用的商品编号
        exportGoodsFormats(goodsFormatSet);
    }

    private void exportGoodsFormats(Set<String> goodsFormatSet) throws Exception{
        List<Map<String, String>> list = new ArrayList<>();
        for(String gf : goodsFormatSet){
            Map<String, String> map = new HashMap<>();
            map.put("goodsFormat", gf);
            list.add(map);
        }

        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"商品编号"};
        String[] fields = new String[]{"goodsFormat"};

        poiUtil.exportXlsxWithMap("可以补充的商品编号", path, heads, fields, list);
    }

    //统计覆盖度
    public void countCoverage(Set<String> carIdSet) throws Exception {
        String sql = "select distinct car_id from db_goods_car where id>3070300";
        List<String> carIdList = commonMapper.selectOneFieldBySql(sql);
        Print.info(carIdList.size());

        carIdList.addAll(carIdSet);
        int s1 = carIdList.size();
        Print.info("覆盖车款："+s1);

        //增加规则后的车款总数
        List<Map<String, Object>> allCarList = commonMapper.selectListBySql(机滤相关sql.getCarListSql());
        Print.info(allCarList.size());
        Print.info(allCarList.get(0));

        List<Map<String, Object>> unCoverageCars = new ArrayList<>();
        for(Map<String, Object> car : allCarList){
            if(!carIdList.contains(car.get("id").toString())){
                unCoverageCars.add(car);
            }
        }

        Print.info("车款总数："+allCarList.size());
        int s2 = unCoverageCars.size();
        Print.info("未覆盖车款："+s2);
        Print.info(s1+s2);
        Print.info("覆盖率："+(s1*1.0)/(s1+s2));


        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"id", "brand", "company", "series", "model", "power", "year", "name"};

        List<Map<String, String>> mapList = PoiUtil.convert(unCoverageCars);
        poiUtil.exportXlsxWithMap("机滤未覆盖的车款", path, heads, fields, mapList);

    }


    //处理 箭冠可以补充的机滤处理(一对多)
    @Test
    public void test_compare_new_old() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("cc2.car_models_id", "id");
        attrMap.put("cc2.goods_format", "goodsFormat");
        attrMap.put("cc1.brand", "brand");
        attrMap.put("cc1.company", "company");
        attrMap.put("cc1.series", "series");
        attrMap.put("cc1.model", "model");
        attrMap.put("cc1.power", "power");
        attrMap.put("cc1.year", "year");
        attrMap.put("cc1.name", "name");
        attrMap.put("备注", "remark");

        String excel1 = path + "之前处理的数据/箭冠可以补充的机滤处理(一对多)-修订后-old.xls";
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel1, attrMap.size());

        List<Map<String, String>> oldDataList = readerXLS.getDataList();
        Print.info(oldDataList.size());
        Print.info(oldDataList.get(0));

        String excel2 = path + "箭冠可以补充的机滤(一对多)-重新处理.xls";
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel2, attrMap.size());

        List<Map<String, String>> newDataList = readerXLS.getDataList();
        Print.info(newDataList.size());
        Print.info(newDataList.get(0));


        List<Map<String, String>> needCheckNewDataList = new ArrayList<>();

        for(Map<String, String> newData : newDataList){
            boolean flag = false;
            String id = newData.get("id");
            String goodsFormat = newData.get("goodsFormat");
            for(Map<String, String> oldData : oldDataList){
                if(id.equals(oldData.get("id")) && goodsFormat.equals(oldData.get("goodsFormat"))){
                    newData.put("remark", oldData.get("remark"));
                    flag = true;
                    break;
                }
            }
            if(!flag){
                Print.info("有疑问的新数据："+newData);
                needCheckNewDataList.add(newData);
            }
        }
        Print.info("有疑问的新数据："+needCheckNewDataList.size());


        excel1 = path + "之前处理的数据/箭冠可以补充的机滤(一对多)-old.xls";
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel1, attrMap.size());

        oldDataList = readerXLS.getDataList();
        Print.info(oldDataList.size());
        Print.info(oldDataList.get(0));


        List<Map<String, String>> needCheckNewDataListTrue = new ArrayList<>();

        for(Map<String, String> ncData : needCheckNewDataList){
            boolean flag = false;
            String id = ncData.get("id");
            String goodsFormat = ncData.get("goodsFormat");
            for(Map<String, String> oldData : oldDataList){
                if(id.equals(oldData.get("id")) && goodsFormat.equals(oldData.get("goodsFormat"))){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                Print.info("真的有疑问的新数据："+ncData);
                needCheckNewDataListTrue.add(ncData);
            }
        }

        if(!needCheckNewDataListTrue.isEmpty()){
            Print.info("存在有疑问的新数据");
            //exportNeedCheckData(needCheckNewDataListTrue);
            //return;
        }

        //删除已确认的数据
        newDataList.removeAll(needCheckNewDataList);

        Print.info(newDataList.size());


        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款", "备注"};
        String[] fields = new String[]{"id", "goodsFormat", "brand", "company", "series", "model", "power",
                "year", "name", "remark"};

        poiUtil.exportXlsxWithMap("箭冠可以补充的机滤(一对多)-修订后", path, heads, fields, newDataList);

    }

    private void exportNeedCheckData(List<Map<String, String>> needCheckNewDataList) throws Exception{
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款", "备注"};
        String[] fields = new String[]{"id", "goodsFormat", "brand", "company", "series", "model", "power",
                "year", "name", "remark"};

        poiUtil.exportXlsxWithMap("箭冠可以补充的机滤(一对多)-需要检查", path, heads, fields, needCheckNewDataList);
    }



    @Test
    public void test__() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("奥盛号", "format");

        String excel1 = path + "机滤未覆盖的车款-20160928处理完成.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel1, 2);
        List<Map<String, String>> dataList1 = readerXLSX.getDataList();
        Print.info(dataList1.size());
        Print.info(dataList1.get(0));

        attrMap = new HashMap<>();
        attrMap.put("云修号", "goodsFormat");
        attrMap.put("厂家编码", "format");
        String excel2 = path + "云修机滤奥盛号与云修号对应关系.xlsx";
        readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel2, 3);
        List<Map<String, String>> dataList2 = readerXLSX.getDataList();
        Print.info(dataList2.size());
        Print.info(dataList2.get(0));


        List<Map<String, String>> carGoodsList = new ArrayList<>();

        Set<String> carIdSet = new HashSet<>();
        for(Map<String, String> car : dataList1){
            String format = car.get("format");
            if(StringUtils.isEmpty(format)){
                continue;
            }
            String carId = car.get("carId");
            for(Map<String, String> goods : dataList2){
                if(format.equals(goods.get("format"))){
                    //Print.info(carId + "  " + goods.get("goodsFormat"));
                    car.put("goodsFormat", goods.get("goodsFormat"));
                    carGoodsList.add(car);

                    if(!carIdSet.add(carId)){
                        Print.info("重复的车款id："+carId);
                    }
                    break;
                }
            }
        }

        Print.info(carGoodsList.size());
        Print.info(carGoodsList.get(0));


        List<Map<String, Object>> multipleDataList = commonMapper.selectListBySql(getMultipleDataSql(YX_CODE));
        Print.info(multipleDataList.size());
        Print.info(multipleDataList.get(0));


        List<Map<String, String>> newCarGoodsList = new ArrayList<>();
        for(Map<String, String> cg : carGoodsList){
            String carId = cg.get("carId");
            boolean flag = true;
            for(Map<String, Object> data : multipleDataList){
                if(carId.equals(data.get("car_models_id").toString())){
                    flag = false;
                    break;
                }
            }
            if(flag) {
                Print.info("可以新增的数据：" + cg);
                newCarGoodsList.add(cg);
            }
        }
        Print.info(newCarGoodsList.size());


        List<Map<String, Object>> oneDataList = commonMapper.selectListBySql(getOneDataSql(YX_CODE));
        Print.info(oneDataList.size());
        Print.info(oneDataList.get(0));

        List<Map<String, String>> newCarGoodsListTrue = new ArrayList<>();
        for(Map<String, String> cg : newCarGoodsList){
            String carId = cg.get("carId");
            boolean flag = true;
            for(Map<String, Object> data : oneDataList){
                if(carId.equals(data.get("car_models_id").toString())){
                    flag = false;
                    break;
                }
            }
            if(flag) {
                Print.info("真的可以新增的数据：" + cg);
                newCarGoodsListTrue.add(cg);
            }
        }
        Print.info(newCarGoodsListTrue.size());


        handleNewData(newCarGoodsListTrue);
    }

    private void handleNewData(List<Map<String, String>> newCarGoodsList) throws Exception{

        List<Map<String, Object>> carInfoList = getCarInfoList();

        path = "/Users/huangzhangting/Desktop/机滤数据处理/商品数据/";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云修机油滤清器.xls", attrMap.size());
        List<Map<String, String>> yxGoodsList = readerXLS.getDataList();
        Print.info(yxGoodsList.size());
        Print.info(yxGoodsList.get(0));

        Set<String> matchGoodsIds = new HashSet<>();
        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        for(Map<String, String> cg : newCarGoodsList){
            String carId = cg.get("carId");
            String goodsFormat = cg.get("goodsFormat");

            boolean flag = false;

            for(Map<String, String> goods : yxGoodsList){
                if(goodsFormat.equals(goods.get("goodsFormat"))){
                    Map<String, Object> gc = getCarInfo(carId, carInfoList);
                    if(gc!=null){
                        gc.put("goods_id", goods.get("goodsId"));
                        gc.put("goods_format", goodsFormat);
                        goodsCarList.add(gc);

                        matchGoodsIds.add(goods.get("goodsId"));
                    }else{
                        Print.info("错误的车款id："+cg);
                    }

                    flag = true;
                }
            }

            if(!flag){
                Print.info("错误的商品编码："+goodsFormat);
            }
        }

        Print.info(goodsCarList.size());

        handleSql(goodsCarList);

        Print.info(matchGoodsIds);


        //导出excel数据
        PoiUtil poiUtil = new PoiUtil();
        String[] heads = new String[]{"id", "商品编码", "品牌", "厂家", "车系", "车型", "排量", "年款", "车款"};
        String[] fields = new String[]{"car_id", "goods_format", "brand", "company", "series", "model", "power", "year", "car_name"};

        List<Map<String, String>> mapList = PoiUtil.convert(goodsCarList);

        Collections.sort(mapList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("brand")+o1.get("company")+o1.get("model")+o1.get("year")+o1.get("car_name");
                String str2 = o2.get("brand")+o2.get("company")+o2.get("model")+o2.get("year")+o2.get("car_name");
                return str1.compareTo(str2);
            }
        });

        poiUtil.exportXlsxWithMap("机滤覆盖的车款信息-增加", path, heads, fields, mapList);
    }

    //查询一个车款对应多个商品编码的sql
    private String getMultipleDataSql(int brandCode){
        String sql = "select tb2.car_models_id,tb2.goods_format " +
                "from ( " +
                "select tt1.car_models_id,count(1) " +
                "from ( " +
                "select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + brandCode +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id  having count(1)>1) tb1," +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + brandCode +
                " group by t2.car_models_id,t1.goods_format) tb2, db_car_category c " +
                "where tb1.car_models_id=tb2.car_models_id and tb2.car_models_id=c.id " +
                "and c.power!='电动' and c.name not like '%柴油%' ";

        return sql;
    }

    //查询一个车款对应一个商品编码的sql
    private String getOneDataSql(int brandCode){
        String sql = "select tb2.car_models_id,tb2.goods_format " +
                "from ( " +
                "select tt1.car_models_id,count(1) " +
                "from ( " +
                "select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + brandCode +
                " group by t2.car_models_id,t1.goods_format) tt1 " +
                "group by tt1.car_models_id  having count(1)=1) tb1," +
                "(select t2.car_models_id,t1.goods_format " +
                "from temp_goods_lyid_rel t1,db_car_all t2 " +
                "where t1.ly_id=t2.new_l_id and t1.brand_code=" + brandCode +
                " group by t2.car_models_id,t1.goods_format) tb2, db_car_category c " +
                "where tb1.car_models_id=tb2.car_models_id and tb2.car_models_id=c.id " +
                "and c.power!='电动' and c.name not like '%柴油%' ";

        return sql;
    }

}
