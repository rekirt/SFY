package com.cchtw.sfy.api;

import android.content.Context;

import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import java.io.ByteArrayInputStream;

import cz.msebera.android.httpclient.entity.InputStreamEntity;


public class SyncHttp {
    public static SyncHttpClient client;

    public static SyncHttpClient getClient() {
        if (client == null) {
            client = new SyncHttpClient();
        }
        return client;
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
