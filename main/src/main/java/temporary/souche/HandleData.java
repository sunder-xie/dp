package temporary.souche;

import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * 处理数据
 * Created by huangzhangting on 16/4/11.
 */
public class HandleData {

    private String path;


    private Map<String, String> initAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("品牌", "品牌");
        attrMap.put("品牌编码", "品牌编码");
        attrMap.put("厂家", "厂家");
        attrMap.put("车系", "车系");
        attrMap.put("车系编码", "车系编码");
        attrMap.put("车型", "车型");
        attrMap.put("车型编码", "车型编码");
        attrMap.put("年款", "年款");
        attrMap.put("排量", "排量");
        attrMap.put("是否进口", "是否进口");
        attrMap.put("变速器", "变速器");
        attrMap.put("长", "长");
        attrMap.put("宽", "宽");
        attrMap.put("高", "高");
        attrMap.put("重量", "重量");

        return attrMap;
    }

    private String getPower(String carName){
        int idx = carName.indexOf(".");
        if(idx>0){
            return carName.substring(idx-1, idx+2);
        }
        return null;
    }


    // TODO ===================== 原始数据处理 ======================
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/";

        String excel = path + "车型数据-0410.xlsx";

        if(!IoUtil.fileExists(excel)){
            return;
        }

        CommReaderXLSX readerX = new CommReaderXLSX(initAttrMap(), Constant.TYPE_LIST, 0);
        readerX.processOneSheet(excel, 1);
        Print.info("大搜车数据："+readerX.getDataList().size());

        List<Map<String, String>> list = new ArrayList<>();
        for(Map<String, String> data : readerX.getDataList()){
            if(checkPower(data)){
                continue;
            }
            list.add(data);
        }
        Print.info("排量有问题的数据："+list.size());

        PoiUtil pu = new PoiUtil();
        try {
            String[] headList = new String[]{"品牌", "品牌编码", "厂家", "车系", "车系编码", "车型", "车型编码", "年款", "排量",
                    "是否进口", "变速器", "长", "宽", "高", "重量"};

            pu.exportXlsxWithMap("大搜车有问题的车型", path, headList, list);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkPower(Map<String, String> data){
        String power = data.get("排量");
        String carName = data.get("车型");
        if("NULL".equals(power)){
            //power = getPower(carName);
            return false;
        }
        if (!power.contains(".")) {
            power += ".0";
        }else if(power.endsWith(".")){
            power += "0";
        }
        String p = getPower(carName);
        if(p!=null && !p.equals(power)){
            return false;
        }
        return true;
    }


    //todo 根据品牌优先级过滤数据
    @Test
    public void test2() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/临时处理/车型匹配/";

        String excel = path + "没匹配上车型的品牌汇总-1.xlsx";
        String lyExcel = path + "力洋车型V4.xlsx";

        if(!IoUtil.fileExists(excel)){
            return;
        }

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("品牌", "品牌");
        attrMap.put("优先级", "优先级");

        CommReaderXLSX readerX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerX.processOneSheet(excel, 1);
        Print.info("品牌数据："+readerX.getDataList().size());


//        attrMap = new HashMap<>();
//        attrMap.put("力洋ID", "力洋ID");
//        attrMap.put("品牌", "品牌");
//
//        CommReaderXLSX lyReader = new CommReaderXLSX(attrMap, Constant.TYPE_LIST_MAP, 2);
//        lyReader.processOneSheet(lyExcel, 1);
//        Set<String> lyBrandSet = lyReader.getDataListMap().keySet();
//        Print.info("力洋数据：" + lyBrandSet.size());

        String key = "3";
        Set<String> brandSet = new HashSet<>();
        for(Map<String, String> data : readerX.getDataList()){
            if(key.equals(data.get("优先级"))){
                brandSet.add(data.get("品牌"));
            }
        }

        excel = path + "二次处理-大搜车没有匹配上的车型-20160412.xlsx";
        CommReaderXLSX readerX1 = new CommReaderXLSX(Init.initAttrMap(), Constant.TYPE_LIST, 0);
        readerX1.processOneSheet(excel, 1);
        Print.info("车型数据："+readerX1.getDataList().size());

        List<Map<String, String>> dataList = new ArrayList<>();
        for(Map<String, String> data : readerX1.getDataList()){
            if(brandSet.contains(data.get("品牌"))){
                dataList.add(data);
            }
        }
        Print.info("车型数据："+dataList.size());

        PoiUtil pu = new PoiUtil();
        String exportPath = path + "待处理/";
        File file = new File(exportPath);
        if(!file.exists()){
            file.mkdirs();
        }

        try {
            String[] headList = new String[]{"品牌", "品牌编码", "厂家", "车系", "车系编码", "车型", "车型编码", "年款", "排量",
                    "是否进口", "变速器", "长", "宽", "高", "重量"};

            pu.exportXlsxWithMap("大搜车车型数据-"+key, exportPath, headList, dataList);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
