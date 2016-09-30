package crawl.qpbkw;

import dp.common.util.*;
import dp.common.util.excelutil.*;
import lombok.Data;
import org.junit.Test;

import java.io.File;
import java.util.*;


/**
 * 汽配百科网配件处理
 * Created by huangzhangting on 16/3/14.
 */

public class HandlePart {

    private String path;
    private String carModelStr;

    @Data
    class Part{
        private String oeNum;
        private String cateName;
        private String name;
        private String imgNo;
        private String partIndex;
        private String imgUrl;

        private Integer id;
        private String partName;

        private String carBrand;
        private String carModel;
        private String carYear;
        private String carPower;

        private Integer pid;
        private String originName;
    }


    //下载图片
    public void downloadImg(Map<String, String> imgMap){
        String imgPath = path + "img/";
        File file = new File(imgPath);
        if(!file.exists()){
            file.mkdirs();
        }

        for(Map.Entry<String, String> entry : imgMap.entrySet()){
            int i = entry.getValue().lastIndexOf(".");
            String name = imgPath + entry.getKey() + entry.getValue().substring(i).toLowerCase();

            String imgName = imgPath + entry.getKey() + ".png";
            ImgUtil.downloadImg(entry.getValue(), imgName);
        }
    }


    // todo 组装车型配件-分类，并处理配件数据
    @Test
    public void test1() throws Exception {
        boolean downloadFlag = true;
        boolean compareFlag = false;

        carModelStr = "别克-荣御";

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/汽配百科网/Excel文件/"+carModelStr+"/";
        String excel = path + carModelStr + "-车型配件.xls";
        File file = new File(excel);
        if(!file.exists()){
            Print.info("文件不存在："+excel);
            return;
        }

        Reader1 reader1 = new Reader1("part");
        reader1.process(excel, 7);

        List<Part> partList = reader1.partList;
        Print.info(partList.size());

        excel = path + carModelStr + "-配件分类.xls";
        file = new File(excel);
        if(!file.exists()){
            Print.info("文件不存在："+excel);
            return;
        }

        reader1 = new Reader1("cate");
        reader1.process(excel, 3);

        Print.info(reader1.cateMap.size());

        handlePartCate(partList, reader1.cateMap, compareFlag);

        //下载图片
        if(downloadFlag) {
            Print.info("开始下载图片");
            downloadImg(reader1.imgMap);
        }
    }

