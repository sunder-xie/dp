package 车辆违章;

import com.fasterxml.jackson.databind.JsonNode;
import dp.common.util.JsonUtil;
import dp.common.util.LocalConfig;
import dp.common.util.Print;
import dp.common.util.excelutil.PoiUtil;
import dp.common.util.http.HttpClientResult;
import dp.common.util.http.HttpClientUtil;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/11/15.
 */
public class HaoServiceWeiZhangTest {
    private static final String APP_KEY = LocalConfig.HAO_SERVICE_APP_KEY;

    //违章支持城市站接口测试
    @Test
    public void test_city(){
        String url = "http://apis.haoservice.com/weizhang/citys?key="+APP_KEY;

        HttpClientResult clientResult = HttpClientUtil.get(url);
        if(clientResult==null){
            return;
        }
        Print.info(clientResult.getData());

        JsonNode result = JsonUtil.getJsonNode(clientResult.getData());
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
                Print.info(provinceList.size());
                Print.info(provinceList.get(0));

                //导出城市站信息
                export(provinceList);
            }
        }
    }

    private WzProvince getWzProvince(JsonNode jsonNode){
        WzProvince province = new WzProvince();
        province.setProvinceName(jsonNode.findValue("province").asText());
        province.setProvinceCode(jsonNode.findValue("province_code").asText());
        province.setCityList(getWzCityList(jsonNode.findValue("citys")));
        return province;
    }
    private List<WzCity> getWzCityList(JsonNode jsonNode){
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
        city.setAbbr(jsonNode.findValue("abbr").asText());
        city.setCityName(jsonNode.findValue("city_name").asText());
        city.setCityCode(jsonNode.findValue("city_code").asText());
        city.setEngineFlag(jsonNode.findValue("engine").asInt());
        city.setEngineNo(jsonNode.findValue("engineno").asInt());
        city.setVinFlag(jsonNode.findValue("classa").asInt());
        city.setVinNo(jsonNode.findValue("classno").asInt());
        city.setRegisterFlag(jsonNode.findValue("regist").asInt());
        city.setRegisterNo(jsonNode.findValue("registno").asInt());

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
        private String abbr; //省份简称
        private String cityName;
        private String cityCode;
        private Integer engineFlag; //是否需要发动机号0,不需要 1,需要
        private Integer engineNo; //需要几位发动机号0,全部 1-9 ,需要发动机号后N位
        private Integer vinFlag; //是否需要车架号0,不需要 1,需要
        private Integer vinNo; //需要几位车架号0,全部 1-9 需要车架号后N位
        private Integer registerFlag; //是否需要登记证书号0,不需要 1,需要
        private Integer registerNo; //需要几位登记证书0,全部 1-9 需要登记证书后N位
    }

    //导出城市站信息
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
                map.put("registerFlag", city.getRegisterFlag()==1?"是":"");
                map.put("registerNo", city.getRegisterNo()+"");
                list.add(map);
            }
        }

        String[] heads = new String[]{"省份", "省份编码", "城市", "城市编码", "是否需要发动机号", "需要发动机号后N位",
                "是否需要车架号", "需要车架号后N位", "是否需要登记证书号", "需要登记证书后N位"};

        String[] fields = new String[]{"provinceName", "provinceCode", "cityName", "cityCode", "engineFlag", "engineNo",
                "vinFlag", "vinNo", "registerFlag", "registerNo"};

        String  filePath = "/Users/huangzhangting/Desktop/车辆违章查询接口/";
        PoiUtil poiUtil = new PoiUtil();
        try {
            poiUtil.exportXlsxWithMap("HaoService车辆违章接口支持的城市站", filePath, heads, fields, list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //违章接口测试
    @Test
    public void test_weiZhang(){
        String url = "http://apis.haoservice.com/weizhang/query";
        List<NameValuePair> pairList = new ArrayList<>();
        pairList.add(new BasicNameValuePair("key", APP_KEY));

        pairList.add(new BasicNameValuePair("city", "S3X_BaoJi")); //城市代码
        pairList.add(new BasicNameValuePair("hphm", "陕CYZ915")); //号牌号码完整7位

        //
        pairList.add(new BasicNameValuePair("engineno", "")); //发动机号 (根据城市接口中的参数填写)

        //
        pairList.add(new BasicNameValuePair("classno", "4945")); //车架号 (根据城市接口中的参数填写)

        pairList.add(new BasicNameValuePair("registno", "")); //车辆登记证书号 (根据城市接口中的参数填写)
        pairList.add(new BasicNameValuePair("hpzl", "02")); //小型车

        HttpClientResult clientResult = HttpClientUtil.get(url, pairList);
        if(clientResult==null){
            return;
        }
        Print.info(clientResult.getData());

    }

}
