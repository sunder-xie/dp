package crawl.jd.goods;

import crawl.jd.Export;
import crawl.jd.Init;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.CommReaderXLSX;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 处理机油
 * Created by huangzhangting on 16/4/11.
 */
public class HandleOil {

    private String path;
    private String type;

    private Map<String, Set<String>> brandKwMap;


    private void initOilAttrMap(Map<String, String> jdAttrMap){
        jdAttrMap.put("机油等级", "机油等级");
    }

    @Test
    public void testOil() throws Exception{
        path = "/Users/huangzhangting/Documents/数据处理/爬取数据/京东/商品数据/";

        type = "机油";
        String excel = path + "京东机油-20160413.xlsx";
        String dsExcel = path + "电商汽机油.xls";

        if(!IoUtil.fileExists(excel) || !IoUtil.fileExists(dsExcel)){
            return;
        }

        Map<String, String> jdAttrMap = Init.initJdAttrMap();
        initOilAttrMap(jdAttrMap);

        CommReaderXLSX readerXLSX = new CommReaderXLSX(jdAttrMap, Constant.TYPE_LIST, 0);
        readerXLSX.processOneSheet(excel, 1);

        Print.info("京东数据："+readerXLSX.getDataList().size());

        Map<String, String> dsAttrMap = Init.initDsAttrMap();
        CommReaderXLS readerXLS = new CommReaderXLS(dsAttrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(dsExcel, dsAttrMap.size());

        Print.info("电商数据："+readerXLS.getDataList().size());

        brandKwMap = Init.intBrandKwMap();

        compareOil(readerXLSX.getDataList(), readerXLS.getDataList());
    }

    private boolean compareLevel(String jdLevel, String dsLevel){
        jdLevel = jdLevel.toUpperCase();
        dsLevel = dsLevel.toUpperCase();

        if(jdLevel.contains(dsLevel) || dsLevel.contains(jdLevel)){
            return true;
        }
        return false;
    }

    private boolean compareKeyword(String brand, Map<String, String> jdGoods, Map<String, String> dsGoods){
        Set<String> kwSet = brandKwMap.get(brand);
        if(CollectionUtils.isEmpty(kwSet)){
            return true;
        }
        String jdName = jdGoods.get("京东商品名称");
        String dsName = dsGoods.get("电商商品名称");
        boolean flag = true;
        for(String kw : kwSet){
            if(jdName.contains(kw) && dsName.contains(kw)){
                return true;
            }
            if(jdName.contains(kw) || dsName.contains(kw)){
                flag = false;
            }
        }
        return flag;
    }

    public void compareOil(List<Map<String, String>> jdGoodsList, List<Map<String, String>> dsGoodsList){
        if(jdGoodsList.isEmpty() || dsGoodsList.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Map<String, List<Map<String, String>>> matchGoodsMap = new HashMap<>();
        List<Map<String, String>> unMatchGoodsList = new ArrayList<>();

        Set<String> dsMatchGoodsIds = new HashSet<>();

        for(Map<String, String> jdg : jdGoodsList){
            String jdBrand = StrUtil.repNotCN(jdg.get("京东品牌"));
            boolean unMatchFlag = true;
            for(Map<String, String> dsg : dsGoodsList){
                String goodsFormat = dsg.get("规格型号");
                if("".equals(goodsFormat)){
                    continue;
                }

                if(dsg.get("电商品牌").contains(jdBrand) && compareKeyword(jdBrand, jdg, dsg)){
                    goodsFormat = goodsFormat.toUpperCase();
                    List<String> formatList = Arrays.asList(goodsFormat.split(" "));
                    int idx = formatList.indexOf(jdg.get("匹配参数").toUpperCase());
                    if(idx>0){
                        if(compareLevel(jdg.get("机油等级"), formatList.get(idx-1))){

                            List<Map<String, String>> matchList = matchGoodsMap.get(jdg.get("jd_id"));
                            if(matchList==null){
                                matchList = new ArrayList<>();
                                matchList.add(jdg); //第一个是京东商品
                                matchGoodsMap.put(jdg.get("jd_id"), matchList);
                            }
                            matchList.add(dsg);
                            dsMatchGoodsIds.add(dsg.get("ds_id"));
                            unMatchFlag = false;
                        }
                    }
                }
            }

            if(unMatchFlag){
                unMatchGoodsList.add(jdg);
            }
        }

        Print.info("京东匹配上的商品："+matchGoodsMap.size());
        Print.info("京东没匹配上的商品："+unMatchGoodsList.size());

        List<Map<String, String>> dsUnMatchGoodsList = new ArrayList<>();
        for(Map<String, String> dsg : dsGoodsList){
            if(dsMatchGoodsIds.contains(dsg.get("ds_id"))){
                continue;
            }
            dsUnMatchGoodsList.add(dsg);
        }
        Print.info("电商没匹配上的商品："+dsUnMatchGoodsList.size());
        Print.info("电商匹配上的商品："+dsMatchGoodsIds.size());

        //导出Excel
        Export export = new Export(path, type);

        String[] headList = new String[]{"jd_id", "京东商品名称", "京东品牌", "匹配参数", "机油等级"};
        String[] headList2 = new String[]{"ds_id", "电商商品名称", "电商品牌", "规格型号"};

        export.exportExcel(unMatchGoodsList, matchGoodsMap, dsUnMatchGoodsList, headList, headList2);
    }

}
