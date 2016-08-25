package com.cchtw.sfy.api;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.BaseApplication;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.itech.message.APPMsgPack;
import com.itech.message.APP_Basic;
import com.itech.utils.SequenceUtil;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

public class ApiRequest {

    public static APP_Basic returnapp;

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
     */
    public static RequestHandle login(APP_Basic app,String MOBILE,
                                      JsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = pack(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream",handler);
    }

    /**
     * 获取验证码
     *
     */
    public static RequestHandle getMsgCode(APP_Basic app,String MOBILE,
                                      JsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);

        // 要发送的字节数组
        byte[] data = pack(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream", handler);
    }


    /**
     * 提交请求
     *
     */
    public static RequestHandle requestData(APP_Basic app,String MOBILE,
                                           JsonHttpHandler handler) {
        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = pack(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream",handler);
    }
    /**
     * 提交请求
     *
     */
    public static RequestHandle syncHttpRequestData(APP_Basic app,String MOBILE,
                                            JsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = pack(json, MOBILE);
        return SyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream", handler);
    }

    // 压缩
    private static byte[] pack(String json, String MOBILE) {
        Boolean isActivation = false;
        String token ="";
        String deskey = "";
        String mPhoneNumber = SharedPreferencesHelper.getString(Constant.PHONE, "");// 保存字符串

        deskey = SharedPreferencesHelper.getString(mPhoneNumber + Constant.DESKEY, "");
        token = SharedPreferencesHelper.getString(mPhoneNumber + Constant.TOKEN, "");

        isActivation = SharedPreferencesHelper.getBoolean(MOBILE + Constant.ACTIVATION, false);
        String uuid = SharedPreferencesHelper.getString("uuid", "");

        if (TextUtils.isEmpty(token)) {
            token = SequenceUtil.TOKEN;
            returnapp.setTrxCode("120032");
        }
        if (!isActivation) {
            returnapp.getTrxCode().equals("120031");
            returnapp.setTrxCode("120032");
        }
        APPMsgPack pack;
        if (TextUtils.isEmpty(deskey)) {
            pack = new APPMsgPack(json.getBytes(), MOBILE, Constant.secretType,
                    returnapp.getTrxCode(), token);
        } else {
            pack = new APPMsgPack(json.getBytes(), MOBILE, Constant.secretType,
                    returnapp.getTrxCode(), token, deskey);
        }
        try {
            pack.setTerminalInfo(uuid);
            return pack.pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}