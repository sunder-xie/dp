package crawl.jd;

import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.ReadExcelXLS;
import lombok.Data;
import org.junit.Test;

import java.io.Writer;
import java.util.*;

/**
 * Created by huangzhangting on 16/4/13.
 */
@Deprecated
public class HandleGoodsCar{
    @Data
    class GoodsCar{
        private Integer goods_id;
        private Integer car_id;
        private String car_name;
        private Integer car_brand_id;
        private String car_brand;
        private Integer car_series_id;
        private String car_series;
        private Integer car_model_id;
        private String car_model;
        private Integer car_power_id;
        private String car_power;
        private Integer car_year_id;
        private String car_year;
    }

    private String path;
    private Writer writer;

    public Map<String, String> initLyGoodsAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("c.liyang_id_list", "ly_ids");
        attrMap.put("gc.goods_id", "g_id");

        return attrMap;
    }

    // 生成力洋id-京东商品id，对应关系
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/";

        String excel = path + "力洋id-京东商品id.xls";

        if(!IoUtil.fileExists(excel)){
            return;
        }

        CommReaderXLS readerXLS = new CommReaderXLS(initLyGoodsAttrMap(), Constant.TYPE_LIST, 0);
        readerXLS.process(excel, 2);
        Print.info(readerXLS.getDataList().size());

        //处理数据
        Map<String, Set<String>> goodsIdLyIdMap = new HashMap<>();
        for(Map<String, String> data : readerXLS.getDataList()){
            Set<String> lyIdSet = goodsIdLyIdMap.get(data.get("g_id"));
            if(lyIdSet==null){
                lyIdSet = new HashSet<>();
                goodsIdLyIdMap.put(data.get("g_id"), lyIdSet);
            }
            String lyId = data.get("ly_ids");
            if(lyId.contains(",")){
                String[] ids = lyId.split(",");
                for(String id : ids){
                    lyIdSet.add(id);
                }
            }else{
                lyIdSet.add(lyId);
            }
        }

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + "ly_id_goods_"+dateStr+".sql";
        writer = IoUtil.getWriter(sql);

        for(Map.Entry<String, Set<String>> entry : goodsIdLyIdMap.entrySet()){
            handleLyIdGoodsId(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        IoUtil.closeWriter(writer);
    }

    public void handleLyIdGoodsId(String goodsId, List<String> lyIdList){
        Print.info("商品id："+goodsId);
        Print.info("力洋id数量："+lyIdList.size());

        int count = 2000;

        int size = lyIdList.size();
        int lastIndex = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            sb.append("(").append(goodsId).append(",'");
            sb.append(lyIdList.get(i)).append("')");
            if((i+1)%count==0){
                writeLgSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeLgSql(sb);
            }
            sb.append(",");
        }
    }

    public void writeLgSql(StringBuilder valueSb){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ly_id_goods(goods_id,ly_id) values");
        sb.append(valueSb).append(";\n");

        IoUtil.writeFile(writer, sb.toString());
    }


    class Reader_1 extends ReadExcelXLS {
        private Map<Integer, Set<Integer>> goodsIdCarIdsMap;
        private Map<Integer, GoodsCar> carMap;

        public Reader_1() {
            goodsIdCarIdsMap = new HashMap<>();
            carMap = new HashMap<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(sheetIndex==0){
                if(curRow==0){
                    Print.info(sheetIndex + "#" + curRow + "  " + rowList.toString());
                }else{
                    handleData(rowList);
                }
            }else{
                handleData(rowList);
            }
        }

        private void handleData(List<String> rowList){
            Integer jdGoodsId = Integer.valueOf(rowList.get(0));

            Integer carId = Integer.valueOf(rowList.get(1));

            Set<Integer> carIds = goodsIdCarIdsMap.get(jdGoodsId);
            if(carIds==null){
                carIds = new HashSet<>();
                goodsIdCarIdsMap.put(jdGoodsId, carIds);
            }
            carIds.add(carId);

            GoodsCar gc = carMap.get(carId);
            if(gc==null){
                gc = new GoodsCar();
//                gc.setCar_id(carId);
                gc.setCar_name(rowList.get(2));
                gc.setCar_brand(rowList.get(3));
                gc.setCar_brand_id(Integer.valueOf(rowList.get(4)));
                gc.setCar_series(rowList.get(5));
                gc.setCar_series_id(Integer.valueOf(rowList.get(6)));
                gc.setCar_model(rowList.get(7));
                gc.setCar_model_id(Integer.valueOf(rowList.get(8)));
                gc.setCar_power(rowList.get(9));
                gc.setCar_power_id(Integer.valueOf(rowList.get(10)));
                gc.setCar_year(rowList.get(11));
                gc.setCar_year_id(Integer.valueOf(rowList.get(12)));

                carMap.put(carId, gc);
            }
        }
    }

    public Map<String, String> initGoodsIdAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("jd_id", "jd_id");
        attrMap.put("ds_id", "ds_id");

        return attrMap;
    }

    // 生成插入临时表 temp_goods_car 的数据
    @Test
    public void test1() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/";

        String excel = path + "tq车型-商品.xls";

        String goodsExcel = path + "商品数据/机油/京东匹配上的机油-20160413.xlsx";

        if(!IoUtil.fileExists(excel) || !IoUtil.fileExists(goodsExcel)){
            return;
        }

        Reader_1 reader1 = new Reader_1();
        reader1.process(excel, 13);

        Print.info("京东商品数量："+reader1.goodsIdCarIdsMap.size());
        Print.info("淘汽车型数量："+reader1.carMap.size());

        CommReaderXLSX readerXLSX = new CommReaderXLSX(initGoodsIdAttrMap(), Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(goodsExcel, 1);
        Print.info("京东商品-电商商品关系数量："+readerXLSX.getDataList().size());

        //电商商品id - 京东商品id集合
        Map<Integer, Set<Integer>> dsGoodsIdMap = new HashMap<>();
        for(Map<String, String> data : readerXLSX.getDataList()){
            Integer jdGoodsId = Integer.valueOf(data.get("jd_id"));
            Integer dsGoodsId = Integer.valueOf(data.get("ds_id"));

            Set<Integer> jdIds = dsGoodsIdMap.get(dsGoodsId);
            if(jdIds==null){
                jdIds = new HashSet<>();
                dsGoodsIdMap.put(dsGoodsId, jdIds);
            }
            jdIds.add(jdGoodsId);
        }
        Print.info("电商商品数量："+dsGoodsIdMap.size());

        String tempGoodsCarTb = "temp_goods_car";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + tempGoodsCarTb+dateStr+".sql";
        writer = IoUtil.getWriter(sql);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGoodsCar(reader1.goodsIdCarIdsMap, dsGoodsIdMap, reader1.carMap, tempGoodsCarTb);

        IoUtil.closeWriter(writer);
    }

    public void handleGoodsCar(Map<Integer, Set<Integer>> jdGoodsCarIdsMap, Map<Integer, Set<Integer>> dsGoodsIdMap,
                               Map<Integer, GoodsCar> carMap, String table){

        for(Map.Entry<Integer, Set<Integer>> entry : dsGoodsIdMap.entrySet()){
            List<Integer> jdGoodsIdList = new ArrayList<>(entry.getValue());
            int size = jdGoodsIdList.size();
            Set<Integer> carIdSet = jdGoodsCarIdsMap.get(jdGoodsIdList.get(0));
            for(int i=1; i<size; i++){
                carIdSet.addAll(jdGoodsCarIdsMap.get(jdGoodsIdList.get(i)));
            }

            handleGcSql(entry.getKey(), carIdSet, carMap, table);
        }
    }

    public void handleGcSql(Integer dsGoodsId, Set<Integer> carIdSet, Map<Integer, GoodsCar> carMap, String table){
        int count = 1000;
        StringBuilder sb = new StringBuilder();
        List<Integer> carIdLst = new ArrayList<>(carIdSet);
        int size = carIdLst.size();
        int lastIndex = size - 1;
        for(int i=0; i<size; i++){
            GoodsCar gc = carMap.get(carIdLst.get(i));
            sb.append("(").append(dsGoodsId).append(",");
            sb.append(carIdLst.get(i)).append(",'");
            sb.append(gc.getCar_name()).append("',");
            sb.append(gc.getCar_brand_id()).append(",'");
            sb.append(gc.getCar_brand()).append("',");
            sb.append(gc.getCar_series_id()).append(",'");
            sb.append(gc.getCar_series()).append("',");
            sb.append(gc.getCar_model_id()).append(",'");
            sb.append(gc.getCar_model()).append("',");
            sb.append(gc.getCar_power_id()).append(",'");
            sb.append(gc.getCar_power()).append("',");
            sb.append(gc.getCar_year_id()).append(",'");
            sb.append(gc.getCar_year()).append("',1,@nowTime)");

            if((i+1)%count==0){
                writeGcSql(sb, table);
                sb.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeGcSql(sb, table);
            }
            sb.append(",");
        }
    }

    public void writeGcSql(StringBuilder valueSb, String table){
        StringBuilder sb = new StringBuilder();
        sb.append("insert ignore into ").append(table);
        sb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sb.append(" values ").append(valueSb).append(";\n");

        IoUtil.writeFile(writer, sb.toString());
    }


    // todo 生成最终插入 db_goods_car 的sql
    @Test
    public void testGoodsCar() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/";
        String excel = path + "电商新增goods-car.xls";
        if(!IoUtil.fileExists(excel)){
            return;
        }

        Reader_2 reader2 = new Reader_2();
        reader2.process(excel, 13);

        Print.info("gc数量："+reader2.goodsCarList.size());

        String goodsCarTb = "db_goods_car";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + "insertGoodsCar_"+dateStr+".sql";
        writer = IoUtil.getWriter(sql);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGcSql(reader2.goodsCarList, goodsCarTb);

        IoUtil.closeWriter(writer);
    }

    class Reader_2 extends ReadExcelXLS {
        private List<GoodsCar> goodsCarList;

        public Reader_2() {
            goodsCarList = new ArrayList<>();
        }

        @Override
        public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
            if(sheetIndex==0){
                if(curRow==0){
                    Print.info(sheetIndex + "#" + curRow + "  " + rowList.toString());
                }else{
                    handleData(rowList);
                }
            }else{
                handleData(rowList);
            }
        }

        private void handleData(List<String> rowList){
            GoodsCar gc = new GoodsCar();
            gc.setGoods_id(Integer.valueOf(rowList.get(0)));
            gc.setCar_id(Integer.valueOf(rowList.get(1)));
            gc.setCar_name(rowList.get(2));
            gc.setCar_brand_id(Integer.valueOf(rowList.get(3)));
            gc.setCar_brand(rowList.get(4));
            gc.setCar_series_id(Integer.valueOf(rowList.get(5)));
            gc.setCar_series(rowList.get(6));
            gc.setCar_model_id(Integer.valueOf(rowList.get(7)));
            gc.setCar_model(rowList.get(8));
            gc.setCar_power_id(Integer.valueOf(rowList.get(9)));
            gc.setCar_power(rowList.get(10));
            gc.setCar_year_id(Integer.valueOf(rowList.get(11)));
            gc.setCar_year(rowList.get(12));

            goodsCarList.add(gc);
        }
    }

    public void handleGcSql(List<GoodsCar> goodsCarList, String table){
        int count = 1000;
        StringBuilder sb = new StringBuilder();
        int size = goodsCarList.size();
        int lastIndex = size - 1;
        for(int i=0; i<size; i++){
            GoodsCar gc = goodsCarList.get(i);
            sb.append("(").append(gc.getGoods_id()).append(",");
            sb.append(gc.getCar_id()).append(",'");
            sb.append(gc.getCar_name()).append("',");
            sb.append(gc.getCar_brand_id()).append(",'");
            sb.append(gc.getCar_brand()).append("',");
            sb.append(gc.getCar_series_id()).append(",'");
            sb.append(gc.getCar_series()).append("',");
            sb.append(gc.getCar_model_id()).append(",'");
            sb.append(gc.getCar_model()).append("',");
            sb.append(gc.getCar_power_id()).append(",'");
            sb.append(gc.getCar_power()).append("',");
            sb.append(gc.getCar_year_id()).append(",'");
            sb.append(gc.getCar_year()).append("',1,@nowTime)");

            if((i+1)%count==0){
                writeGcSql(sb, table);
                sb.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeGcSql(sb, table);
            }
            sb.append(",");
        }
    }

}
