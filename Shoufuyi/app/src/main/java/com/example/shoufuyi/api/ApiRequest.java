package com.example.shoufuyi.api;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.BaseApplication;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
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


    // 压缩
    private static byte[] pack(String json, String MOBILE) {
        String token = SharedPreferencesHelper.getString("token", "");

        String deskey = SharedPreferencesHelper.getString("deskey", "");

        String jihuo = SharedPreferencesHelper.getString("jihuo", "");

        String uuid = SharedPreferencesHelper.getString("uuid", "");

        String strToken;
        if (!token.equals("")) {
            strToken = token;
        } else {
            strToken = SequenceUtil.TOKEN;
            SharedPreferencesHelper.getInstance().setString("token", strToken);
        }
        if (jihuo.equals("")) {
            returnapp.getTrxCode().equals("120031");
            returnapp.setTrxCode("120032");
        }

        APPMsgPack pack;
        if (deskey.equals("")) {
            pack = new APPMsgPack(json.getBytes(), MOBILE, Constant.secretType,
                    returnapp.getTrxCode(), strToken);
        } else {
            pack = new APPMsgPack(json.getBytes(), MOBILE, Constant.secretType,
                    returnapp.getTrxCode(), strToken, deskey);
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