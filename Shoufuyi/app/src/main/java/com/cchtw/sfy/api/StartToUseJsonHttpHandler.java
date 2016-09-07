package com.cchtw.sfy.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cchtw.sfy.BaseApplication;
import com.cchtw.sfy.R;
import com.cchtw.sfy.activity.StartToUseActivity;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.ActivityCollector;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.itech.message.APPMsgPack;
import com.itech.utils.ASCUtil;
import com.itech.utils.SequenceUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public abstract class StartToUseJsonHttpHandler extends AsyncHttpResponseHandler {
    private Context mContext;
    private boolean isShowProgressDialog = false;
    private boolean isShowErrorMessage = true;
    private boolean isProgressDialogCancleable = true;
    private boolean isProgressDialogCanceledOnTouchOutside = true;
    private boolean isNeedToReturnResponseBody = true;
    private String mDialogMessage;
    private String mStatusTag = "retCode";
    private String mMessageTag = "errMsg";
    private String mDataTag = "data";

    public StartToUseJsonHttpHandler(Context context) {
        this.mContext = context;
    }


    /**
     * 传入接口返回字段的构造函数
     *
     * @param statusTag     状态标识
     * @param messageTag    返回提示信息
     * @param dataTag       数据标识
     */
    public StartToUseJsonHttpHandler(String statusTag,
                                     String messageTag,
                                     String dataTag) {
        this.mContext = BaseApplication.getInstance();
        this.mStatusTag = statusTag;
        this.mMessageTag = messageTag;
        this.mDataTag = dataTag;
    }

    @Override
    public void onRetry(int retryNo) {
        super.onRetry(retryNo);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        APPMsgPack response = new APPMsgPack();
        String mPhoneNumber = SharedPreferencesHelper.getString(Constant.PHONE, "");// 保存字符串
        String deskey       = "";
        String token         = Constant.token;

        if (!TextUtils.isEmpty(deskey)){
            response.setDesKey(deskey);
        }
        response.setToken(token);
        String stye = ASCUtil.ASCToStr(ArrayUtils.subarray(responseBody, 75, 77));
        if ("0".equals(stye)){//加密方式为0,使用固态token解包
            response.setToken(SequenceUtil.TOKEN);
        }

        try {
            if (response.unpack(responseBody) == 0) { // 解析成功
                byte[] data = response.getMainData(); // 数据包字节数组
                JSONObject responsedata = new JSONObject(new String(data, "UTF-8"));
                String status = responsedata.getString(this.mStatusTag);
                switch (status){
                    case "0000":
                        handSuccess(responsedata);
                        break;
                    case "1018":
                        handForcedOffLine(responsedata);
                        break;
                    default:
                        onFail(responsedata.getString(mMessageTag));
                        break;
                }
            }else if (response.unpack(responseBody) == 1) {
                // 报文不完整
                onFail("报文不完整");
            } else if (response.unpack(responseBody) == 2) {
                // 报文指纹不正确
                AccountHelper.logout();
                Intent intent_login = new Intent();
                intent_login.setClass(mContext, StartToUseActivity.class);
                intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
                mContext.startActivity(intent_login);
                ActivityCollector.finishAll();
                onFail("报文指纹不正确");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handSuccess(JSONObject data){
        try {
            if (isNeedToReturnResponseBody) {
                onDo(data);
            } else {
                onDo(data.getJSONObject(this.mDataTag));
            }
        } catch (JSONException e) {
            try {
                onDo(data.getJSONArray(this.mDataTag));
            } catch (JSONException e1) {
                try {
                    onDo(data.getString(this.mDataTag));
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void handForcedOffLine(JSONObject data){
        try {
            ToastHelper.ShowToast(data.getString("errMsg"),1);
            SharedPreferencesHelper.setBoolean(AccountHelper.getUserName()+Constant.ISFORCEDOFFLINE, true);
            SharedPreferencesHelper.setString(AccountHelper.getUserName()+Constant.DESK3KEY, "");
            AccountHelper.logout();
            Intent intent_login = new Intent();
            intent_login.setClass(mContext, StartToUseActivity.class);
            intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
            mContext.startActivity(intent_login);
            ActivityCollector.finishAll();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        if (mContext != null) {
            try {
                if (statusCode == 0) {
                    onFail("连接超时，请重试!");
                } else if (statusCode >= 400) {
                    onFail(mContext.getString(R.string.error_http_request));
                } else if (statusCode >= 500) {
                    onFail(mContext.getString(R.string.error_http_server_error));
                } else {
                    onFail(mContext.getString(R.string.error_http_server_busy));
                }
//                onFail(responseBody.toString());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public abstract void onDo(JSONObject responseJsonObject);

    public abstract void onDo(JSONArray responseJsonArray);

    public abstract void onDo(String responseString);

    public void onFail(String msg) {
        if (!TextUtils.isEmpty(msg)&& mContext != null) {
            if (isShowErrorMessage) {
                ToastHelper.ShowToast(msg, mContext);
            }
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if (isShowProgressDialog) {
//        }
//    }

    @Override
    public void onFinish() {
        super.onFinish();
            try {
//              DialogHelper.dismissProgressDialog();
            } catch (Exception e) {
                e.printStackTrace();
        }
    }


    public StartToUseJsonHttpHandler setProgressDialogCancleable(boolean cancleable) {
        this.isProgressDialogCancleable = cancleable;
        return this;
    }
    public StartToUseJsonHttpHandler setProgressDialogCanceledOnTouchOutside(boolean CanceledOnTouchOutside) {
        this.isProgressDialogCanceledOnTouchOutside = CanceledOnTouchOutside;
        return this;
    }

    /**
     * 是否显示加载中。。。
     * @return JsonHttpHandler
     */
    public StartToUseJsonHttpHandler showProgressDialog(boolean isShow) {
        isShowProgressDialog = isShow;
        return this;
    }

    /**
     * 隐藏失败信息
     */
    public StartToUseJsonHttpHandler hideErrorMessage() {
        isShowErrorMessage = false;
        return this;
    }

    /**
     * 请求数据前显示自定义信息
     * @param message 自定义信息
     * @return JsonHttpHandler
     */
    public StartToUseJsonHttpHandler setShowProgressDialog(String message) {
        isShowProgressDialog = true;
        this.mDialogMessage = message;
        return this;
    }

    /**
     * 是否需要返回整个ResponseBody，由于接口不同意问题，有些返回结果并未包装在一个对象中，
     * 而是直接以字符串返回，所以如果设置需要返回整个ResponseBody，则返回所有服务器返回的信息
     * @param isNeedToReturnResponseBody 是否需要返回所有服务器返回的信息
     * @return JsonHttpHandler
     */
    public StartToUseJsonHttpHandler setIsNeedToReturnResponseBody(boolean isNeedToReturnResponseBody) {
        this.isNeedToReturnResponseBody = isNeedToReturnResponseBody;
        return this;
    }

}
