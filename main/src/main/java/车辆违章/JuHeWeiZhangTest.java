package 车辆违章;

import com.fasterxml.jackson.databind.JsonNode;
import dp.common.util.JsonUtil;
import dp.common.util.LocalConfig;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.http.HttpClientResult;
import dp.common.util.http.HttpClientUtil;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/11.
 */
public class JuHeWeiZhangTest {
    private static final String APP_KEY = LocalConfig.JU_HE_APP_KEY;

    //获取支持城市参数接口
    @Test
    public void testCity(){
        String url = "http://v.juhe.cn/wz/citys";
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("key", APP_KEY));
        HttpClientResult httpClientResult = HttpClientUtil.get(url, nameValuePairList);
        if(httpClientResult==null){
            return;
        }
        System.out.println(httpClientResult.getData());
        JsonNode result = JsonUtil.getJsonNode(httpClientResult.getData());
        if(result!=null){
            int errorCode = result.findValue("error_code").asInt();
            if(errorCode==0){
                result = result.findValue("result");
                Iterator<JsonNode> jsonNodeElements = result.elements();
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
        province.setProvinceCode(jsonNode.findValue("province_code").asText());
        province.setCityList(getWzCityList(jsonNode.findValue("citys")));
        return province;
    }
    private List<WzCity> getWzCityList(JsonNode jsonNode){
        //System.out.println(jsonNode.toString());

        List<WzCity> list = new ArrayList<>();
        Iterator<JsonNode> jsonNodeElements = jsonNode.elements();
        while (jsonNodeElements.hasNext()){
            WzCity city = getWzCity(jsonNodeElements.next());
            list.add(city);
        }
        return list;
    }
    private WzCity getWzCity(JsonNode jsonNode){
        WzCity city = new WzCity();
        city.setCityName(jsonNode.findValue("city_name").asText());
        city.setCityCode(jsonNode.findValue("city_code").asText());
        city.setEngineFlag(jsonNode.findValue("engine").asInt());
        city.setEngineNo(jsonNode.findValue("engineno").asInt());
        city.setVinFlag(jsonNode.findValue("class").asInt());
        city.setVinNo(jsonNode.findValue("classno").asInt());

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
        private Integer engineFlag; //是否需要发动机号0,不需要 1,需要
        private Integer engineNo; //需要几位发动机号0,全部 1-9 ,需要发动机号后N位
        private Integer vinFlag; //是否需要车架号0,不需要 1,需要
        private Integer vinNo; //需要几位车架号0,全部 1-9 需要车架号后N位

    }

    public void export(List<WzProvince> provinceList){
        List<Map<String, String>> list = new ArrayList<>();
        for(WzProvince province : provinceList){
            for(WzCity city : province.getCityList()){
                Map<String, String> map = new HashMap<>();
                map.put("provinceName", province.getProvinceName());
                map.put("provinceCode", province.getProvinceCode());
                map.put("cityName", city.getCityName());
                map.put("cityCode", city.getCityCode());
                map.put("engineFlag", city.getEngineFlag()==1?"是":"");
                map.put("engineNo", city.getEngineNo()+"");
                map.put("vinFlag", city.getVinFlag()==1?"是":"");
                map.put("vinNo", city.getVinNo()+"");
                list.add(map);
            }
        }

        String[] heads = new String[]{"省份", "省份编码", "城市", "城市编码", "是否需要发动机号", "需要发动机号后N位", "是否需要车架号", "需要车架号后N位"};
        String[] fields = new String[]{"provinceName", "provinceCode", "cityName", "cityCode", "engineFlag", "engineNo", "vinFlag", "vinNo"};
        String  filePath = "/Users/huangzhangting/Desktop/车辆违章查询接口/";
        PoiUtil poiUtil = new PoiUtil();
        try {
            poiUtil.exportXlsxWithMap("聚合车辆违章接口支持的城市站", filePath, heads, fields, list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //测试违章查询接口
    @Test
    public void testWeiZhang(){
        String url = "http://v.juhe.cn/wz/query";
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("key", APP_KEY));
        nameValuePairList.add(new BasicNameValuePair("city", "SH"));
        nameValuePairList.add(new BasicNameValuePair("hphm", "沪LZ2022"));
        nameValuePairList.add(new BasicNameValuePair("engineno", "A15070865"));
//        nameValuePairList.add(new BasicNameValuePair("classno", "")); //

        HttpClientResult clientResult = HttpClientUtil.get(url, nameValuePairList);
        if(clientResult==null){
            return;
        }
        System.out.println(clientResult.getData());

    }

}
