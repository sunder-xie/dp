package crawl.jd;

import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.excelutil.ReadExcelXLS;
import org.junit.Test;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * excel数据处理
 * Created by huangzhangting on 16/4/14.
 */
public class HandleExcel {
    private String path;


    // TODO 处理京东商品数据，将属性行列转换
    @Test
    public void testGoods() throws Exception{
        String str = "京东瓦尔塔电瓶";
        boolean brandFlag = false;

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/";
        String excel = path + str + ".xls";
        File file = new File(excel);
        if(!file.exists()){
            Print.info("Excel不存在!!!");
            return;
        }

        Reader reader = new Reader();
        reader.process(excel, 8);

        Print.info(reader.dataMap.size());

        List<String> attrList = initAttrList();
        attrList.addAll(reader.attrSet);

        Collection<Map<String, String>> collection = reader.dataMap.values();
        if(brandFlag) {
            for (Map<String, String> data : collection) {
                if ("".equals(data.get("品牌"))) {
                    String name = data.get("商品名称").replace("（", " ").replace("(", " ");
                    int idx = name.indexOf(" ");
                    if (idx > -1) {
                        data.put("品牌", name.substring(0, idx));
                    }
                }

            }
        }

        PoiUtil pu = new PoiUtil();
        try {
            pu.exportXlsxWithMap(str, path, attrList, new ArrayList<>(collection));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<String> initAttrList(){
        List<String> attrList = new ArrayList<>();
        attrList.add("id");
        attrList.add("商品名称");
        attrList.add("短名称");
        attrList.add("品牌");
        //attrList.add("编码");
        //attrList.add("价格");

        return attrList;
    }

    class Reader extends ReadExcelXLS {
        private Map<String, Map<String, String>> dataMap;
        private Set<String> attrSet;

        public Reader() {
            dataMap = new HashMap<>();
            attrSet = new TreeSet<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(sheetIndex==0){
                if(curRow==0){

                }else{
                    String id = rowList.get(0);
                    Map<String, String> data = dataMap.get(id);
                    if(data==null){
                        data = new HashMap<>();
                        data.put("id", id);
                        data.put("商品名称", rowList.get(1));
                        data.put("短名称", rowList.get(2));
                        data.put("编码", rowList.get(3));
                        data.put("品牌", rowList.get(4));
                        data.put("价格", rowList.get(5));

                        String attr = rowList.get(6);
                        data.put(attr, rowList.get(7));
                        dataMap.put(id, data);
                        attrSet.add(attr);
                    }else{
                        String attr = rowList.get(6);
                        if(data.get(attr)==null){
                            data.put(attr, rowList.get(7));
                            attrSet.add(attr);
                        }
                    }
                }
            }
        }

    }


    //电商商品属性，组装
    @Test
    public void testDsGoods() throws Exception{
        String type = "轮胎";

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/";

        String goodsExcel = path + "电商轮胎.xls";

        String attrExcel = path + "电商商品属性值.xls";

        if(!IoUtil.fileExists(attrExcel) || !IoUtil.fileExists(goodsExcel)){
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("ga.goods_id", "goods_id");
        map.put("ac.attr_name", "属性名称");
        map.put("ga.attr_value", "属性值");

        CommReaderXLS readerXLS = new CommReaderXLS(map, Constant.TYPE_LIST, 0);
        readerXLS.process(attrExcel, map.size());
        List<Map<String, String>> attrList = readerXLS.getDataList();
        Print.info("商品-属性关系：" + attrList.size());

        map = new HashMap<>();
        map.put("g.goods_id", "g.goods_id");
        map.put("b.品牌", "b.品牌");
        map.put("g.名称", "g.名称");
        map.put("g.规格型号", "g.规格型号");
        map.put("g.关键词", "g.关键词");

        readerXLS = new CommReaderXLS(map, Constant.TYPE_LIST, 0);
        readerXLS.process(goodsExcel, map.size());
        Print.info("商品数量："+readerXLS.getDataList().size());

        handleDsGoods(attrList, readerXLS.getDataList(), initGoodsAttrMap().get(type));
    }

    private Map<String, Set<String>> initGoodsAttrMap(){
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> attrSet = new HashSet<>();
        attrSet.add("轮胎规格");
        attrSet.add("载重指数");
        attrSet.add("速度级别");
        map.put("轮胎", attrSet);

        return map;
    }

    //处理轮胎
    private void handleTyre(List<Map<String, String>> goodsList){
        for(Map<String, String> goods : goodsList){
            String attr1 = goods.get("载重指数");
            String attr2 = goods.get("速度级别");
            if(StringUtils.isEmpty(attr1) || StringUtils.isEmpty(attr2)){
                String goodsFormat = goods.get("g.规格型号");
                String[] gfs = goodsFormat.split(" ");
                goods.put("载重速度", gfs[1]);
            }else {
                goods.put("载重速度", attr1+attr2);
            }
        }
    }

    public void handleDsGoods(List<Map<String, String>> attrList, List<Map<String, String>> goodsList, Set<String> attrSet){
        if(goodsList.isEmpty() || attrList.isEmpty()){
            Print.info("没有数据");
            return;
        }
        if(CollectionUtils.isEmpty(attrSet)){
            Print.info("没有配置属性");
            return;
        }

        for(Map<String, String> goods : goodsList){
            for(Map<String, String> attr : attrList){
                if(goods.get("g.goods_id").equals(attr.get("goods_id")) && attrSet.contains(attr.get("属性名称"))){
                    goods.put(attr.get("属性名称"), attr.get("属性值"));
                }
            }
        }

        handleTyre(goodsList);

        PoiUtil pu = new PoiUtil();
        try {
            List<String> list = new ArrayList<>();
            list.add("g.goods_id");
            list.add("b.品牌");
            list.add("g.名称");
            list.add("g.规格型号");
            list.add("g.关键词");
            list.addAll(attrSet);
            list.add("载重速度");

            pu.exportXlsWithMap("电商轮胎", path, list, goodsList);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //处理电商商品规格型号
    @Test
    public void handleGoodsFormat() throws Exception{
        String str = "云修电瓶";

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/";

        String goodsExcel = path + str + ".xls";

        if(!IoUtil.fileExists(goodsExcel)){
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("g.goods_id", "g.goods_id");
        map.put("b.品牌", "b.品牌");
        map.put("g.名称", "g.名称");
        map.put("g.规格型号", "g.规格型号");
        map.put("g.关键词", "g.关键词");

        CommReaderXLS readerXLS = new CommReaderXLS(map, Constant.TYPE_LIST, 0);
        readerXLS.process(goodsExcel, map.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info("商品数量："+dataList.size());

        for(Map<String, String> data : dataList){
            String name = data.get("g.名称");
            String[] ns = name.split(" ");
            String goodsFormat = ns[2];
            if(goodsFormat.contains("/")){
                String[] gfs = goodsFormat.split("/");
                Print.info(gfs[1]+"  "+goodsFormat);
                goodsFormat = gfs[1];
            }else{
                Print.info(goodsFormat);
            }
            data.put("g.规格型号", goodsFormat);
        }

        PoiUtil pu = new PoiUtil();
        pu.exportXlsWithMap(str, path, new ArrayList<>(map.keySet()), dataList);
    }

}
