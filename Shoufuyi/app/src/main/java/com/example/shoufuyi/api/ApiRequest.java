package com.example.shoufuyi.api;

import android.content.Context;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

public class ApiRequest {
    /**
     * 基本参数变量
     *
     * @return RequestParams
     */
    public static RequestParams getBaseParams() {
        RequestParams params = new RequestParams();
        return params;
    }

    /**
     * 登陆后参数变量
     *
     * @return RequestParams
     */
    public static RequestParams getBaseLoginParams() {
        RequestParams params = getBaseParams();
        return params;
    }

    /**
     * 登录
     *
     * @param username      用户名
     * @param password      密码
     * @param handler       回调
     */
    public static RequestHandle login(Context context,
                                      String username,
                                      String password,
                                      JsonHttpHandler handler) {
        RequestParams params = getBaseParams();
        params.put("mobile", username);
        params.put("password", password);
        String url = ApiUrl.getAbsoluteUrl(ApiUrl.LOGIN);
        return AsyncHttp.post(context,url, params, handler);
    }



}