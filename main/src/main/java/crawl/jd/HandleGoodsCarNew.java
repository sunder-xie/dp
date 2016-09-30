package crawl.jd;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.io.Writer;
import java.util.*;

/**
 * Created by huangzhangting on 16/4/13.
 */
public class HandleGoodsCarNew extends BaseTest {

    private String path;
    private Writer writer;

    public Map<String, String> initLyGoodsAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("c.liyang_id_list", "ly_ids");
        attrMap.put("gc.goods_id", "g_id");

        return attrMap;
    }


    // TODO 步骤一： 记得修改
    private static final String TYPE = "电瓶";
    private static final String EXCEL = "京东匹配上的"+TYPE+"-20160509.xlsx";

    // todo 生成力洋id-京东商品id，对应关系
    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/"+TYPE+"/";

        String excel = path + "力洋id-商品id.xls";

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
        IoUtil.writeFile(writer, "truncate table ly_id_goods;\n");

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


    public Map<String, String> initGoodsIdAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("jd_id", "jd_id");
        attrMap.put("ds_id", "ds_id");

        return attrMap;
    }


    // todo ============= 生成插入临时表 temp_goods_car 的数据 ==============
    // 必须先将 ly_id_goods 的数据导入
    @Test
    public void testGc() throws Exception{

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/"+TYPE+"/";

        String goodsPath = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/"+TYPE+"/";

        String goodsExcel = goodsPath + EXCEL;

        if(!IoUtil.fileExists(goodsExcel)){
            return;
        }

        Print.info("处理："+TYPE);

        CommReaderXLSX readerXLSX = new CommReaderXLSX(initGoodsIdAttrMap(), Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(goodsExcel, 1);
        Print.info("京东商品-电商商品关系数量：" + readerXLSX.getDataList().size());
        if(readerXLSX.getDataList().isEmpty()){
            return;
        }

        //京东商品id - 电商商品id集合
        Map<String, Set<String>> jdGoodsIdMap = new HashMap<>();
        for(Map<String, String> data : readerXLSX.getDataList()){
            String jdGoodsId = data.get("jd_id");

            Set<String> dsIds = jdGoodsIdMap.get(jdGoodsId);
            if(dsIds==null){
                dsIds = new HashSet<>();
                jdGoodsIdMap.put(jdGoodsId, dsIds);
            }
            dsIds.add(data.get("ds_id"));
        }
        Print.info("京东商品数量："+jdGoodsIdMap.size());


        String tempGoodsCarTb = "temp_goods_car";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + tempGoodsCarTb+dateStr+".sql";
        writer = IoUtil.getWriter(sql);
        IoUtil.writeFile(writer, "truncate table temp_goods_car;\n");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGoodsIdMap(jdGoodsIdMap, tempGoodsCarTb);

        IoUtil.closeWriter(writer);
    }

    public void handleGoodsIdMap(Map<String, Set<String>> jdGoodsIdMap, String table){
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        for(Map.Entry<String, Set<String>> entry : jdGoodsIdMap.entrySet()){

            String sql = Sql.getGcSql(entry.getKey());

            List<Map<String, Object>> list = commonMapper.selectListBySql(sql);

            addGoodsCar(dataMap, entry.getValue(), list);
        }
        Print.info("电商goodsCar数量："+dataMap.size());
        handleGoodsCar(new ArrayList<>(dataMap.values()), table);
    }

    public void addGoodsCar(Map<String, Map<String, Object>> dataMap, Set<String> dsIdSet, List<Map<String, Object>> goodsCarList){
        if(goodsCarList.isEmpty()){
            return;
        }
        for(String dsId : dsIdSet){
            for(Map<String, Object> gc : goodsCarList){
                String key = dsId+"-"+gc.get("car_models_id");
                if(dataMap.get(key)!=null){
                    continue;
                }

                Map<String, Object> data = new HashMap<>(gc);
                data.put("goods_id", dsId);
                dataMap.put(key, data);
            }
        }
    }

    public void handleGoodsCar(List<Map<String, Object>> dataList, String table){
        if(dataList.isEmpty()){
            return;
        }

        int count = 1000;

        StringBuilder sb = new StringBuilder();
        int size = dataList.size();
        int lastIndex = size - 1;
        for(int i=0; i<size; i++){

            initSb(sb, dataList.get(i));

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

    public void initSb(StringBuilder sb, Map<String, Object> data){
        sb.append("(").append(data.get("goods_id")).append(",");
        sb.append(data.get("car_models_id")).append(",'");
        sb.append(data.get("car_models")).append("',");
        sb.append(data.get("brand_id")).append(",'");
        sb.append(data.get("brand")).append("',");
        sb.append(data.get("series_id")).append(",'");
        sb.append(data.get("series")).append("',");
        sb.append(data.get("model_id")).append(",'");
        sb.append(data.get("model")).append("',");
        sb.append(data.get("power_id")).append(",'");
        sb.append(data.get("power")).append("',");
        sb.append(data.get("year_id")).append(",'");
        sb.append(data.get("year")).append("',1,@nowTime)");
    }

    public void writeGcSql(StringBuilder valueSb, String table){
        StringBuilder sb = new StringBuilder();
        sb.append("insert ignore into ").append(table);
        sb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sb.append(" values ").append(valueSb).append(";\n");

        IoUtil.writeFile(writer, sb.toString());
    }


    // 处理汽机油时需注意
    // select * from temp_goods_car where car_name like '%柴油%' or car_name like '%电动%';

    // todo =========== 生成最终插入 db_goods_car 的sql ==============
    // 必须先将 temp_goods_car 数据导入
    @Test
    public void testGoodsCar(){
        List<Map<String, Object>> newGoodsCarList = commonMapper.selectListBySql(Sql.selectNewGcSql());
        if(newGoodsCarList.isEmpty()){
            return;
        }
        Print.info(TYPE+"\n新增goodsCar数量: "+newGoodsCarList.size());

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/"+TYPE+"/";

        String goodsCarTb = "db_goods_car";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + "insertGoodsCar_"+dateStr+".sql";
        writer = IoUtil.getWriter(sql);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGoodsCar2(newGoodsCarList, goodsCarTb);

        IoUtil.closeWriter(writer);
    }

    public void handleGoodsCar2(List<Map<String, Object>> dataList, String table){
        if(dataList.isEmpty()){
            return;
        }

        Set<String> modelIds = new HashSet<>();
        Set<String> carIds = new HashSet<>();

        int count = 1000;

        StringBuilder sb = new StringBuilder();
        int size = dataList.size();
        int lastIndex = size - 1;
        for(int i=0; i<size; i++){

            statistics(dataList.get(i), modelIds, carIds);

            initSb2(sb, dataList.get(i));

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

        Print.info("覆盖车型: "+modelIds.size());
        Print.info("覆盖车款: "+carIds.size());
    }

    public void initSb2(StringBuilder sb, Map<String, Object> data){
        sb.append("(").append(data.get("goods_id")).append(",");
        sb.append(data.get("car_id")).append(",'");
        sb.append(data.get("car_name")).append("',");
        sb.append(data.get("car_brand_id")).append(",'");
        sb.append(data.get("car_brand")).append("',");
        sb.append(data.get("car_series_id")).append(",'");
        sb.append(data.get("car_series")).append("',");
        sb.append(data.get("car_model_id")).append(",'");
        sb.append(data.get("car_model")).append("',");
        sb.append(data.get("car_power_id")).append(",'");
        sb.append(data.get("car_power")).append("',");
        sb.append(data.get("car_year_id")).append(",'");
        sb.append(data.get("car_year")).append("',1,@nowTime)");
    }

    private void statistics(Map<String, Object> data, Set<String> modelIds, Set<String> carIds){
        modelIds.add(data.get("car_model_id").toString());
        carIds.add(data.get("car_id").toString());
    }


    // todo db_goods_car 数据额外补充
    @Test
    public void testAddExt(){
        String sql = Sql.selectGcSql(286628);
        List<Map<String, Object>> goodsCarList = commonMapper.selectListBySql(sql);
        Print.info(goodsCarList.size());

        List<Integer> needAddGoodsIds = new ArrayList<>();
        needAddGoodsIds.add(23418);
        needAddGoodsIds.add(23330);

        List<Map<String, Object>> addGcList = new ArrayList<>();
        for(Integer goodsId : needAddGoodsIds){
            for(Map<String, Object> gc : goodsCarList){
                Map<String, Object> newGc = new HashMap<>(gc);
                newGc.put("goods_id", goodsId);
                addGcList.add(newGc);
            }
        }

        Print.info("新增goodsCar数量："+addGcList.size());

        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/"+TYPE+"/";

        String goodsCarTb = "db_goods_car";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = path + "addGoodsCar_"+dateStr+".sql";
        writer = IoUtil.getWriter(sqlFile);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        handleGoodsCar2(addGcList, goodsCarTb);

        IoUtil.closeWriter(writer);
    }

}
