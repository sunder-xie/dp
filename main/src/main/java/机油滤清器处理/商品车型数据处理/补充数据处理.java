package 机油滤清器处理.商品车型数据处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import org.springframework.util.StringUtils;
import 机油滤清器处理.BrandEnum;
import 机油滤清器处理.处理后数据统计.StatisticConfig;

import java.util.*;

/**
 * Created by huangzhangting on 16/9/29.
 */
public class 补充数据处理 extends BaseTest {

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/补充数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("g.goods_format", "goodsFormat");
        attrMap.put("gc.liyang_Id", "lyId");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path + "豹王机滤-力洋id.xls", attrMap.size());

        List<Map<String, String>> bwDataList = readerXLS.getDataList();
        Print.info(bwDataList.size());
        Print.info(bwDataList.get(0));

        writeSql(BrandEnum.BAO_WANG.getCode(), bwDataList);


        readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path + "马勒机滤-力洋id.xls", attrMap.size());

        List<Map<String, String>> mlDataList = readerXLS.getDataList();
        Print.info(mlDataList.size());
        Print.info(mlDataList.get(0));

        writeSql(BrandEnum.MA_LE.getCode(), mlDataList);
    }

    private void writeSql(int brandCode, List<Map<String, String>> dataList){

        writer = IoUtil.getWriter(path + "insert_goods_ly_id_rel_"+brandCode+".sql");

        for(Map<String, String> data : dataList){
            StringBuilder sql = new StringBuilder();
            sql.append("insert ignore into temp_goods_lyid_rel(goods_format, ly_id, brand_code) value ");
            sql.append("('").append(data.get("goodsFormat"));
            sql.append("', '").append(data.get("lyId"));
            sql.append("', ").append(brandCode);
            sql.append(");\n");

            IoUtil.writeFile(writer, sql.toString());
        }

        IoUtil.closeWriter(writer);
    }


    //TODO 奥盛数据处理
    @Test
    public void test_ao_sheng() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/奥盛机滤/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path+"奥盛可以补充的型号.xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));


        String excel = "/Users/huangzhangting/Desktop/机滤数据处理/云修机滤奥盛号与云修号对应关系.xlsx";
        attrMap = new HashMap<>();
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

        for(Map<String, String> data : oToDataList){
            String goodsFormat = data.get("goodsFormat");
            String str = relMap.get(goodsFormat);
            if(str!=null){
                Print.info("存在对应的云修号："+str+"  奥盛号："+goodsFormat);
            }
        }

    }


    //TODO 处理箭冠补充数据
    @Test
    public void test_jian_guan() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/箭冠补充数据/";

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

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(path+"箭冠可以补充的机滤(一对一).xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path+"箭冠可以补充的机滤(一对多)-处理后.xlsx", 1);
        List<Map<String, String>> oTmDataList = readerXLSX.getDataList();
        Print.info(oTmDataList.size());
        Print.info(oTmDataList.get(0));

        Set<String> carIdSet = new HashSet<>();
        Set<String> repeatCarIds = new HashSet<>(); //重复的车款id
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

        //需要删除的关系数据
        Set<String> needRemoveIds = errorJgCarIds();
        Print.info("需要删除的关系数据："+needRemoveIds.size());
        needRemoveIds.addAll(repeatCarIds);
        Print.info("需要删除的关系数据："+needRemoveIds.size());

        //处理数据
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.addAll(oToDataList);
        dataList.addAll(oTmDataList);

        List<Map<String, String>> effectiveList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            String carId = data.get("carId");
            if(!needRemoveIds.contains(carId)){
                //Print.info("有效数据："+data);
                effectiveList.add(data);
            }
        }
        Print.info("有效数据："+effectiveList.size());

        //TODO 导出excel
        ExcelExporter.exportCarGoodsData(path, "箭冠可以补充的机滤", effectiveList);

    }

    private Set<String> errorJgCarIds() throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("错误说明", "errorDesc");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath+"箭冠可以补充的机滤(一个车款一个机滤)修改汇总.xls", attrMap.size());
        List<Map<String, String>> oToDataList = readerXLS.getDataList();
        Print.info(oToDataList.size());
        Print.info(oToDataList.get(0));

        Set<String> carIdSet = new HashSet<>();
        for(Map<String, String> data : oToDataList){
            String errorDesc = data.get("errorDesc");
            if(!StringUtils.isEmpty(errorDesc)){
                String carId = data.get("carId");
                carIdSet.add(carId);
            }
        }

        return carIdSet;
    }


    // TODO 修订后的箭冠数据处理
    @Test
    public void test_jian_guan_checked() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/待处理的数据/箭冠补充数据/";

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
        attrMap.put("备注", "remark");

        attrMap.put("品牌-new", "brand_new");
        attrMap.put("厂家-new", "company_new");
        attrMap.put("车系-new", "series_new");
        attrMap.put("车型-new", "model_new");
        attrMap.put("排量-new", "power_new");
        attrMap.put("年款-new", "year_new");
        attrMap.put("进气形式-new", "inlet_type");


        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path + "箭冠可以补充的机滤-修订后-20161009.xlsx", 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Set<String> deleteFormats = new HashSet<>();
        Set<String> effectiveFormats = new HashSet<>();
        List<Map<String, String>> modifyDataList = new ArrayList<>(); //修订数据
        List<Map<String, String>> effectiveList = new ArrayList<>(); //有效的数据
        for(Map<String, String> data : dataList){
            String remark = data.get("remark");
            String goodsFormat = data.get("goodsFormat");

            if("删除".equals(remark)){
                deleteFormats.add(goodsFormat);
            }else{
                effectiveFormats.add(goodsFormat);
                if("更改".equals(remark)){
                    modifyDataList.add(data);
                }else{
                    effectiveList.add(data);
                }
            }
        }
        Print.info(effectiveFormats.size());
        Print.info(deleteFormats);

        boolean flag = false;
        for(String df : deleteFormats){
            if(!effectiveFormats.contains(df)){
                Print.info("需要删除的型号："+df);
                flag = true;
            }
        }

        if(flag){
            return;
        }

        Print.info(modifyDataList.size());
        Print.info(modifyDataList.get(0));

        Print.info("有效的数据："+effectiveList.size());

        effectiveList.addAll(handleModifyDataList(modifyDataList));

        Print.info("有效的数据："+effectiveList.size());


        //TODO 导出excel
        ExcelExporter.exportCarGoodsData(path, "箭冠可以补充的型号(修订后)", effectiveList);

    }

    private List<Map<String, String>> handleModifyDataList(List<Map<String, String>> modifyDataList){
        List<Map<String, Object>> oilCarList = commonMapper.selectListBySql(StatisticConfig.oilCarSql());
        Print.info(oilCarList.size());
        Print.info(oilCarList.get(0));

        List<Map<String, String>> dataList = new ArrayList<>();

        for(Map<String, String> md : modifyDataList){
            String brand = md.get("brand_new");
            String company = md.get("company_new");
            String series = md.get("series_new");
            String model = md.get("model_new");

            String year = md.get("year_new");

            Set<String> powerSet = getAttrSet(md.get("power_new"));

            String inletType = md.get("inlet_type");

            for(Map<String, Object> car : oilCarList){
                if(brand.equals(car.get("brand").toString())
                        && company.equals(car.get("company").toString())
                        && series.equals(car.get("series").toString())
                        && model.equals(car.get("model").toString())){

                    if(comparePower(powerSet, car.get("power"), inletType)
                            && compareYear(year, car.get("year"))){

                        //Print.info("匹配上的数据："+md.get("goodsFormat")+"  "+car);

                        Map<String, String> map = ObjectUtil.objToStrMap(car);
                        map.put("carId", map.get("id"));
                        map.put("goodsFormat", md.get("goodsFormat"));
                        map.put("carName", map.get("name"));

                        dataList.add(map);
                    }

                }
            }

        }

        Print.info(dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }

    //比较年款
    private boolean compareYear(String yearStr, Object obj){
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

        return yearStr.equals(year);
    }

    //比较排量
    private boolean comparePower(Set<String> attrs, Object val, String inletType){
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

        return attrs.contains(str.replace(inletType, ""));
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

}
