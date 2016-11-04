package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/10/21.
 */
public class TempTest extends BaseTest {
    private Common common;

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";

        String excel = path + "机油滤清器商品数据与车型关系20161017.xlsx";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("机油滤型号1", "goodsFormat");
        attrMap.put("id", "lyId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> lyIdGoodsList = readerXLSX.getDataList();
        Print.printList(lyIdGoodsList);

        attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> goodsList = readerXLSX.getDataList();
        Print.printList(goodsList);

        common = new Common(commonMapper);
        List<Map<String, String>> goodsCarList = new ArrayList<>();
        for(Map<String, String> goods : goodsList){
            String goodsFormat = goods.get("goodsFormat");
            Set<String> lyIdSet = common.getLyIdSet(goodsFormat, lyIdGoodsList);
            if(!lyIdSet.isEmpty()){
                Collection<Map<String, String>> matchGoodsCarList = common.getMatchGoodsCarList(lyIdSet);
                common.handleMatchGoodsCarList(goods, matchGoodsCarList);
                goodsCarList.addAll(matchGoodsCarList);
            }
        }
        Print.info("\n========== 需要处理的数据 ==========");
        Print.printList(goodsCarList);
        Print.info("");

        //判断有没有重复的数据
        Set<String> oldGoodsCarSet = getOldGoodsCarSet();
        List<Map<String, String>> repeatGoodsList = new ArrayList<>();
        int size = goodsCarList.size();
        for(int i=0; i<size; i++){
            Map<String, String> data = goodsCarList.get(i);
            String key = getKey(data);
            if(oldGoodsCarSet.contains(key)){
                if(goodsCarList.remove(data)) {
                    i--;
                    size--;
                    repeatGoodsList.add(data);
                }else{
                    Print.info("删除重复数据失败："+data);
                }
            }
        }

        Print.info("存在重复的数据："+repeatGoodsList.size());
        Print.info("有用数据："+goodsCarList.size());

        String filePath = path + "处理后/机油滤清器/";
        common.exportGoodsCarExcel("机滤-车款适配关系-补充", filePath, goodsCarList);

    }

    private String getKey(Map<String, String> data){
        return data.get("carId")+"_"+data.get("goodsFormat");
    }
    private Set<String> getOldGoodsCarSet() throws Exception{

        String filePath = path + "处理后/机油滤清器/";
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("id", "carId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "机滤-车款适配关系-20161019.xlsx");
        List<Map<String, String>> oneDataList = readerXLSX.getDataList();
        Print.printList(oneDataList);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "机滤-车款适配关系(一对多)-20161019.xlsx");
        List<Map<String, String>> multiDataList = readerXLSX.getDataList();
        Print.printList(multiDataList);

        oneDataList.addAll(multiDataList);

        Set<String> keySet = new HashSet<>();
        for(Map<String, String> data : oneDataList){
            String key = getKey(data);
            keySet.add(key);
        }

        return keySet;
    }


    // 2016-10-24
    @Test
    public void test_1024() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";

