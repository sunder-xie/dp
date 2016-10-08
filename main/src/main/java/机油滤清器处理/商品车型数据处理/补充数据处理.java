package 机油滤清器处理.商品车型数据处理;

import base.BaseTest;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;
import org.springframework.util.StringUtils;
import 机油滤清器处理.BrandEnum;

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

        //TODO 读取箭冠商品
        List<Map<String, String>> goodsList = new ArrayList<>();

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
        //ExcelExporter.exportCarGoodsData(path, "箭冠可以补充的机滤", effectiveList);

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

}
