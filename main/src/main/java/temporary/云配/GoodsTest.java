package temporary.云配;

import base.BaseTest;
import dp.common.util.*;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/7/8.
 */
public class GoodsTest extends BaseTest {
    Set<String> goodsIdSet;

    @Test
    public void test() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云配商品处理/";
        String excel = path + "公告号匹配力洋数据-160707.xlsx";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("公告号", "noticeNo");
        attrMap.put("力洋id", "lyId");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(attrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        writer = IoUtil.getWriter(path + "addSql.sql");

        for(Map<String, String> data : dataList){
            String no = data.get("noticeNo");
            String id = data.get("lyId");
            if("".equals(no) || "".equals(id)){
                continue;
            }

            String sql = "insert into temp_notice_lyid_rel(notice_no,ly_id) value('"+no
                    +"', '"+id+"');\n";

            IoUtil.writeFile(writer, sql);
        }

    }


    @Test
    public void test2() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/云配商品处理/";

        //云配商品
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t1.goods_id", "goodsId");
        attrMap.put("t1.goods_name", "goodsName");
        attrMap.put("t1.car_type_alias", "carType");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云配商品.xls", attrMap.size());
        List<Map<String, String>> ypGoodsList = readerXLS.getDataList();
        Print.info(ypGoodsList.get(0));

        //名称标准化
        attrMap = new HashMap<>();
        attrMap.put("名称", "goodsName");
        attrMap.put("标准名称", "partName");
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "云配商品名称-0708.xls", attrMap.size());
        List<Map<String, String>> nameObjList = readerXLS.getDataList();
        Print.info(nameObjList.get(0));
        Map<String, String> nameMap = new HashMap<>();
        for(Map<String, String> nameObj : nameObjList){
            String partName = nameObj.get("partName");
            if("".equals(partName)){
                continue;
            }
            nameMap.put(nameObj.get("goodsName"), partName);
        }

        //车型匹配
        attrMap = new HashMap<>();
        attrMap.put("t1.notice_no", "noticeNo");
        attrMap.put("t2.model_id", "modelId");
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "五菱车型.xls", attrMap.size());
        List<Map<String, String>> carList = readerXLS.getDataList();
        Print.info(carList.get(0));

        //五菱配件数据
        attrMap = new HashMap<>();
        attrMap.put("g.oe_number", "oeNum");
        attrMap.put("g.part_name", "partName");
        attrMap.put("gc.model_id", "modelId");
        readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(path + "五菱配件数据.xls", attrMap.size());
        List<Map<String, String>> goodsList = readerXLS.getDataList();
        Print.info(goodsList.get(0));


        Print.info("开始处理商品");
        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "update_oe_num_"+dateStr+".sql");
        IoUtil.writeFile(writer, "select @nowTime := now();\n");

        goodsIdSet = new HashSet<>();
        for(Map<String, String> ypGoods : ypGoodsList){
            handleGoods(ypGoods, nameMap, carList, goodsList);

        }

        IoUtil.closeWriter(writer);

        Print.info(goodsIdSet.toString());
    }

    public void handleGoods(Map<String, String> ypGoods, Map<String, String> nameMap, List<Map<String, String>> carList,
                            List<Map<String, String>> goodsList){
        String partName = nameMap.get(ypGoods.get("goodsName"));
        if(partName==null){
            return;
        }

        Set<String> modelIdSet = new HashSet<>();
        for(Map<String, String> car : carList){
            if(ypGoods.get("carType").equals(car.get("noticeNo"))){
                modelIdSet.add(car.get("modelId"));
            }
        }
        if(modelIdSet.isEmpty()){
            return;
        }

        ypGoods.put("partName", partName);
        for(Map<String, String> goods : goodsList){
            if(partName.equals(goods.get("partName")) && modelIdSet.contains(goods.get("modelId"))){
                ypGoods.put("oeNum", goods.get("oeNum"));
                Print.info(ypGoods);

                writeSql(ypGoods.get("goodsId"), goods.get("oeNum"));

                break;
            }
        }

    }

    public void writeSql(String goodsId, String oeNum){
        goodsIdSet.add(goodsId);

        String sql = "update db_goods set gmt_modified=@nowTime, oe_num='"+oeNum
                +"' where goods_id="+goodsId+";\n";

        IoUtil.writeFile(writer, sql);
    }

}
