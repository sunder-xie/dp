package 保险核算;


import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 17/1/14.
 */
public class DataTest {
    private String path;


    private Map<String, String> getAttrMap(){
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("t2.insured_province", "省份");
        attrMap.put("t2.insured_city", "城市");
        attrMap.put("t2.agent_id", "门店id");
        attrMap.put("t2.agent_name", "门店名称");
        attrMap.put("t2.vehicle_sn", "车牌号");
        attrMap.put("t2.vehicle_owner_name", "车主");
        attrMap.put("t2.scenario_type", "投保场景");
        attrMap.put("t2.cooperation_mode", "购买方式");

        attrMap.put("t2.insurance_type", "保险类别");
        attrMap.put("t2.outer_insurance_form_no", "保单号");
        attrMap.put("t2.insured_fee", "保费");
        attrMap.put("t2.package_start_time", "保单生效时间");

        attrMap.put("t2.id", "basicId");
        attrMap.put("t2.gmt_create", "创建时间");
        attrMap.put("t2.insured_total_fee", "保费合计");

        attrMap.put("t2.pay_time", "支付时间");
        attrMap.put("t2.insurance_transfer_sn", "支付流水号");

        attrMap.put("t2.package_name", "服务包名称");
        attrMap.put("t2.market_price", "服务包金额");
        attrMap.put("t2.order_sn", "物料订单号");
        attrMap.put("t2.reward_amount", "服务包工时费");
        attrMap.put("ma.payable_amount", "门店机滤金额");

        return attrMap;
    }

    private String[] getExcelHeads(){
        List<String> list = new ArrayList<String>(){{
            add("省份");
            add("城市");
            add("门店id");
            add("门店名称");
            add("车牌号");
            add("车主");
            add("投保场景");
            add("购买方式");
            add("");
            add("");
            add("");
            add("");
            add("");
            add("");
            add("");
            add("");
        }};


        String[] heads = new String[]{};
        return heads;
    }

    @org.junit.Test
    public void test_excel() throws Exception{
        path = "/Users/huangzhangting/Desktop/保单核算-0114/";
        String excel = path + "买保险送奖励金+送服务包数据.xls";

        Map<String, String> attrMap = getAttrMap();
        CommReaderXLS readerXLS = new CommReaderXLS(attrMap);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.printList(dataList);

        Map<String, Map<String, String>> dataMap = new HashMap<>();
        for(Map<String, String> data : dataList){
            String basicId = data.get("basicId");
            Map<String, String> map = dataMap.get(basicId);
            if(map==null){
                convertData(data, null);
                dataMap.put(basicId, data);
            }else{
                convertData(data, map);
            }
        }



    }

    private void convertData(Map<String, String> data, Map<String, String> oldData){
        if(oldData != null){
            //保险类别:1表示交强险,2表示商业险
            String insurance_type = data.get("保险类别");
            String key = "";
            switch (insurance_type){
                case "1": key = "交强险"; break;
                case "2": key = "商业险"; break;
                default: break;
            }
            oldData.put(key+"保费", data.get("保费"));
            oldData.put(key+"保单号", data.get("保单号"));
            oldData.put(key + "保单生效时间", data.get("保单生效时间"));

            //支付时间
            if(StringUtils.isEmpty(oldData.get("第1次支付时间"))){
                oldData.put("第1次支付时间", data.get("支付时间"));
            }

            return;
        }

        //支付信息
        data.put("第1次支付时间", data.get("支付时间"));
        data.put("第1次支付流水号", data.get("支付流水号"));
        data.put("第1次支付金额", data.get("保费合计"));

        //购买保险场景 1:商业险 2:商业交强同保 3:交强险 4:第三方责任交强同保 5:责任险单保
        String scenario_type = data.get("投保场景");
        String scenario = scenario_type;
        switch (scenario_type){
            case "1": scenario="商业险"; break;
            case "2": scenario="商业交强同保"; break;
            case "3": scenario="交强险"; break;
            case "4": scenario="第三方责任交强同保"; break;
            case "5": scenario="责任险单保"; break;
            default: break;
        }
        data.put("投保场景", scenario);

        //和淘气合作模式 1:奖励金 2:买保险送服务 3:买服务送保险
        String cooperation_mode = data.get("购买方式");
        String cooperation = cooperation_mode;
        switch (cooperation_mode){
            case "1": cooperation="买保险送奖励金"; break;
            case "2": cooperation="买保险送服务"; break;
            case "3": cooperation="买服务送保险"; break;
            default: break;
        }
        data.put("购买方式", cooperation);

    }

}
