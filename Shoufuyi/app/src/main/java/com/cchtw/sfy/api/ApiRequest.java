package com.cchtw.sfy.api;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.BaseApplication;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.TDevice;
import com.cchtw.sfy.uitls.TLog;
import com.itech.message.APPMsgPack;
import com.itech.message.APP_Basic;
import com.itech.utils.SequenceUtil;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import static com.cchtw.sfy.uitls.Constant.secretType;

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
                                      StartToUseJsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = packForStart(json, MOBILE);
        return AsyncHttp.postLogin(BaseApplication.getInstance(), data, "application/octet-stream",handler);
    }
    /**
     * 退出登录
     *
     */
    public static RequestHandle logout(APP_Basic app,String MOBILE,
                                      StartToUseJsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = packForStart(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream",handler);
    }

    /**
     * 获取验证码
     *
     */
    public static RequestHandle getMsgCode(APP_Basic app,String MOBILE,
                                           StartToUseJsonHttpHandler handler) {

        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);

        // 要发送的字节数组
        byte[] data = packForStart(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream", handler);
    }


    /**
     * 提交请求
     *
     */
    public static RequestHandle requestData(APP_Basic app,String MOBILE,
                                           JsonHttpHandler handler) {
        if (!TDevice.hasInternet()){
//            ToastHelper.ShowToast("网络连接失败,请检查您的网络是否异常!");
            handler.onFail("网络连接失败,请检查您的网络是否异常!");
            handler.onFinish();
            TLog.analytics("网络连接失败,请检查您的网络是否异常!");
            return null;
        }
        TLog.analytics("网络连接正常,requestData()");
        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        byte[] data = pack(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream",handler);
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
            token = SequenceUtil.TOKEN;
            deskey = "";
        }
        APPMsgPack pack;
        if (TextUtils.isEmpty(deskey)) {
            pack = new APPMsgPack(json.getBytes(), MOBILE, secretType,
                    returnapp.getTrxCode(), token);
        } else {
            pack = new APPMsgPack(json.getBytes(), MOBILE, secretType,
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


    /**
     * 提交请求
     *
     */
    public static RequestHandle requestStart(APP_Basic app,String MOBILE,
                                             StartToUseJsonHttpHandler handler) {
        String uuid = SharedPreferencesHelper.getString("uuid", "");
        if (uuid.equals("")) {
            uuid = "2" + SequenceUtil.globalSequenceFor32();
        }
        app.setTerminalInfo(uuid);
        returnapp = app;
        String json = JSON.toJSONString(app, true);
        // 要发送的字节数组
        byte[] data = packForStart(json, MOBILE);
        return AsyncHttp.post(BaseApplication.getInstance(), data, "application/octet-stream",handler);
    }

    // 压缩
    private static byte[] packForStart(String json, String MOBILE) {
        Boolean isActivation = false;
        String token ="";
        String deskey = "";
        String uuid = SharedPreferencesHelper.getString("uuid", "");
        returnapp.setTrxCode("120032");
        returnapp.getTrxCode().equals("120031");
        token = SequenceUtil.TOKEN;
        APPMsgPack pack;
        pack = new APPMsgPack(json.getBytes(), MOBILE, Constant.secretType,
                    returnapp.getTrxCode(), token);
        try {
            pack.setTerminalInfo(uuid);
            return pack.pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}