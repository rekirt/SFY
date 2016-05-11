package com.example.shoufuyi.api;

import android.content.Context;

import com.example.shoufuyi.uitls.TLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;


public class AsyncHttp {
    public static AsyncHttpClient client;

    public static AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
        }
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
}
