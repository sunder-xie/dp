package 车辆违章;

import base.BaseTest;
import com.fasterxml.jackson.databind.JsonNode;
import dp.common.util.JsonUtil;
import dp.common.util.LocalConfig;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLSX;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.http.HttpClientResult;
import dp.common.util.http.HttpClientUtil;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/11.
 */
public class JiSuWeiZhangTest extends BaseTest{
    private static final String APP_KEY = LocalConfig.JI_SU_APP_KEY;

    //获取支持城市参数接口
    @Test
    public void testCity(){
        String url = "http://api.jisuapi.com/illegal/carorg";
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("appkey", APP_KEY));
        nameValuePairList.add(new BasicNameValuePair("onlysupport", "1"));
        HttpClientResult httpClientResult = HttpClientUtil.get(url, nameValuePairList);
        if(httpClientResult==null){
            return;
        }
        System.out.println(httpClientResult.getData());
        JsonNode result = JsonUtil.getJsonNode(httpClientResult.getData());
        if(result!=null){
            int status = result.findValue("status").asInt();
            if(status==0){
                result = result.findValue("result");
                JsonNode data = result.findValue("data");
                Iterator<JsonNode> jsonNodeElements = data.elements();
                List<WzProvince> provinceList = new ArrayList<>();
                while (jsonNodeElements.hasNext()){
                    WzProvince province = getWzProvince(jsonNodeElements.next());
                    provinceList.add(province);
                }
                System.out.println(provinceList.size());
                System.out.println(provinceList.get(0));

                //导出城市站信息
                export(provinceList);
            }
        }

    }

    private WzProvince getWzProvince(JsonNode jsonNode){
        //System.out.println(jsonNode.toString());

        WzProvince province = new WzProvince();
        province.setProvinceName(jsonNode.findValue("province").asText());
        province.setCityList(getWzCityList(jsonNode.findValue("list"), jsonNode));
        return province;
    }
    private List<WzCity> getWzCityList(JsonNode jsonNode, JsonNode provinceNode){
        //System.out.println(jsonNode.toString());

        List<WzCity> list = new ArrayList<>();
        if(jsonNode==null){
            WzCity city = getWzCity(provinceNode);
            list.add(city);
            return list;
        }

        Iterator<JsonNode> jsonNodeElements = jsonNode.elements();
        while (jsonNodeElements.hasNext()){
            WzCity city = getWzCity(jsonNodeElements.next());
            list.add(city);
        }
        return list;
    }
    private WzCity getWzCity(JsonNode jsonNode){
        WzCity city = new WzCity();
        JsonNode cityName = jsonNode.findValue("city");
        if(cityName==null){
            city.setCityName(jsonNode.findValue("province").asText());
        }else{
            city.setCityName(cityName.asText());
        }
        city.setCityCode(jsonNode.findValue("carorg").asText());
        city.setEngineNo(jsonNode.findValue("engineno").asInt());
        city.setVinNo(jsonNode.findValue("frameno").asInt());
        city.setLsPrefix(jsonNode.findValue("lsprefix").asText());
        city.setLsNum(jsonNode.findValue("lsnum").asText());

        return city;
    }

    @Data
    private class WzProvince{
        private String provinceName;
        private String provinceCode;
        private List<WzCity> cityList;
    }
    @Data
    private class WzCity{
        private String lsPrefix;
        private String lsNum;
        private String cityName;
        private String cityCode;
        private Integer engineNo; //发动机号需要输入的长度，100为全部输入 0为不输入
        private Integer vinNo; //车架号需要输入的长度，100为全部输入 0为不输入

    }

    public void export(List<WzProvince> provinceList){
        List<Map<String, String>> list = new ArrayList<>();
        for(WzProvince province : provinceList){
            for(WzCity city : province.getCityList()){
                Map<String, String> map = new HashMap<>();
                map.put("provinceName", province.getProvinceName());
                map.put("cityName", city.getCityName());
                map.put("cityCode", city.getCityCode());
                map.put("engineNo", city.getEngineNo()+"");
                map.put("vinNo", city.getVinNo()+"");
                map.put("lsPrefix", city.getLsPrefix()+city.getLsNum());
                list.add(map);
            }
        }

        String[] heads = new String[]{"省份", "城市", "城市编码", "车牌前缀", "需要发动机号后N位", "需要车架号后N位"};
        String[] fields = new String[]{"provinceName", "cityName", "cityCode", "lsPrefix", "engineNo", "vinNo"};
        String  filePath = "/Users/huangzhangting/Desktop/车辆违章查询接口/";
        PoiUtil poiUtil = new PoiUtil();
        try {
            poiUtil.exportXlsxWithMap("极速车辆违章接口支持的城市站", filePath, heads, fields, list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //测试违章查询接口
    @Test
    public void testWeiZhang(){
        String url = "http://api.jisuapi.com/illegal/query";
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("appkey", APP_KEY));
        nameValuePairList.add(new BasicNameValuePair("carorg", "hangzhou")); //管局名称 不填默认为车牌所在地
        nameValuePairList.add(new BasicNameValuePair("lsprefix", "浙"));
        nameValuePairList.add(new BasicNameValuePair("lsnum", "AU537D")); //车牌号
        nameValuePairList.add(new BasicNameValuePair("engineno", "090407B")); //发动机号后N位
        nameValuePairList.add(new BasicNameValuePair("frameno", "LGBM2DE47AS006276")); //车架号后N位
//        nameValuePairList.add(new BasicNameValuePair("iscity", "1")); //是否返回城市 1返回 默认0不返回 不一定100%返回结果，准确度90% town、lat、lng仅供参考

        HttpClientResult clientResult = HttpClientUtil.get(url, nameValuePairList);
        if(clientResult==null){
            return;
        }
        System.out.println(clientResult.getData());

    }

    public Map<String, String> getDataAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("城市", "cityName");
        attrMap.put("车牌号", "licencePlate");
        attrMap.put("发动机号", "engineNo");
        attrMap.put("车架号", "vinNo");
        attrMap.put("接口返回", "result");

        return attrMap;
    }

    @Test
    public void test() throws Exception{
        String  filePath = "/Users/huangzhangting/Desktop/车辆违章查询接口/";

        Map<String, String> dataAttrMap = getDataAttrMap();

        CommReaderXLSX readerXLSX = new CommReaderXLSX(dataAttrMap);
        readerXLSX.processFirstSheet(filePath + "接口测试/极速违章接口测试结果-20161116.xlsx");
        List<Map<String, String>> testList = readerXLSX.getDataList();
        Print.printList(testList);

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("省份", "provinceName");
        attrMap.put("城市", "cityName");
        attrMap.put("城市编码", "cityCode");
        attrMap.put("需要发动机号后N位", "engineNo");
        attrMap.put("需要车架号后N位", "vinNo");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "极速车辆违章接口支持的城市站-20161116.xlsx");
        List<Map<String, String>> supportCityList = readerXLSX.getDataList();
        Print.printList(supportCityList);

        Set<String> set = new HashSet<>();
        for(Map<String, String> city : supportCityList){
            String cityName = city.get("cityName");
            if(!set.add(cityName)){
                Print.info("重复的城市站："+city);
            }
        }

        for(Map<String, String> data : testList){
            String result = data.get("result");
            if(!StringUtils.isEmpty(result)){
                continue;
            }
            List<NameValuePair> nameValuePairList = getNameValuePairList(data, supportCityList);
            if(nameValuePairList==null){
                Print.info("暂时不支持该城市："+data);
                data.put("result", "暂时不支持该城市");
                continue;
            }
            Print.info(nameValuePairList);

            data.put("result", getWzData(nameValuePairList));
        }

        String[] heads = new String[]{"城市", "车牌号", "发动机号", "车架号", "接口返回"};
        int len = heads.length;
        String[] fields = new String[len];
        for(int i=0; i<len; i++){
            fields[i] = dataAttrMap.get(heads[i]);
        }
        PoiUtil poiUtil = new PoiUtil();
        String excelPath = filePath + "接口测试/";
        poiUtil.exportXlsxWithMap("极速违章接口测试结果", excelPath, heads, fields, testList);

    }
    private List<NameValuePair> getNameValuePairList(Map<String, String> data, List<Map<String, String>> supportCityList){
        String cityName = data.get("cityName");
        Map<String, String> supportCity = getSupportCity(cityName, supportCityList);
        if(supportCity==null){
            return null;
        }
        String licencePlate = data.get("licencePlate");
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("appkey", APP_KEY));
        nameValuePairList.add(new BasicNameValuePair("carorg", supportCity.get("cityCode"))); //管局名称 不填默认为车牌所在地
        nameValuePairList.add(new BasicNameValuePair("lsprefix", licencePlate.substring(0, 1)));
        nameValuePairList.add(new BasicNameValuePair("lsnum", licencePlate.substring(1)));
        nameValuePairList.add(new BasicNameValuePair("engineno", data.get("engineNo")));
        nameValuePairList.add(new BasicNameValuePair("frameno", data.get("vinNo")));

        return nameValuePairList;
    }
    private Map<String, String> getSupportCity(String cityName, List<Map<String, String>> supportCityList){
        if(StringUtils.isEmpty(cityName)){
            return null;
        }
        for(Map<String, String> city : supportCityList){
            if(cityName.equals(city.get("cityName"))){
                return city;
            }
        }
        return null;
    }

    private String getWzData(List<NameValuePair> nameValuePairList){
        String url = "http://api.jisuapi.com/illegal/query";
        HttpClientResult clientResult = HttpClientUtil.get(url, nameValuePairList);
        if(clientResult==null){
            return "";
        }
        System.out.println(clientResult.getData());
        return clientResult.getData();
    }


    //违章接口返回结果解析
    @Test
    public void test_result() throws Exception{
        path = "/Users/huangzhangting/Desktop/车辆违章查询接口/接口测试/";
        String excel = path + "极速违章接口测试结果（需核对是否正确）.xlsx";
        Map<String, String> dataAttrMap = getDataAttrMap();
        CommReaderXLSX readerXLSX = new CommReaderXLSX(dataAttrMap);
        readerXLSX.processFirstSheet(excel);
        List<Map<String, String>> dataList = readerXLSX.getDataList();
        Print.printList(dataList);

        for(Map<String, String> data : dataList){
            String result = data.get("result");
            JsonNode resultJson = JsonUtil.getJsonNode(result);
            if(resultJson != null){
                String resultStr = getResultStr(resultJson);
                Print.info(resultStr);
                data.put("result", resultStr);
            }
        }

        String[] heads = new String[]{"城市", "车牌号", "发动机号", "车架号", "接口返回"};
        int len = heads.length;
        String[] fields = new String[len];
        for(int i=0; i<len; i++){
            fields[i] = dataAttrMap.get(heads[i]);
        }
        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("极速违章接口测试结果（需核对是否正确）", path, heads, fields, dataList);

    }

    public String getResultStr(JsonNode jsonNode){
        if(jsonNode.findValue("status").asInt() != 0){
            return jsonNode.findValue("msg").asText();
        }

        WzInfo wzInfo = getWzInfo(jsonNode.findValue("result"));
        if(CollectionUtils.isEmpty(wzInfo.getWzDetailList())){
            return "无违章";
        }
        return wzInfo.getWzDetailList().toString();
    }

    private WzInfo getWzInfo(JsonNode jsonNode){
        WzInfo wzInfo = new WzInfo();
        wzInfo.setCarOrg(JsonUtil.jsonNodeToStr(jsonNode, "carorg"));
        wzInfo.setLsPrefix(JsonUtil.jsonNodeToStr(jsonNode, "lsprefix"));
        wzInfo.setLsNum(JsonUtil.jsonNodeToStr(jsonNode, "lsnum"));
        wzInfo.setUserCarId(JsonUtil.jsonNodeToInt(jsonNode, "usercarid"));
        wzInfo.setWzDetailList(getWzDetailList(jsonNode.findValue("list")));
        return wzInfo;
    }
    private List<WzDetail> getWzDetailList(JsonNode jsonNode){
        List<WzDetail> wzDetailList = new ArrayList<>();
        Iterator<JsonNode> jsonNodeIterator = jsonNode.elements();
        while (jsonNodeIterator.hasNext()){
            WzDetail wzDetail = getWzDetail(jsonNodeIterator.next());
            wzDetailList.add(wzDetail);
        }
        return wzDetailList;
    }
    private WzDetail getWzDetail(JsonNode jsonNode){
        WzDetail wzDetail = new WzDetail();
        wzDetail.setTime(JsonUtil.jsonNodeToStr(jsonNode, "time"));
        wzDetail.setAddress(JsonUtil.jsonNodeToStr(jsonNode, "address"));
        wzDetail.setContent(JsonUtil.jsonNodeToStr(jsonNode, "content"));
        wzDetail.setIllegalNo(JsonUtil.jsonNodeToStr(jsonNode, "legalnum"));
        wzDetail.setPrice(JsonUtil.jsonNodeToStr(jsonNode, "price"));
        wzDetail.setScore(JsonUtil.jsonNodeToStr(jsonNode, "score"));
        wzDetail.setIllegalId(JsonUtil.jsonNodeToInt(jsonNode, "illegalid"));
        wzDetail.setNumber(JsonUtil.jsonNodeToStr(jsonNode, "number"));
        wzDetail.setAgency(JsonUtil.jsonNodeToStr(jsonNode, "agency"));
        return wzDetail;
    }

    @Data
    private class WzInfo{
        private String carOrg; //管局名称
        private String lsPrefix; //车牌前缀
        private String lsNum; //车牌剩余部分
        private Integer userCarId; //车牌ID
        private List<WzDetail> wzDetailList;

        @Override
        public String toString() {
            return "{" +
                    "管局名称=" + carOrg +
                    ", 违章详情=" + wzDetailList +
                    "}";
        }
    }
    @Data
    private class WzDetail{
        private String time; //时间
        private String address; //地点
        private String content; //违章内容
        private String illegalNo; //违章代码
        private String price; //罚款金额
        private String score; //扣分
        private Integer illegalId; //违章ID
        private String number; //违章编号
        private String agency; //采集机关

        @Override
        public String toString() {
            return "{" +
                    "时间=" + time +
                    ", " + address +
                    ", " + content +
                    ", 违章代码=" + illegalNo +
                    ", 罚款金额=" + price +
                    ", 扣分=" + score +
                    "}";
        }
    }

}
