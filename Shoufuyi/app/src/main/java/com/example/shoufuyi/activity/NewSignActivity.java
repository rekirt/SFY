package com.example.shoufuyi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.cache.FileUtils;
import com.example.shoufuyi.cache.v2.CacheManager;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.PhoneUtils;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.itech.message.APP_120001;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Shoufuyi
 * Description:
 * Created by fuhongliang on
 * Date:16/5/18
 * Time:下午2:48
 * Copyright © 2016-05-16/5/18 Jason. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class NewSignActivity extends BaseActivity{
    private String returnAction = "android.intent.action.MAIN";
    private String resultAction = "";
    private EditText mEdtNewSignId;
    private EditText mEdtNewSignName;
    private EditText mEdtNewSignCardNumber;
    private EditText mEdtNewSignPhoneNumber;
    private ImageView mIvIdCamera;
    private ImageView mIvCardCamera;
    private Button mbtnOk;
    private CheckBox mCbAuto;
    private APP_120001 mReturnApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sign);
        initView();
        initData();
        setCanBack(true);
    }


    private void initView(){
        mEdtNewSignId = (EditText) findViewById(R.id.edt_new_sign_id);
        mEdtNewSignName = (EditText) findViewById(R.id.edt_new_sign_name);
        mEdtNewSignCardNumber = (EditText) findViewById(R.id.edt_new_sign_card_number);
        mEdtNewSignPhoneNumber = (EditText) findViewById(R.id.edt_new_sign_phone_number);
        mIvIdCamera = (ImageView) findViewById(R.id.iv_id_camera);
        mIvCardCamera = (ImageView) findViewById(R.id.iv_card_camera);
        mbtnOk = (Button) findViewById(R.id.btn_ok);
        mCbAuto = (CheckBox) findViewById(R.id.cb_auto);
    }

    private void initData(){
        mIvIdCamera.setOnClickListener(this);
        mIvCardCamera.setOnClickListener(this);
        mbtnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_ok:
                if (prepareToPost()){
                    handlePost();
                }
                break;
            case R.id.iv_id_camera:
                ToastHelper.ShowToast("暂时没法通过拍照识别");
                break;
            case R.id.iv_card_camera:
                ToastHelper.ShowToast("暂时没法通过拍照识别");
//                Intent intentTack = new Intent("com.wintone.bankcard.camera.ScanCamera");
//                intentTack.putExtra("devCode", Constant.devcode);
//                intentTack.putExtra("CopyrightInfo", "");
//                intentTack.putExtra("ReturnAciton", returnAction);
//                intentTack.putExtra("ResultAciton", resultAction);
//                startActivity(intentTack);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case 1:
//                String recogResult = data.getStringExtra("recogResult");
//                idpath = data.getStringExtra("path");
//                if (recogResult.equals("")) {
//                } else {
//                    String[] splite_Result = recogResult.split(",");
//                    String recogname = splite_Result[0].substring(3,
//                            splite_Result[0].length());
//                    String recogidcard = splite_Result[5].substring(7,
//                            splite_Result[5].length());
//                    if (!recogname.equals("")) {
//                        editname.setText(recogname);
//                    }
//                    if (!recogidcard.equals("")) {
//                        editidcard.setText(recogidcard);
//                    }
//                }
//                break;
//        }
    }

    private void handlePost(){
        final APP_120001 app = new APP_120001();
        app.setTrxCode("120001");
        app.setAccountName(mNewSignName);
        app.setAccountNo(mCardNumber);
        app.setIdCard(mIdCardNumber);
        app.setMobile(mNewSignPhoneNumber);
        if (mCbAuto.isChecked()) {
            app.setAutoSign("Y");
        } else {
            app.setAutoSign("N");
        }
        // 新增类型
        app.setReqType("1");
        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        app.setMerchantId(SharedPreferencesHelper.getString(Constant.MERCHANT, ""));

        DialogHelper.showProgressDialog(NewSignActivity.this, "正在操作，请稍候...", true, false);

        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                try {
                    mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120001.class);
                    if("0000".equals(mReturnApp.getDetailCode())){
                        ToastHelper.ShowToast("录入成功");
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("return", mReturnApp);
                        intent.putExtras(bundle);
                        intent.setClass(NewSignActivity.this, ElementVerificationActivity.class);
                        startActivity(intent);
                    }else {
                        ToastHelper.ShowToast(mReturnApp.getDetailInfo());
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
            }

            @Override
            public void onFail(String msg) {

                CacheManager.setCache(FileUtils.getCacheKey(app.getIdCard(), app.getAccountNo()), app.toString().getBytes(),
                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
                ToastHelper.ShowToast("已保存在本地数据库.");
                super.onFail(msg);
                Intent intent = new Intent(NewSignActivity.this, UnfinishedActivity.class);
                startActivity(intent);
            }
        });
    }

    private String mIdCardNumber;// 身份证号
    private String mCardNumber;// 卡号
    private String mNewSignName;// 户名
    private String mNewSignPhoneNumber; // 手机号

    private boolean prepareToPost(){
        mIdCardNumber = mEdtNewSignId.getText().toString();
        mNewSignName = mEdtNewSignName.getText().toString();
        mCardNumber = mEdtNewSignCardNumber.getText().toString().replace(" ", "");
        mNewSignPhoneNumber = mEdtNewSignPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(mIdCardNumber)|| TextUtils.isEmpty(mNewSignName) || TextUtils.isEmpty(mCardNumber)
                || TextUtils.isEmpty(mNewSignPhoneNumber)){
            return false;
        }else {
            boolean isMatch = Pattern.matches("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])", mIdCardNumber);// 这里对身份证号码进行验证
            if (!isMatch) {
                ToastHelper.ShowToast("请填写有效的身份证号码");
                return false;
            }
            if (!PhoneUtils.isPhoneNumberValid(mNewSignPhoneNumber)) {
                ToastHelper.ShowToast("请填写有效的手机号码");
                return false;
            }
            if (mCardNumber.length() < 16 || mCardNumber.length() > 19) {
                ToastHelper.ShowToast("请填写有效的银行卡卡号");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
