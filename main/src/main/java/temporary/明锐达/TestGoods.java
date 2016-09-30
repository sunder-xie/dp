package temporary.明锐达;

import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.excelutil.ReadExcelXLSX;
import lombok.Data;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/4/19.
 */
public class TestGoods {
    private String path;

    class ExcelReaderX extends ReadExcelXLSX{
        private Map<Integer, String> attrIdxMap;
        private Map<String, String> attrMap;
        private List<Map<String, String>> dataList;

        private Set<String> keySet;
        private Set<String> carSet;

        private boolean flag;

        public ExcelReaderX(boolean flag) {
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

    class ReaderX1 extends ReadExcelXLSX{
        private Set<String> nameSet;
        private Map<String, Map<String, String>> dataMap;

        public ReaderX1() {
            nameSet = new HashSet<>();
            dataMap = new HashMap<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(curRow==0){

            }else{
                String name = rowList.get(1);
                nameSet.add(name);

                String oe = rowList.get(0);
                if(dataMap.get(oe)==null){
                    Map<String, String> map = new HashMap<>();
                    map.put("一级分类", "一级分类");
                    map.put("二级分类", "二级分类");
                    map.put("三级分类", "三级分类");

                    dataMap.put(oe, map);
                }
            }
        }
    }

    // todo ============= 明锐达商品数据处理 ==============
    @Test
    public void test2() throws Exception{
        String company = "上海大众-new";

        path = "/Users/huangzhangting/Documents/数据处理/临时处理/";
        String excel = path + "明锐达上海大众数据-20160406.xlsx";

        ExcelReaderX readerX = new ExcelReaderX(true);
        readerX.processOneSheet(excel, 1);

        Print.info("记录数："+readerX.keySet.size());
        Print.info("oe码处理后："+readerX.dataList.size());

//        excel = path + "tq奥迪数据.xls";
//
//        ExcelReader_1 reader1 = new ExcelReader_1();
//        reader1.process(excel, 4);
//
//        Print.info(reader1.dataMap.size());

        excel = "/Users/huangzhangting/Documents/数据处理/爬取数据/汽配百科网/Excel文件/百科大众途观配件数据-处理后-20160331.xlsx";
        ReaderX1 readerX1 = new ReaderX1();
        readerX1.processOneSheet(excel, 1);

        Print.info(readerX1.dataMap.size());
        Print.info(readerX1.nameSet.size());

        compareData(readerX.dataList, readerX1.dataMap, company);


//        Print.info(reader.carSet.size());
//        exportCar(reader.carSet);
    }

    @Data
    class Car{
        private String carBrand;
        private String carSeries;
        private String carModel;
        private String carYear;
        private String carName;
    }

    public void exportCar(Set<String> carSet){
        List<Car> carList = new ArrayList<>();
        for(String name : carSet){
            if("".equals(name)){
                continue;
            }
            Car car = new Car();
            car.setCarName(name);
            carList.add(car);
        }
        Collections.sort(carList, new Comparator<Car>() {
            @Override
            public int compare(Car o1, Car o2) {
                return o1.getCarName().compareTo(o2.getCarName());
            }
        });
        PoiUtil export = new PoiUtil();
        try {
            String[] headList = new String[]{"车型"};
            String[] fieldList = new String[]{"carName"};
            export.exportXLSX(path, "明锐达-车型", headList, fieldList, carList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String repName(String name){
        int idx = name.indexOf("(");
        if(idx>-1){
            name = name.substring(0, idx);
        }
        return StrUtil.repNotCN(name);
    }

    public void compareData(List<Map<String, String>> dataList, Map<String, Map<String, String>> oeDataMap, String company){
        List<Map<String, String>> matchDataList = new ArrayList<>();
        List<Map<String, String>> unMatchDataList = new ArrayList<>();

        Map<String, Map<String, String>> nameMap = new HashMap<>();

        for(Map<String, String> data : dataList){
            String oe = data.get("oeNum");
            Map<String, String> oeData = oeDataMap.get(oe);
            if(oeData==null){
                oe = "L"+oe;
                oeData = oeDataMap.get(oe);
                if(oeData==null) {
                    unMatchDataList.add(data);
                    continue;
                }
            }

            data.put("一级分类", oeData.get("一级分类"));
            data.put("二级分类", oeData.get("二级分类"));
            data.put("三级分类", oeData.get("三级分类"));
            matchDataList.add(data);

            String name = repName(data.get("名称"));
            nameMap.put(name, oeData);

        }

        Print.info("匹配上的oe："+matchDataList.size());
        Print.info("没有匹配上的oe："+unMatchDataList.size());

        PoiUtil export = new PoiUtil();
        String[] headList;
        try {
            headList = new String[]{"加入顺序","编码","特征码","名称","产地","型号","品牌","类别",
                    "单位","底价","备注","分组","碳粉量","加入时间", "一级分类", "二级分类", "三级分类"};

            export.exportXlsxWithMap(path, company + "匹配上的oe数据", headList, matchDataList);

            headList = new String[]{"加入顺序","编码","特征码","名称","产地","型号","品牌","类别",
                    "单位","底价","备注","分组","碳粉量","加入时间"};

            export.exportXlsxWithMap(path, company + "没有匹配上的oe数据", headList, unMatchDataList);

        }catch (Exception e){
            e.printStackTrace();
        }


        //二次处理
        List<Map<String, String>> finalUnMatchDataList = new ArrayList<>();
        if(!nameMap.isEmpty() && !unMatchDataList.isEmpty()){
            for(Map<String, String> data : unMatchDataList){
                String name = repName(data.get("名称"));
                Map<String, String> oeData = nameMap.get(name);
                if(oeData!=null){
                    data.put("一级分类", oeData.get("一级分类"));
                    data.put("二级分类", oeData.get("二级分类"));
                    data.put("三级分类", oeData.get("三级分类"));
                    matchDataList.add(data);
                }else{
                    finalUnMatchDataList.add(data);
                }
            }
        }

        Print.info("二次处理-匹配上的oe："+matchDataList.size());
        Print.info("二次处理-没有匹配上的oe："+finalUnMatchDataList.size());

        //去除重复的
        handleDataList(matchDataList);
        handleDataList(finalUnMatchDataList);

        Print.info("二次处理-匹配上的oe："+matchDataList.size());
        Print.info("二次处理-没有匹配上的oe："+finalUnMatchDataList.size());

        try {
            headList = new String[]{"加入顺序","编码","特征码","名称","产地","型号","品牌","类别",
                    "单位","底价","备注","分组","碳粉量","加入时间", "一级分类", "二级分类", "三级分类"};

            export.exportXlsxWithMap(path, company + "挂上分类的数据", headList, matchDataList);

            headList = new String[]{"加入顺序","编码","特征码","名称","产地","型号","品牌","类别",
                    "单位","底价","备注","分组","碳粉量","加入时间"};

            export.exportXlsxWithMap(path, company + "没有挂上分类数据", headList, finalUnMatchDataList);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void handleDataList(List<Map<String, String>> dataList){
        Set<String> keySet = new HashSet<>();
        int size = dataList.size();
        for(int i=0; i<size; i++){
            if(keySet.add(dataList.get(i).get("加入顺序"))){
                continue;
            }
            dataList.remove(i);
            i--;
            size--;
        }
    }

}
