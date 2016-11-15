package 库存商品处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 *
 * Created by huangzhangting on 16/11/15.
 */
public class YuShuaTest extends BaseTest{

    //TODO ========== 处理雨刷数据 ==========
    @Test
    public void test_yuShua() throws Exception{
        path = "/Users/huangzhangting/Desktop/库存商品数据处理/1116/";
        String excel = path + "剩余库存商品整理完成情况表2(1)";
        Map<String, String> attrMap = Common.getGoodsAttrMap();
        attrMap.put("接口", "interface");
        attrMap.put("尺寸", "goodsSize");
        //雨刷
        List<Map<String, String>> yuShuaList = Common.getOKGoodsList(excel, 5, attrMap);

        attrMap = new HashMap<>();
        attrMap.put("雨刷接口", "interface");
        attrMap.put("尺寸主驾/副驾", "goodsSize");
        attrMap.put("淘气厂商", "company");
        attrMap.put("淘气品牌", "brand");
        attrMap.put("淘气车系", "series");
        attrMap.put("淘气车型", "model");
        attrMap.put("年款", "year");
        attrMap.put("排量", "power");

        excel = path + "雨刮车型匹配表修改(1).xlsx";
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
