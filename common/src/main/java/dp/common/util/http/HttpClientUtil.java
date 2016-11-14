package dp.common.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by huangzhangting on 15/9/5.
 */
@Slf4j
public class HttpClientUtil {

    private static final int CONNECT_TIME_OUT = 6000;
    private static final int READ_TIME_OUT = 6000;

    private static final String defaultCharset = "UTF-8";

    private static CloseableHttpClient httpclient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(10);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIME_OUT)
                .setSocketTimeout(READ_TIME_OUT).build();

        httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(cm)
                .build();
    }

    public static HttpClientResult post(String url) {
        return post(url, null);
    }

    public static HttpClientResult post(String url, List<NameValuePair> nvpList) {
        try {
            HttpPost request = new HttpPost(url);
            if (!CollectionUtils.isEmpty(nvpList)) {
                request.setEntity(new UrlEncodedFormEntity(nvpList, defaultCharset));
            }

            CloseableHttpResponse response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            try {
                hcResult.setStatus(response.getStatusLine().getStatusCode());

                log.info("http post url:" + url + ", status:" + hcResult.getStatus());

                if (hcResult.getStatus() != HttpStatus.SC_OK) {
                    return hcResult;
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    hcResult.setData(EntityUtils.toString(resEntity, defaultCharset));
                }
                return hcResult;

            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("http post error, url:" + url, e);
        }

        return null;
    }

    public static HttpClientResult postJson(String url, String jsonString) {
        try {
            HttpPost request = new HttpPost(url);
            if (!StringUtils.isEmpty(jsonString)) {
                request.setEntity(new StringEntity(jsonString));
            }
            request.addHeader("Content-Type", "application/json;charset=UTF-8");
            request.addHeader("accept", "application/json");
            request.addHeader("connection", "Keep-Alive");

            CloseableHttpResponse response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            try {

                hcResult.setStatus(response.getStatusLine().getStatusCode());

                log.info("http post json url:" + url + ", params:" + jsonString + ", status:" + hcResult.getStatus());

                if (hcResult.getStatus() != HttpStatus.SC_OK) {
                    return hcResult;
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    hcResult.setData(EntityUtils.toString(resEntity, defaultCharset));
                }
                return hcResult;

            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("http post json error, url:" + url + ", params:" + jsonString, e);
        }

        return null;
    }


    public static HttpClientResult get(String url) {
        return get(url, null);
    }

    public static HttpClientResult get(String url, List<NameValuePair> nvpList) {
        try {
            if (!CollectionUtils.isEmpty(nvpList)) {
                String params = (URLEncodedUtils.format(nvpList, defaultCharset));
                url = url + "?" + params;
            }

            HttpGet request = new HttpGet(url);

            CloseableHttpResponse response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            try {
                hcResult.setStatus(response.getStatusLine().getStatusCode());

                log.info("http get url:" + url + ", status:" + hcResult.getStatus());

                if (hcResult.getStatus() != HttpStatus.SC_OK) {
                    return hcResult;
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    hcResult.setData(EntityUtils.toString(resEntity, defaultCharset));
                }
                return hcResult;

            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("http get error, url:" + url, e);
        }

        return null;
    }

    public static HttpClientResult put(String url, List<NameValuePair> nvpList) {
        try {
            HttpPut request = new HttpPut(url);
            if (!CollectionUtils.isEmpty(nvpList)) {
                request.setEntity(new UrlEncodedFormEntity(nvpList, defaultCharset));
            }

            CloseableHttpResponse response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            try {
                hcResult.setStatus(response.getStatusLine().getStatusCode());

                log.info("http put url:" + url + ", status:" + hcResult.getStatus());

                if (hcResult.getStatus() != HttpStatus.SC_OK) {
                    return hcResult;
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    hcResult.setData(EntityUtils.toString(resEntity, defaultCharset));
                }
                return hcResult;

            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("http put error, url:" + url, e);
        }

        return null;
    }

    public static HttpClientResult putJson(String url, String jsonString) {
        try {
            HttpPut request = new HttpPut(url);
            if (!StringUtils.isEmpty(jsonString)) {
                request.setEntity(new StringEntity(jsonString));
            }
            request.addHeader("Content-Type", "application/json;charset=UTF-8");
            request.addHeader("accept", "application/json");
            request.addHeader("connection", "Keep-Alive");

            CloseableHttpResponse response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            try {

                hcResult.setStatus(response.getStatusLine().getStatusCode());

                log.info("http put json url:" + url + ", params:" + jsonString + ", status:" + hcResult.getStatus());

                if (hcResult.getStatus() != HttpStatus.SC_OK) {
                    return hcResult;
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    hcResult.setData(EntityUtils.toString(resEntity, defaultCharset));
                }
                return hcResult;

            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("http put json error, url:" + url + ", params:" + jsonString, e);
        }

        return null;
    }

}
