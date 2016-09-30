package crawl.jd.goods;

import crawl.jd.BrandUtil;
import crawl.jd.Export;
import crawl.jd.Init;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;

import java.util.*;

/**
 * 处理规则通用的商品
 * Created by huangzhangting on 16/4/11.
 */
public class HandleGoods {

    private String path;
    private String type;


    @Test
    public void testGoods() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/";

        type = "电瓶";
//        String excel = path + type+ "-20160414.xlsx";
        String excel = path + "京东瓦尔塔电瓶-20160509.xlsx";
//        String dsExcel = path + "电商"+type+".xls";
        String dsExcel = path + "云修电瓶-20160509.xls";

        if(!IoUtil.fileExists(excel) || !IoUtil.fileExists(dsExcel)){
            return;
        }

        Print.info("# ========== "+type+"处理 ==========\n");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(Init.initJdAttrMap(), Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);

        Print.info("京东数据："+readerXLSX.getDataList().size());

        Map<String, String> dsAttrMap = Init.initDsAttrMap();
        CommReaderXLS readerXLS = new CommReaderXLS(dsAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(dsExcel, dsAttrMap.size());

        Print.info("电商数据："+readerXLS.getDataList().size());

        compareGoods(readerXLSX.getDataList(), readerXLS.getDataList());
    }

    public void compareGoods(List<Map<String, String>> jdGoodsList, List<Map<String, String>> dsGoodsList){
        if(jdGoodsList.isEmpty() || dsGoodsList.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Map<String, List<Map<String, String>>> matchGoodsMap = new HashMap<>();
        List<Map<String, String>> unMatchGoods = new ArrayList<>();

        Set<String> dsMatchIdSet = new HashSet<>();

        for(Map<String, String> jdg : jdGoodsList){
            String brand = "淘汽云修";//BrandUtil.handleBrand(jdg.get("京东品牌"));
            String param = StrUtil.toUpCase(jdg.get("匹配参数"));

            if("".equals(brand) || "".equals(param)){
                unMatchGoods.add(jdg);
                continue;
            }
            boolean unMatchFlag = true;
            for(Map<String, String> dsg : dsGoodsList){
                if(dsg.get("电商商品名称").contains("重复商品")){
                    continue;
                }
                if(dsg.get("电商品牌").contains(brand) && compareParam(jdg.get("京东商品名称"), dsg.get("规格型号"))){
                    List<Map<String, String>> list = matchGoodsMap.get(jdg.get("jd_id"));
                    if(list==null){
                        list = new ArrayList<>();
                        list.add(jdg);//第一个是京东商品
                        matchGoodsMap.put(jdg.get("jd_id"), list);
                    }
                    list.add(dsg);
                    dsMatchIdSet.add(dsg.get("ds_id"));
                    unMatchFlag = false;
                }
            }

            if(unMatchFlag){
                unMatchGoods.add(jdg);
            }
        }

        List<Map<String, String>> dsUnMatchGoods = new ArrayList<>();
        for(Map<String, String> dsg : dsGoodsList){
            if(dsMatchIdSet.contains(dsg.get("ds_id"))){
                continue;
            }
            dsUnMatchGoods.add(dsg);
        }

        Print.info("京东匹配上的商品："+matchGoodsMap.size());
        Print.info("京东没匹配上的商品："+unMatchGoods.size());
        Print.info("电商没匹配上的商品："+dsUnMatchGoods.size());


        Export export = new Export(path, type);

        String[] headList = new String[]{"jd_id", "京东商品名称", "京东品牌", "匹配参数"};
        String[] headList2 = new String[]{"ds_id", "电商商品名称", "电商品牌", "规格型号"};

        export.exportExcel(unMatchGoods, matchGoodsMap, dsUnMatchGoods, headList, headList2);
    }

//    private boolean compareParam(String param, String goodsFormat){
//        goodsFormat = StrUtil.toUpCase(goodsFormat);
//        return goodsFormat.equals(param);
//    }

    private boolean compareParam(String jdGoodsName, String dsGoodsFormat){
        String goodsFormat = StrUtil.toUpCase(dsGoodsFormat);
        String name = StrUtil.toUpCase(jdGoodsName);
        return name.contains(goodsFormat);
    }

    private boolean compareParam(String param, Map<String, String> dsg){
        String goodsFormat = StrUtil.toUpCase(dsg.get("规格型号"));
        if(goodsFormat.contains(param) || param.contains(goodsFormat)){
            return true;
        }
        String goodsName = StrUtil.toUpCase(dsg.get("电商商品名称"));
        if(goodsName.contains(param)){
            return true;
        }

        param = param.replace("（", "/").replace("(", "/").replace("）","").replace(")","");

        if (param.contains("/")){
            String[] params = param.split("/");
            for(String str : params){
                if(str.length()<3){
                    continue;
                }
                if(goodsFormat.contains(str) || goodsName.contains(str)){
                    return true;
                }
            }
        }
        return false;
    }

}
