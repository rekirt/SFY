package com.cchtw.sfy.uitls;

import android.content.Context;
import android.widget.Toast;
import com.cchtw.sfy.BaseApplication;


/**
 * TaiJiQuan
 * Description:
 * Created by Fu.H.L on
 * Date:2015-05-20
 * Time:下午4:59
 * Copyright © 2015年 Fu.H.L All rights reserved.
 */
public class ToastHelper {

    public static Toast mToast;

    public static void ShowToast(int resId, Context context) {
        String text = context.getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void ShowToast(String text, Context context) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void ShowToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseApplication.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
    public static void ShowToast(String text,int type) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseApplication.getInstance(), text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }
    public static void ShowToast(int resId) {
        String text = BaseApplication.getInstance().getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(BaseApplication.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
