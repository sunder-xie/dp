package temporary.souche;

import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * 匹配车型
 * Created by huangzhangting on 16/4/11.
 */
public class HandleCar {

    private String path;
    private List<Map<String, String>> matchList;
    private List<Map<String, String>> unMatchList;

    private String prefix;

    private Map<String, String> carModelMap;
    private Map<String, String> brandMap;
    private Map<String, String> spBrandMap;//特殊品牌

    private Set<String> spCompanySet;

    private List<Map<String, String>> spCpDataList;


    @Test
    public void testCar() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/";

        String lyExcel = path + "力洋车型V4.xlsx";
        String excel = path + "车型数据-0410.xlsx";

        if(!IoUtil.fileExists(lyExcel) || !IoUtil.fileExists(excel)){
            return;
        }

        CommReaderXLSX lyReader = new CommReaderXLSX(Init.initLyAttrMap(), Constant.TYPE_LIST_MAP, 2);
        lyReader.processOneSheet(lyExcel, 1);
        Print.info("力洋数据：" + lyReader.getDataListMap().size());

        CommReaderXLSX readerX = new CommReaderXLSX(Init.initAttrMap(), Constant.TYPE_LIST, 0);
        readerX.processOneSheet(excel, 1);
        Print.info("大搜车数据："+readerX.getDataList().size());

        carModelMap = Init.initCarModelMap();
        brandMap = Init.initBrandMap();
        spBrandMap = Init.initSpBrandMap();

        matchList = new ArrayList<>();
        unMatchList = new ArrayList<>();

        compareCar(lyReader.getDataListMap(), readerX.getDataList());

        Print.info("匹配上的车型："+matchList.size());
        Print.info("没有匹配上的车型："+unMatchList.size());

        //导出excel
        prefix = "";
        exportExcel();

        if(unMatchList.isEmpty()){
            return;
        }

        //二次处理
        List<Map<String, String>> dataList = new ArrayList<>(unMatchList);
//        matchList = new ArrayList<>();
        unMatchList = new ArrayList<>();

        compareCar2(lyReader.getDataListMap(), dataList);

        prefix = "二次处理-";
        Print.info(prefix+"匹配上的车型："+matchList.size());
        Print.info(prefix+"没有匹配上的车型："+unMatchList.size());

