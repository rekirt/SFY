package com.cchtw.sfy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.StartToUseJsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.PhoneUtils;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.SimpleTextWatcher;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120031;
import com.itech.message.APP_120032;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StartToUseActivity extends BaseActivity{
	private Button butregister, btn_getcode;
	private EditText editcode, editphone;
    private CheckBox cb_privacy;
	// 通过设备读取的手机号码
	private APP_120032 returnapp;
    private boolean isGetCode = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(this, R.layout.activity_start_to_use, null);
        setContentView(view);
		initView();
        initData();
	}

    private String mPhoneNumber;
	private void initView() {
		butregister = (Button) findViewById(R.id.zhucejihuo);
        btn_getcode = (Button) findViewById(R.id.btn_get_code);
        editcode = (EditText) findViewById(R.id.edt_code);
        editphone = (EditText) findViewById(R.id.edt_phone);
        cb_privacy = (CheckBox) findViewById(R.id.cb_privacy);
        cb_privacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, b);//是否记住密码
            }
        });
        editcode.setEnabled(true);
	}

	private void initData(){
        Boolean isRemmber = SharedPreferencesHelper.getBoolean(Constant.ISREMMBER, false);//是否记住密码
        if (isRemmber){
            editphone.setText(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        }
        String phone = getIntent().getStringExtra("mEditPhone");
        if (!TextUtils.isEmpty(phone)){
            editphone.setText(phone);
        }
        btn_getcode.setOnClickListener(this);
        butregister.setOnClickListener(this);
        editphone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (count == 1) {
                    int length = s.toString().length();
                    if (length == 3 || length == 8) {
                        editphone.setText(s + " ");
                        editphone.setSelection(editphone.getText().toString().length());
                    }
                }
                if (PhoneUtils.isPhoneNumberValid(s.toString().replaceAll(" ", ""))) {
                    btn_getcode.setClickable(true);
                    btn_getcode.setEnabled(true);
                } else {
                    btn_getcode.setClickable(false);
                    btn_getcode.setEnabled(false);
                }
            }
        });
        editcode.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                int length = s.toString().length();
                if (length >= 6) {
                    butregister.setClickable(true);
                    butregister.setEnabled(true);
                } else {
                    butregister.setClickable(false);
                    butregister.setEnabled(false);
                }
            }
        });
        editphone.setSelection(editphone.getText().toString().length());
        if (PhoneUtils.isPhoneNumberValid(editphone.getText().toString().replaceAll(" ",""))) {
            btn_getcode.setClickable(true);
            btn_getcode.setEnabled(true);
        } else {
            btn_getcode.setClickable(false);
            btn_getcode.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        mPhoneNumber = editphone.getText().toString().replaceAll(" ", "");
        switch (view.getId()){
            case R.id.btn_get_code:
                    getCode();
                break;
            case R.id.zhucejihuo:
                if (isGetCode){
                    StartToUse();
                }else {
                    ToastHelper.ShowToast("请先重新请求初始密码!");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 进行倒计时
     *
     */
    Timer timer ;
    private int recLen = 30;
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
                    btn_getcode.setEnabled(false);
                    recLen--;
                    btn_getcode.setText(recLen+"S重新获取");
                    if(recLen < 0){
                        timer.cancel();
                        btn_getcode.setText("获取初始密码");
                        btn_getcode.setClickable(true);
                        btn_getcode.setEnabled(true);
                    }
                }
            });
        }
    };

    /**
     * 判断是否短时间内重复点击
     */
    private long lastClick = 0;

    public boolean IsNotDuplication() {
        if (System.currentTimeMillis() - lastClick <= 2000) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }


    // 获得验证码
    String sn = "";
    private void getCode(){
                // 发送报文获取验证码

        DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求...", true, true);
        APP_120031 app = new APP_120031();
        mPhoneNumber = editphone.getText().toString().replaceAll(" ", "");
        sn = System.currentTimeMillis()+"";
        SharedPreferencesHelper.setString(mPhoneNumber+"sn",sn);
        app.setMobile(mPhoneNumber);
        app.setUserName(mPhoneNumber);
        app.setTrxCode("120031");
        app.setType("2");
        app.setValSn(sn);
        app.setReqSn(sn);
        SharedPreferencesHelper.setBoolean(mPhoneNumber+Constant.ACTIVATION, false);
        ApiRequest.getMsgCode(app, mPhoneNumber, new StartToUseJsonHttpHandler(StartToUseActivity.this) {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        APP_120031 returnapp = JSON.parseObject(responseJsonObject.toString(),
                                APP_120031.class);
                        if (returnapp.getDetailCode().equals("0000")) {
                            isGetCode = true;
                            ToastHelper.ShowToast("请求发送成功");
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
                });
    }

    // 启用
    private void StartToUse(){
        final APP_120032 app = new APP_120032();
        app.setTrxCode("120032");
        app.setUserName(mPhoneNumber);
        String strserect = editcode.getText().toString().replaceAll(" ","");
        if (TextUtils.isEmpty(strserect)) {
            ToastHelper.ShowToast("初始密码不能为空");
            return;
        }
        sn = SharedPreferencesHelper.getString(mPhoneNumber+"sn", "");
        if (TextUtils.isEmpty(sn)){
            ToastHelper.ShowToast("请先获取短信初始密码！");
            return;
        }
        app.setReqSn(sn);
        try {
            app.setUserPass(null, strserect);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求启用...", true, false);
        ApiRequest.requestStart(app, mPhoneNumber, new StartToUseJsonHttpHandler(StartToUseActivity.this) {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120032.class);
                        if ("0000".equals(returnapp.getDetailCode())){
                            SharedPreferencesHelper.setBoolean(mPhoneNumber+Constant.ISFORCEDOFFLINE, false);
                            SharedPreferencesHelper.setBoolean(mPhoneNumber + Constant.ACTIVATION, true);// 记录启用成功状态
                            SharedPreferencesHelper.setString(Constant.PHONE, mPhoneNumber);// 保存字符串
                            SharedPreferencesHelper.setString(mPhoneNumber+Constant.DESKEY, returnapp.getDesKey());
                            SharedPreferencesHelper.setString(mPhoneNumber + Constant.DESK3KEY, returnapp.getDes3Key());
                            SharedPreferencesHelper.setString(mPhoneNumber + Constant.TOKEN, returnapp.getToken());
                            if (cb_privacy.isChecked()){
                                SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, true);//是否记住密码
                            }else {
                                SharedPreferencesHelper.setBoolean(Constant.ISREMMBER, false);//是否记住密码
                            }
                            Intent intent = new Intent();
                            intent.putExtra("isFromStartToUseActivity",true);
                            intent.setClass(StartToUseActivity.this, ChangePwdActivity.class);
                            startActivity(intent);
                        }else {
//                            ToastHelper.ShowToast(returnapp.getDetailInfo());
                            ToastHelper.ShowToast("启用失败，密码错误!");
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
                }.setIsNeedToReturnResponseBody(true)
        );
    }
}
