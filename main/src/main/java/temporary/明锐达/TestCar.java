package temporary.明锐达;

import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.excelutil.ReadExcelXLS;
import dp.common.util.excelutil.ReadExcelXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/4/19.
 */
public class TestCar {
    private String path;

    class ExcelReader extends ReadExcelXLS {
        private Map<Integer, String> attrIdxMap;
        private Map<String, String> attrMap;
        private List<Map<String, String>> dataList;

        private Set<String> keySet;
        private Set<String> carSet;

        private boolean flag;

        public ExcelReader(boolean flag) {
            attrMap = initAttrMap();
            attrIdxMap = new HashMap<>();
            dataList = new ArrayList<>();

            keySet = new HashSet<>();
            carSet = new HashSet<>();

            this.flag = flag;
        }

        private Map<String, String> initAttrMap(){
            Map<String, String> attrMap = new HashMap<>();
            attrMap.put("加入顺序", "加入顺序");
            attrMap.put("编码", "编码");
            attrMap.put("特征码", "特征码");
            attrMap.put("名称", "名称");
            attrMap.put("产地", "产地");
            attrMap.put("型号", "型号");
            attrMap.put("品牌", "品牌");
            attrMap.put("类别", "类别");
            attrMap.put("单位", "单位");
            attrMap.put("底价", "底价");
            attrMap.put("备注", "备注");
            attrMap.put("分组", "分组");
            attrMap.put("碳粉量", "碳粉量");
            attrMap.put("加入时间", "加入时间");

            return attrMap;
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(sheetIndex==0) {
                if (curRow == 0) {
                    Print.info(rowList.toString());
                    for(int i=0; i<rowList.size(); i++){
                        attrIdxMap.put(i, StrUtil.strip(rowList.get(i)));
                    }
                } else {
                    if(flag) {
                        handleData(rowList);
                    }else{
                        Map<String, String> dataMap = new HashMap<>();
                        for(int i=0; i<rowList.size(); i++){
                            String attr = attrMap.get(attrIdxMap.get(i));
                            if(attr==null) {
                                continue;
                            }
                            dataMap.put(attr, StrUtil.strip(rowList.get(i)));
                        }

                        dataList.add(dataMap);
                    }

                    keySet.add(StrUtil.strip(rowList.get(0)));

                }
            }
        }

        //组装数据
        private void addData(List<String> rowList, String oe){
            Map<String, String> dataMap = new HashMap<>();
            for(int i=0; i<rowList.size(); i++){
                String attr = attrMap.get(attrIdxMap.get(i));
                if(attr==null) {
                    continue;
                }
                dataMap.put(attr, StrUtil.strip(rowList.get(i)));
            }
            dataMap.put("oeNum", oe);

            dataList.add(dataMap);

            String[] cars = dataMap.get("型号").replace("\\", "/").split("/");
            for(String car : cars) {
                carSet.add(car.trim());
            }
        }

        private void handleData(List<String> rowList){
            String oe = StrUtil.strip(rowList.get(1)).toUpperCase();
            if("".equals(oe)){
                addData(rowList, "");
                return;
            }
            int idx = oe.lastIndexOf("/");
            List<String> oeList = new ArrayList<>();
            if(idx>-1){
//                Print.info("原始oe："+oe);
                try {
                    int idx2 = oe.lastIndexOf(" ");
                    String oe2 = oe.substring(0, idx2) + oe.substring(idx + 1);
//                Print.info(oe2+"   "+repOe(oe2));
                    oeList.add(repOe(oe2));

                    oe = oe.substring(0, idx);

                    idx = oe.lastIndexOf("/");
                    if (idx > -1) {
                        String oe3 = oe.substring(0, idx2) + oe.substring(idx + 1);
//                    Print.info(oe3+"   "+repOe(oe3));
                        oeList.add(repOe(oe3));

                        oe = oe.substring(0, idx);
                    }
                }catch (Exception e){

                }
            }
            oeList.add(repOe(oe));

            for(String oeNum : oeList){
                addData(rowList, oeNum);
            }
        }

        private String repOe(String oe){
            return oe.replaceAll("[^0-9A-Z=]", "");
        }
    }

    class ReaderX extends ReadExcelXLSX {
        private Map<String, Map<String, String>> map;

        private String kw;

        private Map<String, Map<String, Map<String, String>>> dataMap;

        public ReaderX(String company) {

            kw = company;
            dataMap = new HashMap<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(curRow==0){

            }else{
                String carModel = rowList.get(1);
                String company = rowList.get(2);
                if("".equals(carModel) || "".equals(company)){
                    return;
                }

                map = dataMap.get(company);
                if(map==null){
                    map = new HashMap<>();
                    dataMap.put(company, map);
                }

                String key = rowList.get(0);
                if(map.get(key)==null){
                    Map<String, String> carMap = new HashMap<>();
                    carMap.put("carModel", carModel);
                    carMap.put("company", company);
                    map.put(key, carMap);
                }
            }
        }
    }

    // todo 根据车型筛选明锐达商品数据
    @Test
    public void testCar() throws Exception {
        path = "/Users/huangzhangting/Documents/数据处理/临时处理/";
        String excel = path + "明锐达-车型.xlsx";

        ReaderX readerX = new ReaderX("");
        readerX.processOneSheet(excel, 1);

//        Print.info(readerX.map.size());
//        for(Map.Entry<String, Map<String, String>> entry : readerX.map.entrySet()){
//            Print.info(entry.getKey()+"   "+entry.getValue());
//        }

        excel = path + "明锐达商品列表-1.xls";

        ExcelReader reader = new ExcelReader(false);
        reader.process(excel, 14);

        Print.info("记录数：" + reader.keySet.size());
        Print.info("oe码处理后："+reader.dataList.size());

        List<Map<String, String>> unMatchList = new ArrayList<>();

        Map<String, List<Map<String, String>>> matchDataMap = new HashMap<>();

        for(Map<String, String> data : reader.dataList){
            String car = data.get("型号");
            boolean unMatchFlag = true;

            for(Map.Entry<String, Map<String, Map<String, String>>> companyEt : readerX.dataMap.entrySet()){

                List<Map<String, String>> matchList = matchDataMap.get(companyEt.getKey());
                if(matchList==null){
                    matchList = new ArrayList<>();
                    matchDataMap.put(companyEt.getKey(), matchList);
                }

                Map<String, Map<String, String>> map = companyEt.getValue();
                if(map.get(car)==null){
                    if(car.contains("/")) {
                        String[] cars = car.split("/");
                        for (String str : cars) {
                            if(map.get(str.trim())!=null){
                                matchList.add(data);
                                unMatchFlag = false;
                                break;
                            }
                        }
                    }
                }else{
                    matchList.add(data);
                    unMatchFlag = false;
                }
            }

            if(unMatchFlag){
                unMatchList.add(data);
            }
        }

        Print.info("没匹配上车型的数据："+unMatchList.size());


        PoiUtil export = new PoiUtil();
        try {
            String[] headList = new String[]{"加入顺序","编码","特征码","名称","产地","型号","品牌","类别",
                    "单位","底价","备注","分组","碳粉量","加入时间"};

            export.exportXlsxWithMap(path, "明锐达数据-new", headList, unMatchList);

            for(Map.Entry<String, List<Map<String, String>>> entry : matchDataMap.entrySet()){
                Print.info(entry.getKey()+"："+entry.getValue().size());
                export.exportXlsxWithMap(path, "明锐达" + entry.getKey() + "数据", headList, entry.getValue());
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
