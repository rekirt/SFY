package com.example.shoufuyi.api;

import android.content.Context;
import android.text.TextUtils;

import com.example.shoufuyi.BaseApplication;
import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.ToastHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public abstract class JsonHttpHandler extends AsyncHttpResponseHandler {
    private Context mContext;
    private boolean isShowProgressDialog = false;
    private boolean isShowErrorMessage = true;
    private boolean isProgressDialogCancleable = true;
    private boolean isProgressDialogCanceledOnTouchOutside = true;
    private boolean isNeedToReturnResponseBody = false;
    private String mDialogMessage;
    private String mStatusTag = "status";
    private String mMessageTag = "message";
    private String mDataTag = "data";

    public JsonHttpHandler() {
        this.mContext = BaseApplication.getInstance();;
    }

    /**
     * 传入接口返回字段的构造函数
     *
     * @param statusTag     状态标识
     * @param messageTag    返回提示信息
     * @param dataTag       数据标识
     */
    public JsonHttpHandler(String statusTag,
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
        try {
            JSONObject response = new JSONObject(new String(responseBody, "UTF-8"));
            int status = response.getInt(this.mStatusTag);
            if (0 == status) {
                try {
                    if (isNeedToReturnResponseBody){
                        onDo(response);
                    }else {
                        onDo(response.getJSONObject(this.mDataTag));
                    }
                } catch (JSONException e) {
                    try {
                        onDo(response.getJSONArray(this.mDataTag));
                    } catch (JSONException e1) {
                        try{
                            onDo(response.getString(this.mDataTag));
                        }catch (JSONException e2){
                            e2.printStackTrace();
                        }
                    }
                }
            } else {
                onFail(response.getString(mMessageTag));
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFail("数据请求成功，数据出错了");
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        if (mContext != null) {
            try {
                if (statusCode == 0) {
                    onFail(mContext.getString(R.string.error_http_fail_connet_server));
                } else if (statusCode >= 400) {
                    onFail(mContext.getString(R.string.error_http_request));
                } else if (statusCode >= 500) {
                    onFail(mContext.getString(R.string.error_http_server_error));
                } else {
                    onFail(mContext.getString(R.string.error_http_server_busy));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                onFail(responseBody.toString());
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

    @Override
    public void onStart() {
        super.onStart();
        if (isShowProgressDialog) {
//            DialogHelper.getWaitDialog()
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
            try {
//              DialogHelper.dismissProgressDialog();
            } catch (Exception e) {
                e.printStackTrace();
        }
    }


    public JsonHttpHandler setProgressDialogCancleable(boolean cancleable) {
        this.isProgressDialogCancleable = cancleable;
        return this;
    }
    public JsonHttpHandler setProgressDialogCanceledOnTouchOutside(boolean CanceledOnTouchOutside) {
        this.isProgressDialogCanceledOnTouchOutside = CanceledOnTouchOutside;
        return this;
    }

    /**
     * 是否显示加载中。。。
     * @return JsonHttpHandler
     */
    public JsonHttpHandler showProgressDialog(boolean isShow) {
        isShowProgressDialog = isShow;
        return this;
    }

    /**
     * 隐藏失败信息
     */
    public JsonHttpHandler hideErrorMessage() {
        isShowErrorMessage = false;
        return this;
    }

    /**
     * 请求数据前显示自定义信息
     * @param message 自定义信息
     * @return JsonHttpHandler
     */
    public JsonHttpHandler setShowProgressDialog(String message) {
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
    public JsonHttpHandler setIsNeedToReturnResponseBody(boolean isNeedToReturnResponseBody) {
        this.isNeedToReturnResponseBody = isNeedToReturnResponseBody;
        return this;
    }
}
