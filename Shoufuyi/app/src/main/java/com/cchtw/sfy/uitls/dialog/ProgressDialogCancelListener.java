package com.cchtw.sfy.uitls.dialog;import android.app.Dialog;import android.content.DialogInterface;import com.cchtw.sfy.api.AsyncHttp;import com.cchtw.sfy.uitls.TLog;/** *  * Description: * Created by FuHL on * Date:2015-09-06 * Time:下午4:39 * Copyright © 2015年 FuHL. All rights reserved. */public  class ProgressDialogCancelListener implements Dialog.OnCancelListener{    @Override    public void onCancel(DialogInterface dialogInterface) {        AsyncHttp.getClient().cancelAllRequests(true);        AsyncHttp.getLoginClient().cancelAllRequests(true);        TLog.analytics("ProgressDialogCancelListener--onCancel-----");    }}