package com.cchtw.sfy.uitls;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import com.cchtw.sfy.BaseApplication;


/**
 * MiGo
 * Description:使用方法：直接new一个SharedPreferencesHelper对象，然后调用对应的方法。
 * Created by FuHL on
 * Date:2016-01-14
 * Time:上午10:58
 * Copyright © 2016年 FuHL. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class SharedPreferencesHelper {

    private static SharedPreferences settings;
    public static SharedPreferences.Editor editor;
    private static SharedPreferencesHelper self = null;
    private static boolean sIsAtLeastGB;//Android的版本代码，版本号为2.3

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            sIsAtLeastGB = true;
        }
    }

    public SharedPreferencesHelper(Context context) {
        settings = context.getSharedPreferences(Constant.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    /**
     * 可以直接通过类获取到SharedPreferencesHelper对象，然后保存或者读取数据
     *
     * @return SharedPreferencesHelper对象
     */
    public static SharedPreferencesHelper getInstance() {
        if (self == null) {
            self = new SharedPreferencesHelper(BaseApplication.getInstance());
        }
        return self;
    }

    public static SharedPreferencesHelper getInstance(Context context) {
        if (self == null) {
            self = new SharedPreferencesHelper(context);
        }
        return self;
    }

    /**
     * 针对不同系统版本调用不同方法去保存SharedPreferences文件
     * @param editor SharedPreferences的编辑器
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void apply(SharedPreferences.Editor editor) {
        if (sIsAtLeastGB) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    /**
     *
     * @param paramString  配置名称
     * @param paramBoolean 配置值
     */
    public static void setBoolean(String paramString, boolean paramBoolean) {
        getInstance();
        editor.putBoolean(paramString, paramBoolean);
        apply(editor);
    }

    /**
     *
     * @param paramString  配置名称
     * @param defaultValue 返回的默认值
     * @return
     */
    public static boolean getBoolean(String paramString, boolean defaultValue) {
        getInstance();
        return settings.getBoolean(paramString, defaultValue);
    }

    /**
     *
     * @param paramString 配置名称
     * @param paramValue  配置值
     */
    public static void setString(String paramString, String paramValue) {
        getInstance();
        editor.putString(paramString, paramValue);
        apply(editor);
    }

    /**
     *
     * @param paramString   配置名称
     * @param defaultValue  返回的默认值
     * @return
     */
    public static String getString(String paramString, String defaultValue) {
        getInstance();
        return settings.getString(paramString, defaultValue);
    }

    /**
     *
     * @param paramString 配置名称
     * @param paramValue  配置值
     */
    public static void setInt(String paramString, int paramValue) {
        getInstance();
        editor.putInt(paramString, paramValue);
        apply(editor);
    }

    /**
     *
     * @param paramString   配置名称
     * @param defaultValue  返回的默认值
     * @return
     */
    public static int getInt(String paramString, int defaultValue) {
        getInstance();
        return settings.getInt(paramString, defaultValue);
    }

    /**
     *
     * @param paramString 配置名称
     * @param paramValue  配置值
     */
    public void setLong(String paramString, long paramValue) {
        getInstance();
        editor.putLong(paramString, paramValue);
        apply(editor);
    }

    /**
     *
     * @param paramString   配置名称
     * @param defaultValue  返回的默认值
     * @return
     */
    public long getLong(String paramString, long defaultValue) {
        getInstance();
        return settings.getLong(paramString, defaultValue);
    }

    public void setFloat(String paramString, float paramValue) {
        getInstance();
        editor.putFloat(paramString, paramValue);
        apply(editor);
    }
    public float getFloat(String paramString, float defaultValue) {
        getInstance();
        return settings.getFloat(paramString, defaultValue);
    }

    public void clear() {
        getInstance();
        settings.edit().clear().commit();
    }
}
