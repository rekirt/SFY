package com.cchtw.sfy.uitls;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cchtw.sfy.BaseApplication;
import com.cchtw.sfy.activity.LoginActivity;
import com.google.gson.Gson;
import com.itech.message.APP_120033;


/**
 * Description:用户帮助类
 * Created by Fu.H.L on
 * Date:2015-05-22
 * Time:上午9:24
 * Copyright © 2015年 Fu.H.L All rights reserved.
 */
public class AccountHelper {

    public static APP_120033 getUser() {
        String s = SharedPreferencesHelper.getInstance(BaseApplication.getInstance()).getString(Constant.USER,"");
        if (TextUtils.isEmpty(s)) {
            return null;
        } else {
            return new Gson().fromJson(s, APP_120033.class);
        }
    }


    public static void setUser(APP_120033 user) {
        if (user == null) {
            SharedPreferencesHelper.setString(Constant.USER, null);
            return;
        } else {
            SharedPreferencesHelper.setString(Constant.USER, new Gson().toJson(user));
        }
    }

    public static String getUserName() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getUserName();
        }else {
            return "";
        }
    }

    public static String getMerchantId() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getMerchantId();
        }else {
            return "";
        }
    }

    public static String getDes3Key() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getDes3Key();
        }else {
            return "";
        }
    }

    public static String getDesKey() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getDesKey();
        }else {
            return "";
        }
    }

    public static String getToken() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getToken();
        }else {
            return "";
        }
    }

    public static String getReqSn() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getReqSn();
        }else {
            return "";
        }
    }
    public static String setReqSn(String sn) {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getReqSn();
        }else {
            return "";
        }
    }
    public static String getLoginState() {
        APP_120033 user = AccountHelper.getUser();
        if (user != null){
            return user.getLoginState();
        }else {
            return "";
        }
    }


    public static void setUserFingerPwd(String pwd) {
            SharedPreferencesHelper.setString(Constant.FINGERPASSWORD, pwd);
    }

    public static String getUserFingerPwd() {
        return SharedPreferencesHelper.getString(Constant.FINGERPASSWORD, "");
    }

    public static String getUserFingerPwdTimes() {
        return SharedPreferencesHelper.getString(Constant.FINGERPASSWORDTIMES, "5");
    }
    public static void setUserFingerPwdTimes(int i) {
         SharedPreferencesHelper.setString(Constant.FINGERPASSWORDTIMES, i+"");
    }

    public static boolean haveSetFingerPwd() {
        return SharedPreferencesHelper.getBoolean(Constant.HAVESETFINGERPWD, false);
    }

    public static void haveSetFingerPwd(boolean b) {
         SharedPreferencesHelper.setBoolean(Constant.HAVESETFINGERPWD, b);
    }

    public static boolean isLogin() {
        APP_120033 user = getUser();
        if (user == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void logout() {
        String mPhoneNumber = SharedPreferencesHelper.getString(Constant.PHONE, "");// 保存字符串
        SharedPreferencesHelper.setString(mPhoneNumber+Constant.DESKEY,"");
        SharedPreferencesHelper.setString(mPhoneNumber + Constant.TOKEN, "");
        AccountHelper.setUser(null);
    }

    public static void logoutAndGotoLogin(Context mContext) {
        logout();
        Intent intent_login = new Intent();
        intent_login.setClass(mContext,LoginActivity.class);
        intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
        mContext.startActivity(intent_login);
        ActivityCollector.finishAll();
    }
}
