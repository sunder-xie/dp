package dp.common.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by huangzhangting on 16/12/5.
 */
@Slf4j
public class HttpClientUtil {

    private static final int CONNECT_TIME_OUT = 60000;
    private static final int READ_TIME_OUT = 60000;

    private static final String DEFAULT_CHARSET = "UTF-8";

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

    private static HttpClientResult send(HttpRequestBase request, String url){
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(request);
            HttpClientResult hcResult = new HttpClientResult();
            hcResult.setStatus(response.getStatusLine().getStatusCode());

            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                hcResult.setData(EntityUtils.toString(resEntity, DEFAULT_CHARSET));
            }

            log.info("http execute success, url:{}, status:{}", url, hcResult.getStatus());
            return hcResult;

        } catch (Exception e) {
            log.error("http execute error, url:" + url, e);
        } finally {
            if(response!=null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("CloseableHttpResponse close error", e);
                }
            }
            if(request!=null) {
                request.releaseConnection();
            }
        }

        return null;
    }

    public static HttpClientResult post(String url) {
        return post(url, null);
    }

    public static HttpClientResult post(String url, List<NameValuePair> nvpList) {
        HttpPost request = new HttpPost(url);
        if (!CollectionUtils.isEmpty(nvpList)) {
            try {
                request.setEntity(new UrlEncodedFormEntity(nvpList, DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                log.error("http post set entity error, url:"+url, e);
                return null;
            } finally {
                request.releaseConnection();
            }
        }
        return send(request, url);
    }

    public static HttpClientResult postJson(String url, String jsonString) {
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/json;charset=UTF-8");
        request.addHeader("Accept", "application/json");

        if (!StringUtils.isEmpty(jsonString)) {
            try {
                request.setEntity(new StringEntity(jsonString));
            } catch (UnsupportedEncodingException e) {
                log.error("http postJson set entity error, url:"+url, e);
                return null;
            } finally {
                request.releaseConnection();
            }
        }
        return send(request, url);
    }


    public static HttpClientResult get(String url) {
        return get(url, null);
    }

    public static HttpClientResult get(String url, List<NameValuePair> nvpList) {
        HttpGet request = new HttpGet(url);
        if (!CollectionUtils.isEmpty(nvpList)) {
            String params = (URLEncodedUtils.format(nvpList, DEFAULT_CHARSET));
            url = url + "?" + params;
        }
        return send(request, url);
    }

    public static HttpClientResult postXml(String url, String xml) {
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/soap+xml;charset=UTF-8");

        if (!StringUtils.isEmpty(xml)) {
            try {
                request.setEntity(new StringEntity(xml));
            } catch (UnsupportedEncodingException e) {
                log.error("http postXml error, url:"+url, e);
                return null;
            }finally {
                request.releaseConnection();
            }
        }
        return send(request, url);
    }

}
