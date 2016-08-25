package com.cchtw.sfy.api;

import android.content.Context;

import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.TLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.io.ByteArrayInputStream;

import cz.msebera.android.httpclient.entity.InputStreamEntity;


public class AsyncHttp {
    public static AsyncHttpClient client;

    public static AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
        }
        int mTimeout = Integer.parseInt(SharedPreferencesHelper.getString(Constant.TIMEOUT, "30"))*1000;
//        client.setConnectTimeout(5000);
        client.setTimeout(5000);
//        client.setMaxRetriesAndTimeout(0,5000);
//        client.setResponseTimeout(mTimeout);
        return client;
    }

    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        TLog.analytics("get----url----"+url+";params----"+params);
        return getClient().get(url, params, responseHandler);
    }

    public static RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        TLog.analytics("post----url----"+url+";params----"+params);
        return getClient().post(url, params, responseHandler);
    }

    public static RequestHandle get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        TLog.analytics("get----url----"+url+";params----"+params);
        return getClient().get(context, url, params, responseHandler);
    }

    public static RequestHandle post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        TLog.analytics("post----url----"+url+";params----"+params);
        return getClient().post(context, url, params, responseHandler);
    }

    public static RequestHandle get(Context context,
                             byte[] zipSendDateService,
                             String contentType,
                             ResponseHandlerInterface responseHandler){
            if (zipSendDateService !=null){
                //字节转成字节输入流
                ByteArrayInputStream bis = new ByteArrayInputStream(zipSendDateService);
                //将流写到Entity中
                InputStreamEntity entity = new InputStreamEntity(bis,
                        zipSendDateService.length);
                //请求服务器
                return getClient().get(context, ApiUrl.ONLINE_URL, entity, contentType,
                        responseHandler);
            }
          return null;
    }

    //获得上面方法的参数
    public static RequestHandle post(Context context,
                              byte[] zipSendDateService,
                              String contentType,
                              ResponseHandlerInterface responseHandler) {
        if (zipSendDateService !=null){
            //字节转成字节输入流
            ByteArrayInputStream bis = new ByteArrayInputStream(zipSendDateService);
            //将流写到Entity中
            InputStreamEntity entity = new InputStreamEntity(bis, zipSendDateService.length);
            //请求服务器
            return getClient().post(context, ApiUrl.ONLINE_URL, entity, contentType,
                    responseHandler);
        }
        return null;
    }


}