    // 组装配件分类
    public void handlePartCate(List<Part> partList, Map<Integer, Part> cateMap, boolean compareFlag){
        for(Part part : partList){
            Part cate = cateMap.get(part.getPid());
            if(cate==null){
                Print.info("存在没有分类的配件："+part);
                part.setCateName("");
                part.setImgNo("");
                part.setImgUrl("");
            }else {
                part.setCateName(cate.getCateName());
                part.setImgNo(cate.getImgNo());
                part.setImgUrl(cate.getImgUrl());
            }
        }

        Print.info("配件数量："+partList.size());

        PoiUtil poiUtil = new PoiUtil();
        try {
            String[] heads = new String[]{"品牌","车型","出厂年份","排量", "oe码", "百科配件名称", "名称（处理后）", "图编号", "配件位置", "图片链接"};
            String[] fields = new String[]{"carBrand","carModel","carYear","carPower", "oeNum", "originName", "name", "imgNo", "partIndex", "imgUrl"};

            poiUtil.exportXLSX(carModelStr + "-处理后", path, heads, fields, partList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //比较配件
        if(compareFlag) {
            Print.info("开始比较数据");
            comparePart(partList);
        }
    }

    private void handlePart(List<String> rowList, Set<String> keySet, List<Part> partList){
        String oeNum = rowList.get(4);
        int idx = oeNum.indexOf(" ");
        if(idx>-1){
            oeNum = oeNum.substring(0, idx);
        }

        String name = rowList.get(5).replace(oeNum, "");

        idx = name.indexOf("：");
        name = name.substring(idx+1);
        idx = name.indexOf("-");
        int idx_end = name.indexOf("(");
        if(idx_end>idx) {
            name = name.substring(idx + 1, idx_end).trim();
        }else{
            name = name.substring(idx+1).trim();
        }

        idx = name.indexOf("（");
        if(idx>-1){
            name = name.substring(0, idx);
        }

        name = StrUtil.repNotCN(name);

        String key = getKey(rowList, oeNum);
        if(keySet.add(key)){
            Part data = new Part();
            data.setOeNum(oeNum);
            data.setName(name);
            data.setPid(Integer.valueOf(rowList.get(6)));
            data.setOriginName(rowList.get(5));
            data.setCarBrand(rowList.get(0));
            data.setCarModel(rowList.get(1));
            data.setCarYear(rowList.get(2));
            data.setCarPower(rowList.get(3));

            data.setPartIndex(handlePartIndex(rowList.get(5)));

            partList.add(data);
        }

    }

    private String getKey(List<String> rowList, String oe){
        StringBuilder sb = new StringBuilder();
        sb.append(rowList.get(0)).append("_");
        sb.append(rowList.get(1)).append("_");
        sb.append(rowList.get(2)).append("_");
        sb.append(rowList.get(3)).append("_");
        sb.append(oe);

        return sb.toString();
    }

    private void handleCate(List<String> rowList, Map<Integer, Part> cateMap, Map<String, String> imgMap){
        Integer id = Integer.valueOf(rowList.get(0));
        if(cateMap.get(id)!=null){
            return;
        }
        Part cate = new Part();
        cate.setId(id);
        cate.setImgUrl(rowList.get(2));

        String cateName = rowList.get(1);
        cate.setImgNo(handleImgNo(cateName));

        cateName = StrUtil.repNotCN(cateName);

        cate.setCateName(cateName);

        cateMap.put(id, cate);

        imgMap.put(cate.getImgNo(), cate.getImgUrl());
    }

    private String handleImgNo(String cateName){
        int idx1 = cateName.indexOf("：");
        int idx2 = cateName.indexOf(".");

        if(idx1==-1 && idx2==-1){
            Print.info("类目有问题，name= "+cateName);
            return "";
        }
        int idx;
        if(idx1>-1 && idx2>-1){
            idx = idx1<idx2?idx1:idx2;
        }else{
            idx = idx1>idx2?idx1:idx2;
        }

        return cateName.substring(0, idx);
    }

    private String handlePartIndex(String partName){
        int idx = partName.indexOf("：");
        String partIndexStr = partName.substring(0, idx).replace("(","").replace(")","");

        if("".equals(partIndexStr) || "-".equals(partIndexStr)){
            return "";
        }

        try {
            int partIndex = Integer.parseInt(partIndexStr);
            return partIndex+"";

        }catch (Exception e){
            Print.info(partIndexStr+"  配件名称："+partName);

            return partIndexStr;
        }
    }

    class Reader1 extends ReadExcelXLS {
        private Set<String> keySet;
        private List<Part> partList;

        private Map<String, String> imgMap;

        private Map<Integer, Part> cateMap;

        private String kw;

        public Reader1(String kw) {
            keySet = new HashSet<>();
            partList = new ArrayList<>();
            imgMap = new HashMap<>();
            cateMap = new HashMap<>();
            this.kw = kw;
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(sheetIndex==0){
                if(curRow==0){
                    Print.info(sheetIndex + "#" + curRow + "  " + rowList.toString());
                }else{
                    switch (kw){
                        case "part": handlePart(rowList, keySet, partList); break;
                        case "cate": handleCate(rowList, cateMap, imgMap); break;
                        default: break;
                    }
                }
            }else{
                switch (kw){
                    case "part": handlePart(rowList, keySet, partList); break;
                    case "cate": handleCate(rowList, cateMap, imgMap); break;
                    default: break;
                }
            }
        }
    }


    // ======================== 跟其他网站比较数据 ========================
    class ReaderX1 extends ReadExcelXLSX{
        private Map<String, String> oeMap;

        public ReaderX1() {
            oeMap = new HashMap<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(curRow==0){

            }else{
                String oe = StrUtil.toUpCase(rowList.get(0));
                if("".equals(oe)){
                    return;
                }
                if(oeMap.get(oe)==null){
                    oeMap.put(oe, rowList.get(1));
                }
            }
        }
    }

    public void comparePart(List<Part> partList){
        String excel = "/Users/huangzhangting/Documents/数据处理/爬取数据/汽配百科网/Excel文件/上海通用所有配件号码和名称.xlsx";
        ReaderX1 readerX1 = new ReaderX1();
        try {
            readerX1.processOneSheet(excel, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Print.info(readerX1.oeMap.size());

        for(Part part : partList){
            String partName = readerX1.oeMap.get(part.getOeNum());
            if(partName==null){
                part.setPartName("");
            }else{
                part.setPartName(partName);
            }
        }

        PoiUtil poiUtil = new PoiUtil();
        try {
            String[] heads = new String[]{"品牌","车型","出厂年份","排量", "oe码", "百科配件名称", "名称（处理后）",
                    "卡卡配件名称", "图编号", "配件位置", "图片链接"};
            String[] fields = new String[]{"carBrand","carModel","carYear","carPower", "oeNum", "originName", "name",
                    "partName", "imgNo", "partIndex", "imgUrl"};

            poiUtil.exportXLSX(carModelStr+"-二次处理后", path, heads, fields, partList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