        String excel = path + "OK状态下没有匹配上的商品-20161024.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("产品编码", "goodsSn");
        attrMap.put("产品品牌", "goodsBrand");
        attrMap.put("产品名称", "goodsName");
        attrMap.put("规格型号", "goodsFormat");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> kongqilv = readerXLSX.getDataList();
        Print.printList(kongqilv);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 3);
        List<Map<String, String>> kongtiaolv = readerXLSX.getDataList();
        Print.printList(kongtiaolv);

        //力洋id关系数据
        attrMap = new HashMap<>();
        attrMap.put("规格型号", "goodsFormat");
        attrMap.put("id", "lyId");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 2);
        List<Map<String, String>> kongqilvLyIds = readerXLSX.getDataList();
        Print.printList(kongqilvLyIds);

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(excel, 4);
        List<Map<String, String>> kongtiaolvLyIds = readerXLSX.getDataList();
        Print.printList(kongtiaolvLyIds);

        common = new Common(commonMapper);

        //处理空气滤清器
        common.handleGoodsCar(kongqilv, kongqilvLyIds, "空气滤清器补充", path + "处理后/空气滤清器/");

        //处理空调滤清器
        common.handleGoodsCar(kongtiaolv, kongtiaolvLyIds, "空调滤清器补充", path + "处理后/空调滤清器/");
    }


    //2016-10-31
    @Test
    public void test_1031() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";
        String excel = path + "1031/1级别商品完成情况整理表20161021";
        //火花塞
        List<Map<String, String>> huoHuaList = Common.getOKGoodsList(excel, 1);
        //刹车片
        List<Map<String, String>> shaCheList = Common.getOKGoodsList(excel, 2);

        excel = path + "1031/1级别商品与力洋ID对应关系20161027";
        //力洋id火花塞
        List<Map<String, String>> lyHuoHuaList = Common.getLyIdGoodsList(excel, 1);
        //力洋id刹车片
        List<Map<String, String>> lyShaCheList = Common.getLyIdGoodsList(excel, 2);

        excel = path + "monkey商品库/力洋id博世刹车片数据.xls";
        List<Map<String, String>> mkLyShaCheList = Common.getMonkeyLyIdGoodsList(excel);


        common = new Common(commonMapper);
        String filePath = path+"1031/处理后的/";

        //处理火花塞
        common.handleGoodsCar(huoHuaList, lyHuoHuaList, "火花塞数据", filePath);

        //处理刹车片
        lyShaCheList.addAll(mkLyShaCheList);
        common.handleGoodsCar(shaCheList, lyShaCheList, "刹车片数据", filePath);

    }


    //2016-11-04
    @Test
    public void test_1104() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/";
        String excelPath = path + "1104/";
        String excel = excelPath + "2级别商品完成情况整理表";
        //刹车蹄
        List<Map<String, String>> shaCheTiList = Common.getOKGoodsList(excel, 1);
        //刹车盘
        List<Map<String, String>> shaChePanList = Common.getOKGoodsList(excel, 2);
        //灯泡
        List<Map<String, String>> dengPaoList = Common.getOKGoodsList(excel, 3);
        //喇叭
        List<Map<String, String>> laBaList = Common.getOKGoodsList(excel, 4);
        //火花塞
        List<Map<String, String>> huoHuaSaiList = Common.getOKGoodsList(excel, 5);

        excel = excelPath + "2级别商品与车型关系整理表 20161031";
        //刹车蹄
        List<Map<String, String>> lyShaCheTiList = Common.getLyIdGoodsList(excel, 1);
        //刹车盘
        List<Map<String, String>> lyShaChePanList = Common.getLyIdGoodsList(excel, 2);
        //灯泡
        List<Map<String, String>> lyDengPaoList = Common.getLyIdGoodsList(excel, 3);
        //喇叭
        List<Map<String, String>> lyLaBaList = Common.getLyIdGoodsList(excel, 4);
        //火花塞
        List<Map<String, String>> lyHuoHuaSaiList = Common.getLyIdGoodsList(excel, 5);

        excel = path + "monkey商品库/力洋id火花塞数据.xls";
        //monkey库火花塞
        List<Map<String, String>> monkeyLyHuoHuaSaiList = Common.getMonkeyLyIdGoodsList(excel);

        lyHuoHuaSaiList.addAll(monkeyLyHuoHuaSaiList);


        common = new Common(commonMapper);
        String filePath = excelPath + "处理后的/";

        common.handleGoodsCar(shaCheTiList, lyShaCheTiList, "刹车蹄", filePath);

        common.handleGoodsCar(shaChePanList, lyShaChePanList, "刹车盘", filePath);

        common.handleGoodsCar(dengPaoList, lyDengPaoList, "灯泡", filePath);

        common.handleGoodsCar(laBaList, lyLaBaList, "喇叭", filePath);

        common.handleGoodsCar(huoHuaSaiList, lyHuoHuaSaiList, "火花塞", filePath);
    }

    @Test
    public void test_1104_yuShua() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/1104/";
        String excel = path + "2级别商品完成情况整理表";
        Map<String, String> attrMap = Common.getGoodsAttrMap();
        attrMap.put("接口", "interface");
        attrMap.put("尺寸", "goodsSize");
        //雨刷
        List<Map<String, String>> yuShuaList = Common.getOKGoodsList(excel, 6, attrMap);

        attrMap = new HashMap<>();
        attrMap.put("雨刷接口", "interface");
        attrMap.put("尺寸主驾/副驾", "goodsSize");
        attrMap.put("淘气厂商", "company");
        attrMap.put("淘气品牌", "brand");
        attrMap.put("淘气车系", "series");
        attrMap.put("淘气车型", "model");
        attrMap.put("年款", "year");
        attrMap.put("排量", "power");

        excel = path + "雨刮车型匹配表修改.xlsx";
        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> goodsCarList = readerXLSX.getDataList();
        Print.printList(goodsCarList);

        List<Map<String, Object>> carList = getCarList();
        Print.printList(carList);

        List<Map<String, String>> resultList = new ArrayList<>();
        List<Map<String, String>> unMatchGoods = new ArrayList<>();
        for(Map<String, String> ys : yuShuaList){
            List<Map<String, String>> matchGoodsCarList = getMatchGoodsCarList(goodsCarList, ys);
            //Print.info("匹配上的数据：" + matchGoodsCarList.size());
            if(matchGoodsCarList.isEmpty()){
                Print.info("商品没有匹配上："+ys);
                unMatchGoods.add(ys);
                continue;
            }
            Collection<Map<String, String>> matchCarList = getMatchCarList(carList, matchGoodsCarList);
            if(matchCarList.isEmpty()){
                Print.info("车型没有匹配上："+ys);
                unMatchGoods.add(ys);
                continue;
            }
            for(Map<String, String> map : matchCarList){
                map.put("goodsSn", ys.get("goodsSn"));
                map.put("goodsBrand", ys.get("goodsBrand"));
                map.put("goodsName", ys.get("goodsName"));
                map.put("goodsFormat", ys.get("goodsFormat"));
            }
            resultList.addAll(matchCarList);
        }

        Print.info("没有匹配上的商品信息："+unMatchGoods.size());
        Print.info("最终数据："+resultList.size());

        String filePath = path + "处理后的/";
        IoUtil.mkdirsIfNotExist(filePath);

        Common.exportGoodsExcel("没有匹配上的雨刷", filePath, unMatchGoods);

        Common.exportGoodsCarExcel("雨刷", filePath, resultList);

    }
    private List<Map<String, Object>> getCarList(){
        String sql = "select brand,company,series,model,power,year,name as carName,id as carId from db_car_category where level=6 and is_del=0";
        return commonMapper.selectListBySql(sql);
    }

    /** 匹配之前的商品车型关系 */
    private List<Map<String, String>> getMatchGoodsCarList(List<Map<String, String>> goodsCarList, Map<String, String> goods){
        List<Map<String, String>> list = new ArrayList<>();
        String goodsSize = goods.get("goodsSize").replace("寸", "");
        //Print.info("尺寸："+goodsSize);
        if(goodsSize.contains("/")){
            for(Map<String, String> gc : goodsCarList){
                if(goods.get("interface").equals(gc.get("interface")) && goodsSize.equals(gc.get("goodsSize"))){
                    list.add(gc);
                }
            }
        }else{
            for(Map<String, String> gc : goodsCarList){
                if(goods.get("interface").equals(gc.get("interface")) && gc.get("goodsSize").contains(goodsSize)){
                    list.add(gc);
                }
            }
        }

        return list;
    }
    /** 匹配车型 */
    private Collection<Map<String, String>> getMatchCarList(List<Map<String, Object>> carList, List<Map<String, String>> goodsCarList){

        Map<String, Map<String, String>> carIdMap = new HashMap<>();

        for(Map<String, String> gc : goodsCarList){
            String brand = gc.get("brand");
            String company = gc.get("company");
            String series = gc.get("series");

            if(StringUtils.isEmpty(series) || StringUtils.isEmpty(brand) || StringUtils.isEmpty(company)){
                continue;
            }

            String model = gc.get("model").replace("\\", "/");
            Set<String> modelSet = getAttrSet(model);
            String power = gc.get("power");

            String year = gc.get("year");
            String startYear = getStartYear(year);
            String endYear = getEndYear(year);

            for(Map<String, Object> car : carList){
                if(brand.equals(car.get("brand").toString())
                        && company.equals(car.get("company").toString())
                        && series.equals(car.get("series").toString())){

                    if(compareAttrs(modelSet, car.get("model"))
                            && comparePower(power, car.get("power"))
                            && compareYear(year, startYear, endYear, car.get("year"))){

                        String carId = car.get("carId").toString();
                        if(carIdMap.get(carId)==null){
                            carIdMap.put(carId, ObjectUtil.objToStrMap(car));
                        }

                    }

                }
            }
        }

        return carIdMap.values();
    }

    //比较排量
    private boolean comparePower(String power, Object val){
        if(StringUtils.isEmpty(power)){
            return true;
        }
        if(val==null){
            return false;
        }
        String str = val.toString().trim();
        return str.contains(power);
    }

    //比较年款
    private boolean compareYear(String yearStr, String startYear, String endYear, Object obj){
        if(StringUtils.isEmpty(yearStr)){
            return true;
        }
        if(obj==null){
            return false;
        }
        String year = obj.toString().trim();
        if("".equals(year)){
            return false;
        }
        if(year.compareTo(startYear)<0){
            return false;
        }
        if(endYear==null){
            return true;
        }
        return year.compareTo(endYear)<=0;
    }

    //比较有多个值的属性
    private boolean compareAttrs(Set<String> attrs, Object val){
        if(attrs==null){
            return true;
        }
        if(val==null){
            return false;
        }
        String str = val.toString().trim();
        if("".equals(str)){
            return false;
        }
        return attrs.contains(str);
    }

    //有多个值的属性，封装成set
    private Set<String> getAttrSet(String attrs){
        if(attrs==null){
            return null;
        }
        attrs = attrs.trim();
        if("".equals(attrs)){
            return null;
        }
        Set<String> set = new HashSet<>();

        String[] ps = attrs.split("/");
        for(int i=0; i<ps.length; i++){
            set.add(ps[i]);
        }
        return set;
    }

    private String getStartYear(String year){
        if(StringUtils.isEmpty(year)){
            return null;
        }
        int idx = year.indexOf("-");
        if(idx>0){
            return year.substring(0, idx).trim();
        }
        return year.trim();
    }
    private String getEndYear(String year){
        if(StringUtils.isEmpty(year)){
            return null;
        }
        String[] ys = year.split("-");
        if(ys.length==1){
            return null;
        }
        return ys[1].trim();
    }
}
