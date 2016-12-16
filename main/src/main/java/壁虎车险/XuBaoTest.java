package 壁虎车险;

import dp.common.util.JsonUtil;
import dp.common.util.MD5Utils;
import dp.common.util.Print;
import dp.common.util.http.HttpClientResult;
import dp.common.util.http.HttpClientUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhangting on 16/12/5.
 */
public class XuBaoTest {

    @Test
    public void test(){
        String secCodeKey = "400effe49a4";

        String Agent = "66808";
        String LicenseNo = "浙A88888";
        int CityCode = 9;
        int Group = 1;
        String CustKey = "tqmall2017888888";

        StringBuilder params = new StringBuilder();
        params.append("Agent=").append(Agent);
        params.append("&LicenseNo=").append(LicenseNo);
        params.append("&CityCode=").append(CityCode);
        params.append("&Group=").append(Group);
        params.append("&CustKey=").append(CustKey);

        String str = params.toString();
        Print.info(str);

        String SecCode = MD5Utils.MD5(str + secCodeKey);
        Print.info(SecCode);

        String url = "http://iu.91bihu.com/api/CarInsurance/getreinfo?"+str+"&SecCode="+SecCode;

        HttpClientResult clientResult = HttpClientUtil.get(url);
        System.out.println(JsonUtil.objectToStr(clientResult));

    }
}
