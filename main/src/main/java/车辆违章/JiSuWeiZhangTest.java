package 车辆违章;

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
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/11.
 */
public class JiSuWeiZhangTest {
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
                list.add(map);
            }
        }

        String[] heads = new String[]{"省份", "城市", "城市编码", "需要发动机号后N位", "需要车架号后N位"};
        String[] fields = new String[]{"provinceName", "cityName", "cityCode", "engineNo", "vinNo"};
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
        nameValuePairList.add(new BasicNameValuePair("lsnum", "AW9F76"));
        nameValuePairList.add(new BasicNameValuePair("engineno", "0"));
        nameValuePairList.add(new BasicNameValuePair("frameno", "471887"));
//        nameValuePairList.add(new BasicNameValuePair("iscity", "1")); //是否返回城市 1返回 默认0不返回 不一定100%返回结果，准确度90% town、lat、lng仅供参考

        HttpClientResult clientResult = HttpClientUtil.get(url, nameValuePairList);
        if(clientResult==null){
            return;
        }
        System.out.println(clientResult.getData());

    }

    @Test
    public void test() throws Exception{
        String  filePath = "/Users/huangzhangting/Desktop/车辆违章查询接口/";

        Map<String, String> dataAttrMap = new HashMap<>();
        dataAttrMap.put("城市", "cityName");
        dataAttrMap.put("车牌号", "licencePlate");
        dataAttrMap.put("发动机号", "engineNo");
        dataAttrMap.put("车架号", "vinNo");
        dataAttrMap.put("备注", "remark");
        dataAttrMap.put("接口返回", "result");

        CommReaderXLSX readerXLSX = new CommReaderXLSX(dataAttrMap);
        readerXLSX.processFirstSheet(filePath + "车辆违章查询测试需求-反馈-1.xlsx");
        List<Map<String, String>> testList = readerXLSX.getDataList();
        Print.printList(testList);

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("省份", "provinceName");
        attrMap.put("城市", "cityName");
        attrMap.put("城市编码", "cityCode");
        attrMap.put("需要发动机号后N位", "engineNo");
        attrMap.put("需要车架号后N位", "vinNo");

        readerXLSX = new CommReaderXLSX(attrMap);
        readerXLSX.processFirstSheet(filePath + "极速车辆违章接口支持的城市站-20161114.xlsx");
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
            String remark = data.get("remark");
            if(!StringUtils.isEmpty(remark)){
                continue;
            }
            List<NameValuePair> nameValuePairList = getNameValuePairList(data, supportCityList);
            if(nameValuePairList==null){
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
        poiUtil.exportXlsxWithMap("极速违章接口测试结果", filePath, heads, fields, testList);

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

}
