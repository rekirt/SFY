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
    public static AsyncHttpClient clientLogin;
    public static AsyncHttpClient client;

    public static AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
        }
        int mTimeout = Integer.parseInt(SharedPreferencesHelper.getString(Constant.TIMEOUT, "30"));
        client.setMaxRetriesAndTimeout(1,1000*mTimeout);
        client.setTimeout(1000*mTimeout);
        client.setConnectTimeout(1000*mTimeout);
        client.setResponseTimeout(1000*mTimeout);
        TLog.analytics("getClient----mTimeout"+mTimeout+"getConnectTimeout()"+client.getConnectTimeout()+"getMaxConnections"+client.getMaxConnections()+"getResponseTimeout"+client.getResponseTimeout());
        return client;
    }

    public static AsyncHttpClient getLoginClient() {
        if (clientLogin == null) {
            clientLogin = new AsyncHttpClient();
        }
        clientLogin.setConnectTimeout(1000*185);
        clientLogin.setMaxRetriesAndTimeout(5,1000*185);
        clientLogin.setTimeout(1000*185);
        clientLogin.setResponseTimeout(1000*185);

        TLog.analytics("getLoginClient----getConnectTimeout()"+clientLogin.getConnectTimeout()+"getMaxConnections"+clientLogin.getMaxConnections()+"getResponseTimeout"+clientLogin.getResponseTimeout());
        return clientLogin;
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

    //获得上面方法的参数
    public static RequestHandle postLogin(Context context,
                                     byte[] zipSendDateService,
                                     String contentType,
                                     ResponseHandlerInterface responseHandler) {
        if (zipSendDateService !=null){
            //字节转成字节输入流
            ByteArrayInputStream bis = new ByteArrayInputStream(zipSendDateService);
            //将流写到Entity中
            InputStreamEntity entity = new InputStreamEntity(bis, zipSendDateService.length);
            //请求服务器
            return getLoginClient().post(context, ApiUrl.ONLINE_URL, entity, contentType,
                    responseHandler);
        }
        return null;
    }
}