        exportExcel();
    }

    public void compareCar(Map<String, List<Map<String, String>>> lyBrandMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            List<Map<String, String>> lyCarList = compareBrand(brand, lyBrandMap);

            boolean flag = false;

            if(lyCarList==null){
                brand += "-" + data.get("车系");
                lyCarList = lyBrandMap.get(spBrandMap.get(brand));
                if(lyCarList==null) {
                    unMatchList.add(data);
                    continue;
                }
                flag = true;
                Print.info(brand);
            }

            if(compareDetail(data, lyCarList)){
                if(flag){
                    unMatchList.add(data);
                    continue;
                }

                brand += "-" + data.get("车系");
                lyCarList = lyBrandMap.get(spBrandMap.get(brand));
                if(lyCarList==null) {
                    unMatchList.add(data);
                    continue;
                }
                Print.info(brand);
                if(compareDetail(data, lyCarList)) {
                    unMatchList.add(data);
                }
            }
        }
    }

    public void compareCar2(Map<String, List<Map<String, String>>> lyBrandMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            List<Map<String, String>> lyCarList = compareBrand(brand, lyBrandMap);

            boolean flag = false;

            if(lyCarList==null){
                brand += "-" + data.get("车系");
                lyCarList = lyBrandMap.get(spBrandMap.get(brand));
                if(lyCarList==null) {
                    unMatchList.add(data);
                    continue;
                }
                flag = true;
                Print.info(brand);
            }

            if(compareDetail2(data, lyCarList)){
                if(flag){
                    unMatchList.add(data);
                    continue;
                }

                brand += "-" + data.get("车系");
                lyCarList = lyBrandMap.get(spBrandMap.get(brand));
                if(lyCarList==null) {
                    unMatchList.add(data);
                    continue;
                }
                Print.info(brand);
                if(compareDetail2(data, lyCarList)) {
                    unMatchList.add(data);
                }
            }
        }
    }

    private List<Map<String, String>> compareBrand(String brand, Map<String, List<Map<String, String>>> lyBrandMap){
        List<Map<String, String>> list = lyBrandMap.get(brand);
        if(list==null){
            Set<String> brandSet = lyBrandMap.keySet();
            for(String lyBrand : brandSet){
//                if(brand.contains(lyBrand) || lyBrand.contains(brand)){
//                    return lyBrandMap.get(lyBrand);
//                }
                if(StrUtil.rep(brand).equals(StrUtil.rep(lyBrand))){

                    return lyBrandMap.get(lyBrand);
                }
            }
        }
        return list;
    }

    //比较长宽高
    private boolean compareCKG(Map<String, String> lyCar, Map<String, String> data){
        if(lyCar.get("长").equals(data.get("长"))
                && lyCar.get("宽").equals(data.get("宽"))
                && lyCar.get("高").equals(data.get("高"))){
            return true;
        }
        return false;
    }
    //比较长宽高车型
    private boolean compareCKGM(Map<String, String> lyCar, Map<String, String> data){
        if(compareCKG(lyCar, data)){
            return compareModel(lyCar, data);
        }
        return false;
    }
    //比较车型
    private boolean compareModel(Map<String, String> lyCar, Map<String, String> data){
        String carName = StrUtil.toUpCase(data.get("车型"));
        String lyModel = StrUtil.toUpCase(lyCar.get("车型"));
        if(carName.contains(lyModel)){
            return true;
        }
        lyModel = carModelMap.get(lyModel);
        if(lyModel!=null)
            return carName.contains(lyModel);

        return false;
    }

    private String getPower(String carName){
        int idx = carName.indexOf(".");
        if(idx>0){
            return carName.substring(idx-1, idx+2);
        }
        return null;
    }
    //比较年款排量
    private boolean compareYearPower(Map<String, String> lyCar, Map<String, String> data){
        String year = data.get("年款");
        if("".equals(year)){
            String car = data.get("车型");
            int idx = car.indexOf("款");
            if(idx>-1){
                year = car.substring(0, idx);
                data.put("年款", year);
            }
        }
        if(lyCar.get("年款").equals(year)){
            String power = data.get("排量");
            String carName = data.get("车型");
            if("NULL".equals(power)){
                power = getPower(carName);
                if(power!=null){
                    data.put("排量", power);
                    return lyCar.get("排量").equals(power);
                }
            }else {
                if (!power.contains(".")) {
                    power += ".0";
                }else if(power.endsWith(".")){
                    power += "0";
                }
                String p = getPower(carName);
                if(p!=null && !p.equals(power)){
//                    Print.info(carName+"  "+p+"  "+power);
                    power = p;
                    data.put("排量", power);
                }
                return lyCar.get("排量").equals(power);
            }
        }

        return false;
    }
    //比较是否进口
    private boolean compareIsImport(Map<String, String> lyCar, Map<String, String> data){
        return data.get("是否进口").contains(lyCar.get("是否进口"));
    }

    private void addToMatchList(Map<String, String> lyCar, Map<String, String> data){
        data.put("力洋ID", lyCar.get("力洋ID"));
        data.put("力洋品牌", lyCar.get("品牌"));
        data.put("力洋厂家", lyCar.get("厂家"));
        data.put("力洋车系", lyCar.get("车系"));
        data.put("力洋车型", lyCar.get("车型"));
        data.put("力洋销售名称", lyCar.get("销售名称"));
        matchList.add(data);
    }

    //匹配细节
    public boolean compareDetail(Map<String, String> data, List<Map<String, String>> lyCarList){
        boolean umFlag = true;
        //一次匹配
        for(Map<String, String> lyCar : lyCarList){
            if(compareIsImport(lyCar, data) && compareCKGM(lyCar, data) && compareYearPower(lyCar, data)){
                addToMatchList(lyCar, data);
                umFlag = false;
                break;
            }
        }
        if(umFlag){
            //二次匹配，降低标准
            for(Map<String, String> lyCar : lyCarList){
                if(compareIsImport(lyCar, data) && compareCKG(lyCar, data) && compareYearPower(lyCar, data)){
                    addToMatchList(lyCar, data);
                    umFlag = false;
                    break;
                }
            }
//
//            if(umFlag) {
//                unMatchList.add(data);
//            }
        }

        return umFlag;
    }

    //匹配细节
    public boolean compareDetail2(Map<String, String> data, List<Map<String, String>> lyCarList){
        boolean umFlag = true;
        for(Map<String, String> lyCar : lyCarList){
            if(compareIsImport(lyCar, data) && compareModel(lyCar, data) && compareYearPower(lyCar, data)){
                addToMatchList(lyCar, data);
                umFlag = false;
                break;
            }
        }
//        if(umFlag) {
//            unMatchList.add(data);
//        }

        return umFlag;
    }

    //导出excel
    public void exportExcel(){
        PoiUtil pu = new PoiUtil();
        try {
            String[] headList = new String[]{"品牌", "品牌编码", "厂家", "车系", "车系编码", "车型", "车型编码", "年款", "排量",
                    "是否进口", "变速器", "长", "宽", "高", "重量", "力洋品牌", "力洋厂家", "力洋车系", "力洋车型", "力洋ID"};

            pu.exportXlsxWithMap(prefix+"大搜车匹配上的车型", path, headList, matchList);

            String[] headList2 = new String[]{"品牌", "品牌编码", "厂家", "车系", "车系编码", "车型", "车型编码", "年款", "排量",
                    "是否进口", "变速器", "长", "宽", "高", "重量"};

            pu.exportXlsxWithMap(prefix+"大搜车没有匹配上的车型", path, headList2, unMatchList);

            List<Map<String, String>> list = new ArrayList<>();
            Set<String> set = new HashSet<>();
            for(Map<String, String> data : unMatchList){
                if(set.add(data.get("品牌"))){
                    Map<String, String> map = new HashMap<>();
                    map.put("品牌", data.get("品牌"));
                    list.add(map);
                }
            }
            //pu.exportXlsxWithMap(prefix+"没匹配上车型的品牌", path, new String[]{"品牌"}, list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // todo ==================== 处理车型 ====================
    @Test
    public void testCarNew() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/待处理/";

        String excel = path + "大搜车车型数据-3-20160420.xlsx";

        if(!IoUtil.fileExists(excel)){
            return;
        }

        CommReaderXLSX lyReader = new CommReaderXLSX(Init.initLyAttrMap(), Constant.TYPE_LIST_MAP, 2);
        lyReader.processOneSheet("/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/力洋车型V4.xlsx", 1);
        Print.info("力洋数据：" + lyReader.getDataListMap().size());
        if(lyReader.getDataListMap().isEmpty()){
            Print.info("没有力洋数据");
            return;
        }

        CommReaderXLSX readerX = new CommReaderXLSX(Init.initAttrMap(), Constant.TYPE_LIST, 0);
        readerX.processOneSheet(excel, 1);
        Print.info("大搜车数据："+readerX.getDataList().size());
        if(readerX.getDataList().isEmpty()){
            Print.info("没有数据");
            return;
        }

        path = path + DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd)+"/";
        IoUtil.mkdirsIfNotExist(path);

        carModelMap = Init.initCarModelMap();
        brandMap = Init.initBrandMap();
        spCompanySet = Init.initSpCompanySet();

        spCpDataList = new ArrayList<>();

        matchList = new ArrayList<>();
        unMatchList = new ArrayList<>();

        compareCarNew(lyReader.getDataListMap(), readerX.getDataList());

        Print.info("匹配上的车型："+matchList.size());
        Print.info("没有匹配上的车型："+(unMatchList.size()+spCpDataList.size()));

        //导出excel
        prefix = "";
        exportExcel();

        if(unMatchList.isEmpty()){
            return;
        }

        //二次处理
        List<Map<String, String>> dataList = new ArrayList<>(unMatchList);
//        matchList = new ArrayList<>();
        unMatchList = new ArrayList<>();

        compareCarNew2(lyReader.getDataListMap(), dataList);

        prefix = "二次处理-";
        Print.info(prefix+"匹配上的车型："+matchList.size());
        Print.info(prefix+"没有匹配上的车型："+(unMatchList.size()+spCpDataList.size()));

        exportExcel();

        //通过厂家比较
//        compareByCompany();
    }

    //比较车型
    public void compareCarNew(Map<String, List<Map<String, String>>> lyBrandMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            if(spCompanySet.contains(brand)){
                spCpDataList.add(data);
                continue;
            }
            List<Map<String, String>> lyCarList = compareBrand(brand, lyBrandMap);
            if(lyCarList==null){
                lyCarList = lyBrandMap.get(brandMap.get(brand));
                if(lyCarList==null){
                    unMatchList.add(data);
                    continue;
                }
            }
            if(compareDetail(data, lyCarList)){
                unMatchList.add(data);
            }
        }
    }

    public void compareCarNew2(Map<String, List<Map<String, String>>> lyBrandMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            List<Map<String, String>> lyCarList = compareBrand(brand, lyBrandMap);
            if(lyCarList==null){
                lyCarList = lyBrandMap.get(brandMap.get(brand));
                if(lyCarList==null){
                    unMatchList.add(data);
                    continue;
                }
            }
            if(compareDetail2(data, lyCarList)){
                unMatchList.add(data);
            }
        }
    }


    // todo 通过厂家比较
    public void compareByCompany() throws Exception{
        Print.info("通过厂家比较的数据："+spCpDataList.size());
        if(spCpDataList.isEmpty()){
            return;
        }
//        matchList = new ArrayList<>();
        unMatchList = new ArrayList<>();

        CommReaderXLSX lyReader = new CommReaderXLSX(Init.initLyAttrMap(), Constant.TYPE_LIST_MAP, 1);
        lyReader.processOneSheet("/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/力洋车型V4.xlsx", 1);
        Print.info("力洋厂商数据：" + lyReader.getDataListMap().size());

        compareCarByCp(lyReader.getDataListMap(), spCpDataList);

        Print.info("匹配上的车型总数："+matchList.size());
        Print.info("没有匹配上的车型："+unMatchList.size());

        prefix = "三次处理-";
        exportExcel();

        if(unMatchList.isEmpty()){
            return;
        }

        List<Map<String, String>> dataList = new ArrayList<>(unMatchList);
        unMatchList = new ArrayList<>();

        compareCarByCp2(lyReader.getDataListMap(), dataList);

        prefix = "四次处理-";
        Print.info(prefix+"匹配上的车型总数："+matchList.size());
        Print.info(prefix+"没有匹配上的车型："+unMatchList.size());

        exportExcel();
    }

    //比较车型
    public void compareCarByCp(Map<String, List<Map<String, String>>> lyCompanyMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            List<Map<String, String>> lyCarList = lyCompanyMap.get(brand);
            if(lyCarList==null){
                unMatchList.add(data);
                continue;
            }
            if(compareDetail(data, lyCarList)){
                unMatchList.add(data);
            }
        }
    }

    public void compareCarByCp2(Map<String, List<Map<String, String>>> lyCompanyMap, List<Map<String, String>> dataList){
        for(Map<String, String> data : dataList){
            String brand = StrUtil.toUpCase(data.get("品牌"));
            List<Map<String, String>> lyCarList = lyCompanyMap.get(brand);
            if(lyCarList==null){
                unMatchList.add(data);
                continue;
            }
            if(compareDetail2(data, lyCarList)){
                unMatchList.add(data);
            }
        }
    }

}
