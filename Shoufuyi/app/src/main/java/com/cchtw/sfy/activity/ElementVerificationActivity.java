package com.cchtw.sfy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.PhoneUtils;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.csii.pe.enter.CSIIPinConvertor;
import com.csii.powerenter.PEEditText;
import com.csii.powerenter.PEEditTextAttrSet;
import com.itech.message.APP_120001;
import com.itech.message.APP_120002;
import com.itech.message.APP_120031;
import com.itech.message.VerifyGroup;
import com.itech.message.VerifyItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ElementVerificationActivity extends BaseActivity {
    private String PublicKey = "e772a7ba31dc574f7adb3a0b8a05bc7780146fed534b72e2c921ab5e11791608d44212f323a3c233f8721cf8546ade8c4dc8162b79005489ee821b4d3875eb048f762359c077094cc013e1f85fb45068500d1e4b31a060eed42aede6f2872f3f4110adc443be174410618bf4b75e5122ea7e17ed3c1dd5929d3ae84c1c1c1295";
    private LinearLayout ll_new_sign_id;
    private LinearLayout ll_new_sign_name;
    private LinearLayout ll_new_sign_card_number;
    private LinearLayout ll_new_sign_phone_number;
    private LinearLayout ll_new_sign_verif_code;
    private EditText edt_new_sign_id;
    private EditText edt_new_sign_name;
    private EditText edt_new_sign_card_number;
    private EditText edt_new_sign_phone_number;
    private EditText edt_verif_code;
    private PEEditText password;
    private Button btn_get_code;
    private Button btn_submit;
    private APP_120001 mReturn = new APP_120001();
    private String mElementVerifyGroupCode;
    int mElementMember = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_verification);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mReturn = (APP_120001) bundle.get("return");
        initView();
        initData();
        judgeElement();
    }

    List<VerifyGroup> verifyGroupList;

    /**
     * 判断是几要素验证
     */
    private void judgeElement() {// 根据要素验证显示不同界面
        if (mReturn != null) {
            verifyGroupList = mReturn.getVerifyGroupList();
            if (verifyGroupList.size() >= 1) {
                for (VerifyGroup verifyGroup : verifyGroupList) {
                    switch (verifyGroup.getVerifyGroupCode()){
                        case "SIX_ELEMENT"://六要素(卡号\户名\身份证号\手机号\手机验证码\支付密码)
                            mElementMember = 6;
                            break;
                        case "TWO_ELEMENT"://二要素(卡号\户名)
                            mElementMember = 2;
                            break;
                        case "THREE_ELEMENT"://三要素(卡号\户名\身份证号)
                            mElementMember = 3;
                            break;
                        case "FOUR_ELEMENT"://四要素(卡号\户名\身份证号\手机号)
                            mElementMember = 4;
                            break;
                        default:
                            break;
                    }
                    mElementVerifyGroupCode = verifyGroup.getVerifyGroupCode();
                }
            }
            changeView(mElementMember);
        }
    }

    /**
     * 初始化布局
     */
    private void initView(){
        edt_new_sign_id = (EditText) findViewById(R.id.edt_new_sign_id);
        edt_new_sign_name = (EditText) findViewById(R.id.edt_new_sign_name);
        edt_new_sign_card_number = (EditText) findViewById(R.id.edt_new_sign_card_number);
        edt_new_sign_phone_number = (EditText) findViewById(R.id.edt_new_sign_phone_number);
        edt_verif_code = (EditText) findViewById(R.id.edt_verif_code);
        password = (PEEditText) findViewById(R.id.password);
        btn_get_code = (Button) findViewById(R.id.btn_get_code);
        btn_submit = (Button) findViewById(R.id.btn_submit);
    }

    /**
     * 初始化数据
     */
    private void initData(){
        btn_get_code.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        //密码输入框
        PEEditTextAttrSet attrs = new PEEditTextAttrSet();
        attrs.name = "CSII-POWERENTER";
        attrs.encryptType = 9;
        attrs.clearWhenOpenKbd = false;
        attrs.softkbdType = 1;
        attrs.softkbdMode = 0;
        attrs.kbdRandom = false;
        attrs.kbdVibrator = true;
        attrs.whenMaxCloseKbd = true;
        attrs.minLength = 6;
        attrs.maxLength = 6;
        password.initialize(attrs);
        // editmima.setWEEncryptFactor("6228480084728922117");
        password.setWEEncryptFactor(mReturn.getAccountNo());
        password.setPublicKey(PublicKey);
        // editmima.clear();

        //基本信息
        edt_new_sign_id.setText(mReturn.getIdCard());
        edt_new_sign_card_number.setText(mReturn.getAccountNo());
        edt_new_sign_name.setText(mReturn.getAccountName());
        edt_new_sign_phone_number.setText(mReturn.getMobile());
        edt_new_sign_id.setEnabled(false);
        edt_new_sign_card_number.setEnabled(false);
        edt_new_sign_name.setEnabled(false);
        edt_new_sign_phone_number.setEnabled(false);
        setCanBack(true);
    }

    /**
     * 根据验证要素显示布局
     * @param mMember 几要素
     */
    private void changeView(int mMember){
        switch (mMember) {
            case 2:
                password.setVisibility(View.GONE);
                ll_new_sign_id.setVisibility(View.GONE);
                ll_new_sign_phone_number.setVisibility(View.GONE);
                ll_new_sign_verif_code.setVisibility(View.GONE);
                break;
            case 3:
                password.setVisibility(View.GONE);
                ll_new_sign_phone_number.setVisibility(View.GONE);
                ll_new_sign_verif_code.setVisibility(View.GONE);
                break;
            case 4:
                password.setVisibility(View.GONE);
                ll_new_sign_id.setVisibility(View.GONE);
                ll_new_sign_verif_code.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.btn_get_code:
                if (PhoneUtils.isPhoneNumberValid(edt_new_sign_phone_number.getText().toString())){
                    getMsgCode();
                }else {
                    ToastHelper.ShowToast("手机号码无效");
                }
                break;
            case R.id.btn_submit:
                submitForCheck();
                break;
        }
    }

    /**
     * 获取验证码
     */
    String sn = "";
    private void getMsgCode(){
        DialogHelper.showProgressDialog(ElementVerificationActivity.this, "正在请求...", true, true);
        APP_120031 app = new APP_120031();
        app.setMobile(edt_new_sign_phone_number.getText().toString());
        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        app.setType("1");
        sn = System.currentTimeMillis()+"";
        app.setValSn(sn);
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        APP_120031 returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120031.class);
                        if ("0000".equals(returnapp.getDetailCode())) {
                            ToastHelper.ShowToast("短信发送成功");
                            Countdown();
                        } else {
                            ToastHelper.ShowToast(returnapp.getDetailInfo());
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
                }
        );
    }

    private void submitForCheck(){
        boolean isNotIn = false;
        APP_120002 app = new APP_120002();
        app.setMerchantId(AccountHelper.getMerchantId());
        app.setUserName(AccountHelper.getUserName());
        app.setIdCard(mReturn.getIdCard());
        app.setAccountNo(mReturn.getAccountNo());
        app.setVerifyGroupCode(mElementVerifyGroupCode);
        List<VerifyItem> verifyItemList = new ArrayList<VerifyItem>();
        switch (mElementMember){
            case 2:
                VerifyItem[] mTwoVerifyItems = new VerifyItem[2];
                mTwoVerifyItems[0] = new VerifyItem();
                mTwoVerifyItems[0].setVerifyItemCode("ACCOUNT_NO");
                mTwoVerifyItems[0].setVerifyItemValue(mReturn.getAccountNo());
                mTwoVerifyItems[1] = new VerifyItem();
                mTwoVerifyItems[1].setVerifyItemCode("ACCOUNT_NAME");
                mTwoVerifyItems[1].setVerifyItemValue(mReturn.getAccountName());
                for (VerifyItem it : mTwoVerifyItems) {
                    verifyItemList.add(it);
                }
                break;
            case 3:
                VerifyItem[] mThreeVerifyItems = new VerifyItem[3];
                mThreeVerifyItems[0] = new VerifyItem();
                mThreeVerifyItems[0].setVerifyItemCode("ACCOUNT_NO");
                mThreeVerifyItems[0].setVerifyItemValue(mReturn.getAccountNo());
                mThreeVerifyItems[1] = new VerifyItem();
                mThreeVerifyItems[1].setVerifyItemCode("ACCOUNT_NAME");
                mThreeVerifyItems[1].setVerifyItemValue(mReturn.getAccountName());
                mThreeVerifyItems[2] = new VerifyItem();
                mThreeVerifyItems[2].setVerifyItemCode("IDCARD");
                mThreeVerifyItems[2].setVerifyItemValue(mReturn.getIdCard());
                for (VerifyItem it : mThreeVerifyItems) {
                    verifyItemList.add(it);
                }
                break;
            case 4:
                VerifyItem[] mFourVerifyItems = new VerifyItem[4];
                mFourVerifyItems[0] = new VerifyItem();
                mFourVerifyItems[0].setVerifyItemCode("ACCOUNT_NO");
                mFourVerifyItems[0].setVerifyItemValue(mReturn.getAccountNo());
                mFourVerifyItems[1] = new VerifyItem();
                mFourVerifyItems[1].setVerifyItemCode("ACCOUNT_NAME");
                mFourVerifyItems[1].setVerifyItemValue(mReturn.getAccountName());
                mFourVerifyItems[2] = new VerifyItem();
                mFourVerifyItems[2].setVerifyItemCode("IDCARD");
                mFourVerifyItems[2].setVerifyItemValue(mReturn.getIdCard());
                mFourVerifyItems[3] = new VerifyItem();
                mFourVerifyItems[3].setVerifyItemCode("MOBILE");
                mFourVerifyItems[3].setVerifyItemValue(mReturn.getMobile());
                for (VerifyItem it : mFourVerifyItems) {
                    verifyItemList.add(it);
                }
                break;
            case 6:
                VerifyItem[] mSixVerifyItems = new VerifyItem[6];
                mSixVerifyItems[0] = new VerifyItem();
                mSixVerifyItems[0].setVerifyItemCode("ACCOUNT_NO");
                mSixVerifyItems[0].setVerifyItemValue(mReturn.getAccountNo());
                mSixVerifyItems[1] = new VerifyItem();
                mSixVerifyItems[1].setVerifyItemCode("ACCOUNT_NAME");
                mSixVerifyItems[1].setVerifyItemValue(mReturn.getAccountName());
                mSixVerifyItems[2] = new VerifyItem();
                mSixVerifyItems[2].setVerifyItemCode("IDCARD");
                mSixVerifyItems[2].setVerifyItemValue(mReturn.getIdCard());
                mSixVerifyItems[3] = new VerifyItem();
                mSixVerifyItems[3].setVerifyItemCode("MOBILE");
                mSixVerifyItems[3].setVerifyItemValue(mReturn.getMobile());
                mSixVerifyItems[4] = new VerifyItem();
                mSixVerifyItems[4].setVerifyItemCode("VAL_CODE");
                String verif_code = edt_verif_code.getText().toString().trim();
                if (TextUtils.isEmpty(verif_code)){
                    ToastHelper.ShowToast("验证码不能为空");
                    return;
                }else {
                    mSixVerifyItems[4].setVerifyItemValue(verif_code);
                    app.setValCode(verif_code);
                }
                mSixVerifyItems[5] = new VerifyItem();
                mSixVerifyItems[5].setVerifyItemCode("PASS");

                Date date = new Date();
                Long msec = date.getTime();
                String timeStamp = Long.toString(msec);
                long time = (long) (java.lang.System.currentTimeMillis());
                String mima = password.getValue(timeStamp);
                int v_check = password.validity_check();
                if (v_check == -1) {
                    ToastHelper.ShowToast("密码不能为空");
                    return;
                } else if (v_check == -2) {
                    ToastHelper.ShowToast("密码长度小于最小长度");
                    return;
                } else if (v_check == -3) {
                    ToastHelper.ShowToast("密码内容不合法");
                    return;
                }


                CSIIPinConvertor convertor = new CSIIPinConvertor();
                convertor.setTimeout(600);
                try {
                    String utf_mima = new String(mima.getBytes(), "UTF-8");
                    String decryptedPass = convertor.convert(utf_mima);
                    mSixVerifyItems[5].setVerifyItemValue(decryptedPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (VerifyItem it : mSixVerifyItems) {
                    verifyItemList.add(it);
                }
                break;
            default:
                ToastHelper.ShowToast("没有对应的验证要素");
                isNotIn = true;
                break;
        }
        if (isNotIn){
            return;
        }
        if (TextUtils.isEmpty(sn)){
            ToastHelper.ShowToast("请先获取短信验证码！");
            return;
        }
        app.setValSn(sn);
        app.setVerifyItemList(verifyItemList);
        DialogHelper.showProgressDialog(ElementVerificationActivity.this, "正在请求...", true, true);
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120031 returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120031.class);
                    if ("0000".equals(returnapp.getDetailCode())) {
                        ToastHelper.ShowToast("验证成功");
                        ElementVerificationActivity.this.finish();
                    }else {
                        ToastHelper.ShowToast(returnapp.getDetailInfo());
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
        });
    }
    /**
     * 进行倒计时
     *
     */
    Timer timer ;
    private int recLen = 10;
    private MyTask task;
    public void Countdown(){
        recLen= 10;
        if (timer == null){
            timer = new Timer();
        }else {
            timer.cancel();
            timer = new Timer();
        }
        if (task != null){
            task.cancel();
            task = new MyTask();
        }else {
            task = new MyTask();
        }
        timer.schedule(task, 100,1000);
    }

    /**
     * 执行倒计时任务
     *
     */
    class MyTask extends TimerTask {
        @Override
        public void run(){
            runOnUiThread(new Runnable() {      // UI thread
                @Override
                public void run() {
                    btn_get_code.setEnabled(false);
                    recLen--;
                    btn_get_code.setText(recLen+"S重新获取");
                    if(recLen < 0){
                        timer.cancel();
                        btn_get_code.setText("获取验证码");
                        btn_get_code.setClickable(true);
                        btn_get_code.setEnabled(true);
                    }
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        password.onDestroy(); // 释放资源
        super.onDestroy();
    }
}
