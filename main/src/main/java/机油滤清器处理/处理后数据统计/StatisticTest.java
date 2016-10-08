package 机油滤清器处理.处理后数据统计;

import base.BaseTest;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import org.springframework.util.StringUtils;
import 机油滤清器处理.BrandEnum;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/2.
 */
public class StatisticTest extends BaseTest {
    //覆盖的车款id集合
    private static Set<String> CAR_ID_SET = new HashSet<>();


    @Test
    public void justTest() throws Exception{
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(StatisticConfig.needCheckCarsSql());
        Print.info(dataList.size());
        Print.info(dataList.get(0));

    }

    @Test
    public void test() throws Exception{

        path = "/Users/huangzhangting/Desktop/机滤数据处理/商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "car_models_id");
        attrMap.put("商品编码", "goods_format");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path + "机滤覆盖的车款信息-增加-20160929.xlsx", 1);

        List<Map<String, String>> yxAddDataList = readerXLSX.getDataList();
        Print.info(yxAddDataList.size());
        Print.info(yxAddDataList.get(0));

        Set<String> yxGoodsFormat = goodsFormatSet(BrandEnum.YUN_XIU.getCode());
        Print.info(yxGoodsFormat.size());
        addYxGoods(yxGoodsFormat, yxAddDataList);
        Print.info(yxGoodsFormat.size());

        //验证过的数据
        List<Map<String, String>> checkedList = getCheckedDataList();

        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> yxDataList = oneToOneDataList(BrandEnum.YUN_XIU.getCode()); //云修机滤
        Print.info(yxDataList.size());
        yxDataList.addAll(ObjectUtil.strToObjMapList(yxAddDataList));
        Print.info(yxDataList.size());

        yxDataList = grepDataList(checkedList, yxDataList);
        Print.info("验证后的云修覆盖量："+yxDataList.size());

        List<Map<String, Object>> bsDataList = oneToOneDataList(BrandEnum.BO_SHI.getCode()); //博世机滤
        Print.info(bsDataList.size());
        bsDataList = grepDataList(checkedList, bsDataList);
        Print.info("验证后的博世覆盖量："+bsDataList.size());

        List<Map<String, Object>> acDataList = oneToOneDataList(BrandEnum.AC_DE_KE.getCode()); //AC德科机滤
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

        Set<String> goodsFormatSet = new HashSet<>();
        for(Map.Entry<Integer, List<String>> entry : carIdMap.entrySet()){
            if(entry.getValue().size()>1){
                //Print.info(entry.getKey()+"  "+entry.getValue());
                checkGoodsFormat(yxGoodsFormat, entry.getValue(), yxDataList, bsDataList, acDataList, goodsFormatSet);

            }
        }

        Print.info("最终可以删除的非云修机滤："+goodsFormatSet);

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

        Set<String> bwFormat = supplyGoodsFormatSet("豹王机滤可补充的数据.xls");
        Set<String> mlFormat = supplyGoodsFormatSet("马勒机滤可补充的数据.xls");
        Set<String> hyFormat = supplyGoodsFormatSet("海业机滤可补充的数据.xls");

        List<Map<String, String>> yxGoodsList = getGoodsDataList("云修机油滤清器.xls");
        List<Map<String, String>> bsGoodsList = getGoodsDataList("博世机油滤清器.xls");
        List<Map<String, String>> acGoodsList = getGoodsDataList("AC德科机油滤清器.xls");
        List<Map<String, String>> bwGoodsList = getGoodsDataList("豹王机滤.xls");
        List<Map<String, String>> mlGoodsList = getGoodsDataList("马勒机滤.xls");
        List<Map<String, String>> hyGoodsList = getGoodsDataList("海业机滤.xls");

        //最终要导出excel的数据
        List<Map<String, String>> goodsDataList = new ArrayList<>();

        handleGoodsDataList(yxGoodsFormat, yxGoodsList, "云修", goodsDataList);
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

        //TODO 导出商品信息excel
//        ExcelExporter.exportGoodsInfo(path, goodsDataList);


        //统计未覆盖车款信息
        handleCarCoverage();

    }

    //处理车款覆盖度
    private void handleCarCoverage(){
        List<Map<String, Object>> needCheckCars = commonMapper.selectListBySql(StatisticConfig.needCheckCarsSql());
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

        //TODO 导出未覆盖车款信息excel
//        ExcelExporter.exportUnCoverCars(path, needCheckCarsTrue);

    }

    private Set<String> goodsFormatSet(int code){
        List<String> list = commonMapper.selectOneFieldBySql(StatisticConfig.goodsFormatSql(code));
        return new HashSet<>(list);
    }

    private List<Map<String, Object>> oneToOneDataList(int code){
        return commonMapper.selectListBySql(StatisticConfig.oneToOneDataSql(code));
    }

    //添加云修商品型号
    private void addYxGoods(Set<String> yxGoodsFormats, List<Map<String, String>> yxAddDataList){
        for(Map<String, String> data : yxAddDataList){
            String goodsFormat = data.get("goods_format");
            if(!yxGoodsFormats.contains(goodsFormat)){
                yxGoodsFormats.add(goodsFormat);
            }
        }
    }

    //修订后的数据
    private List<Map<String, String>> getCheckedDataList() throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("备注", "remark");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(filePath + "机滤覆盖车型信息处理汇总表.xlsx", 1);

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

    //过滤掉，人工检查后，错误的数据
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
    private void checkGoodsFormat(Set<String> yxGoodsFormat, List<String> formatList, List<Map<String, Object>> yxDataList,
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


    //处理验证后的箭冠数据
    private void checkJgCarIds(Set<String> carIdSet) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("错误说明", "errorDesc");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath+"箭冠可以补充的机滤(一个车款一个机滤)修改汇总.xls", attrMap.size());
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

    //TODO 处理箭冠补充数据
    private List<Map<String, String>> handleJgGoods() throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/箭冠补充数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath+"箭冠可以补充的机滤(一对一).xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(filePath+"箭冠可以补充的机滤(一对多)-处理后.xlsx", 1);
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

    //TODO 处理奥盛补充数据
    private List<Map<String, String>> handleAsGoods() throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/奥盛机滤/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath+"奥盛可以补充的型号.xls", attrMap.size());
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

    //组装最终需要生成excel的数据
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

    //电商商品数据
    private List<Map<String, String>> getGoodsDataList(String fileName) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/电商机滤商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("new_goods_sn", "goodsSn");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }

    private Set<String> supplyGoodsFormatSet(String fileName) throws Exception{
        List<Map<String, String>> goodsCarList = supplyGoodsCarDataList(fileName);
        Set<String> formatSet = new HashSet<>();
        for(Map<String, String> data : goodsCarList){
            formatSet.add(data.get("goodsFormat"));

            //统计覆盖的车款
            CAR_ID_SET.add(data.get("carId"));
        }
        return formatSet;
    }

    private List<Map<String, String>> supplyGoodsCarDataList(String fileName) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/补充数据/待处理数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("gc2.car_models_id", "carId");
        attrMap.put("gc2.goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }

}
