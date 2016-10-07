package 机油滤清器处理.生成sql脚本;

import base.BaseTest;
import dp.common.util.DateUtils;
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
 * Created by huangzhangting on 16/10/2.
 */
public class GoodsCarSqlTest extends BaseTest {
    private static Set<String> unMatchGoodsFormats = new HashSet<>();
    private static Set<String> matchGoodsIds = new HashSet<>();
    private static List<Map<String, Object>> needDeleteGoodsCars = new ArrayList<>(); //需要删除的关系数据


    @Test
    public void justTest() throws Exception{

    }

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/机滤数据处理/商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "car_models_id");
        attrMap.put("商品编码", "goods_format");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(path + "机滤覆盖的车款信息-增加-20160929.xlsx", 1);

        List<Map<String, String>> yxAddDataList = readerXLSX.getDataList();
        Print.info(yxAddDataList.size());
        Print.info(yxAddDataList.get(0));

        //验证过的数据
        List<Map<String, String>> checkedList = getCheckedDataList();

        List<Map<String, Object>> yxDataList = oneToOneDataList(BrandEnum.YUN_XIU.getCode()); //云修机滤
        Print.info(yxDataList.size());
        yxDataList.addAll(ObjectUtil.strToObjMapList(yxAddDataList));
        Print.info(yxDataList.size());

        yxDataList = grepDataList(checkedList, yxDataList);
        Print.info("验证后的云修覆盖量："+yxDataList.size());

        List<Map<String, Object>> bsDataList = oneToOneDataList(BrandEnum.BO_SHI.getCode()); //博世机滤
        Print.info(bsDataList.size());
        bsDataList = grepDataList(checkedList, bsDataList);
        Print.info("验证后的博世覆盖量："+bsDataList.size());

        List<Map<String, Object>> acDataList = oneToOneDataList(BrandEnum.AC_DE_KE.getCode()); //AC德科机滤
        Print.info(acDataList.size());
        acDataList = grepDataList(checkedList, acDataList);
        Print.info("验证后的AC德科覆盖量："+acDataList.size());

        List<Map<String, Object>> bwDataList = supplyGoodsCarDataList("豹王机滤可补充的数据.xls");
        List<Map<String, Object>> mlDataList = supplyGoodsCarDataList("马勒机滤可补充的数据.xls");
        List<Map<String, Object>> hyDataList = supplyGoodsCarDataList("海业机滤可补充的数据.xls");

        //电商商品数据
        List<Map<String, String>> yxGoodsList = getGoodsDataList("云修机油滤清器.xls");
        List<Map<String, String>> bsGoodsList = getGoodsDataList("博世机油滤清器.xls");
        List<Map<String, String>> acGoodsList = getGoodsDataList("AC德科机油滤清器.xls");
        List<Map<String, String>> bwGoodsList = getGoodsDataList("豹王机滤.xls");
        List<Map<String, String>> mlGoodsList = getGoodsDataList("马勒机滤.xls");
        List<Map<String, String>> hyGoodsList = getGoodsDataList("海业机滤.xls");


        /** 开始处理关系数据 */
        List<Map<String, Object>> goodsCarList = new ArrayList<>();
        //全部的车款信息
        List<Map<String, Object>> carInfoList = getCarInfoList();
        Print.info(carInfoList.size());
        Print.info(carInfoList.get(0));

        handleGoodsCarList(yxGoodsList, yxDataList, carInfoList, goodsCarList); //云修
        Print.info("云修关系数据处理后："+goodsCarList.size());

        handleGoodsCarList(bsGoodsList, bsDataList, carInfoList, goodsCarList); //博世
        Print.info("博世关系数据处理后："+goodsCarList.size());

        handleGoodsCarList(acGoodsList, acDataList, carInfoList, goodsCarList); //AC德科
        Print.info("AC德科关系数据处理后："+goodsCarList.size());

        handleGoodsCarList(bwGoodsList, bwDataList, carInfoList, goodsCarList); //豹王
        Print.info("豹王关系数据处理后："+goodsCarList.size());

        handleGoodsCarList(mlGoodsList, mlDataList, carInfoList, goodsCarList); //马勒
        Print.info("马勒关系数据处理后："+goodsCarList.size());

        handleGoodsCarList(hyGoodsList, hyDataList, carInfoList, goodsCarList); //海业
        Print.info("海业关系数据处理后："+goodsCarList.size());

        Print.info("不存在的商品编码："+unMatchGoodsFormats);
        Print.info("匹配上的商品id："+matchGoodsIds.size());

        //处理sql
        handleDeleteSql();

        handleSql(goodsCarList);


        //导出excel数据
        ExcelExporter.exportCoverCars(path, goodsCarList);

    }

    private List<Map<String, Object>> oneToOneDataList(int code){
        return commonMapper.selectListBySql(StatisticConfig.oneToOneDataSql(code));
    }

    //修订后的数据
    private List<Map<String, String>> getCheckedDataList() throws Exception{
        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/数据校验/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("id", "carId");
        attrMap.put("商品编码", "goodsFormat");
        attrMap.put("备注", "remark");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processOneSheet(filePath + "机滤覆盖车型信息处理汇总表.xlsx", 1);

        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        List<Map<String, String>> resultList = new ArrayList<>();
        for(Map<String, String> data : dataList){
            if(!StringUtils.isEmpty(data.get("remark"))){
                resultList.add(data);
            }
        }
        Print.info(resultList.size());
        Print.info(resultList.get(0));

        return resultList;
    }

    //过滤掉，人工检查后，错误的数据
    private List<Map<String, Object>> grepDataList(List<Map<String, String>> checkedList, List<Map<String, Object>> dataList){
        List<Map<String, Object>> list = new ArrayList<>();
        for(Map<String, Object> data : dataList){
            String carId = data.get("car_models_id").toString();
            String goodsFormat = data.get("goods_format").toString();
            boolean flag = true;
            for(Map<String, String> cd : checkedList){
                if(carId.equals(cd.get("carId")) && goodsFormat.equals(cd.get("goodsFormat"))){
                    flag = false;
                    break;
                }
            }
            if(flag){
                list.add(data);
            }
        }

        return list;
    }

    private List<Map<String, Object>> supplyGoodsCarDataList(String fileName) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/补充数据/待处理数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("gc2.car_models_id", "car_models_id");
        attrMap.put("gc2.goods_format", "goods_format");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return ObjectUtil.strToObjMapList(dataList);
    }

    //电商商品数据
    private List<Map<String, String>> getGoodsDataList(String fileName) throws Exception{

        String filePath = "/Users/huangzhangting/Desktop/机滤数据处理/电商机滤商品数据/";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("goods_id", "goodsId");
        attrMap.put("goods_format", "goodsFormat");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(filePath + fileName, attrMap.size());

        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(fileName+"  "+dataList.size());
        Print.info(dataList.get(0));

        return dataList;
    }


    /** 关系数据处理 */
    private List<Map<String, Object>> getCarInfoList(){
        String sql = "select brand,brand_id,series,series_id,model,model_id," +
                "power,power_id,year,year_id,car_models as car_name,car_models_id as car_id, company " +
                "from db_car_all group by car_models_id";

        return commonMapper.selectListBySql(sql);
    }
    private Map<String, Object> getCarInfo(String carId, List<Map<String, Object>> carInfoList){
        for(Map<String, Object> car : carInfoList){
            if(carId.equals(car.get("car_id").toString())){
                return ObjectUtil.copyMap(car);
            }
        }
        return null;
    }

    private void handleGoodsCarList(List<Map<String, String>> goodsList, List<Map<String, Object>> carList,
                                    List<Map<String, Object>> carInfoList, List<Map<String, Object>> goodsCarList){

        for(Map<String, Object> car : carList){
            String goodsFormat = car.get("goods_format").toString();
            String carId = car.get("car_models_id").toString();

            boolean flag = false;

            for(Map<String, String> goods : goodsList){
                String format = goods.get("goodsFormat");
                if(goodsFormat.replace(" ", "").equals(format.replace(" ", ""))){
                    Map<String, Object> gc = getCarInfo(carId, carInfoList);
                    if(gc!=null){
                        gc.put("goods_id", goods.get("goodsId"));
                        gc.put("goods_format", format);
                        goodsCarList.add(gc);

                        matchGoodsIds.add(goods.get("goodsId"));

                    }else{
                        Print.info("错误的车款id："+car);
                    }

                    flag = true;
                }
            }

            if(!flag){
                unMatchGoodsFormats.add(goodsFormat);
            }
        }

    }


    /** 处理sql */
    //处理需要删除的数据
    private void handleDeleteSql(){
        if(matchGoodsIds.isEmpty()){
            return;
        }
        List<String> goodsIdList = new ArrayList<>(matchGoodsIds);

        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(sqlPath + "delete_car_oil_filter_" + dateStr + ".sql");

        int count = 50;
        int size = goodsIdList.size();
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            sql.append(goodsIdList.get(i));
            if((i+1)%count==0){
                writeDeleteSql(sql);
                sql.setLength(0);
                continue;
            }
            if(lastIndex==i){
                writeDeleteSql(sql);
                break;
            }
            sql.append(",");
        }

        IoUtil.closeWriter(writer);
    }
    private void writeDeleteSql(StringBuilder sql){
        sql.insert(0, "delete from db_goods_car where goods_id in(");
        sql.append(");\n");

        IoUtil.writeFile(writer, sql.toString());
    }


    //处理sql
    public void handleSql(List<Map<String, Object>> goodsCarList){
        Print.info(goodsCarList.size());
        Print.info(goodsCarList.get(0));

        String sqlPath = path + "sql/";
        IoUtil.mkdirsIfNotExist(sqlPath);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(sqlPath + "add_car_oil_filter_" + dateStr + ".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        int count = 500;
        int size = goodsCarList.size();
        int lastIdx = size - 1;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<size; i++){
            addGcVal(sb, goodsCarList.get(i));
            if((i+1)%count==0){
                writeGcSql(sb);
                sb.setLength(0);
                continue;
            }
            if(lastIdx==i){
                writeGcSql(sb);
                break;
            }
            sb.append(",");
        }

        IoUtil.closeWriter(writer);
    }

    public void addGcVal(StringBuilder sb, Map<String, Object> data){
        sb.append("(");
        sb.append(data.get("goods_id")).append(",");
        sb.append(data.get("car_id")).append(",'");
        sb.append(data.get("car_name")).append("',");
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
    public void writeGcSql(StringBuilder sb){
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("insert ignore into db_goods_car");
        sqlSb.append("(goods_id,car_id,car_name,car_brand_id,car_brand,car_series_id,car_series,car_model_id,car_model,car_power_id,car_power,car_year_id,car_year,status,gmt_create)");
        sqlSb.append(" values ").append(sb).append(";\n");

        IoUtil.writeFile(writer, sqlSb.toString());
    }

}
