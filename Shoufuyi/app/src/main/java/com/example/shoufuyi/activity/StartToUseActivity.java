package com.example.shoufuyi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.PhoneUtils;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.SimpleTextWatcher;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.itech.message.APP_120031;
import com.itech.message.APP_120032;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StartToUseActivity extends BaseActivity{
	private Button butregister, btn_getcode;
	private EditText editcode, editphone;

	// 通过设备读取的手机号码
	private APP_120032 returnapp;
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
	}

	private void initData(){
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
        //为了方便测试，暂时将号码写入输入框
        editphone.setText("134 3099 0001");
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
        switch (view.getId()){
            case R.id.btn_get_code:
                    getCode();
                break;
            case R.id.zhucejihuo:
                    StartToUse();
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
                    btn_getcode.setEnabled(false);
                    recLen--;
                    btn_getcode.setText(recLen+"S重新获取");
                    if(recLen < 0){
                        timer.cancel();
                        btn_getcode.setText("获取验证码");
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
    private void getCode(){
                //ToastHelper.ShowToast("发送成功！");
//                Countdown();
                // 发送报文获取验证码
                 DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求验证码...", true, true);
                APP_120031 app = new APP_120031();
                mPhoneNumber = editphone.getText().toString().replaceAll(" ", "");
                app.setMobile(mPhoneNumber);
                app.setUserName(mPhoneNumber);
                app.setTrxCode("120031");
                app.setType("2");
                ApiRequest.getMsgCode(app, mPhoneNumber, new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        APP_120031 returnapp = JSON.parseObject(responseJsonObject.toString(),
                                APP_120031.class);
                        if (returnapp.getDetailCode().equals("0000")) {
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
                });
    }

    // 启用
    private void StartToUse(){
        final APP_120032 app = new APP_120032();
        app.setTrxCode("120032");
        app.setUserName(mPhoneNumber);
        String strserect = editcode.getText().toString().replaceAll(" ","");
        if (strserect.equals("")) {
            ToastHelper.ShowToast("验证码不能为空");
            return;
        }
        try {
            app.setUserPass(null, strserect);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求启用...", true, false);
        ApiRequest.requestData(app, mPhoneNumber, new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120032.class);
                        if ("0000".equals(returnapp.getDetailCode())){
                            SharedPreferencesHelper.setBoolean(Constant.ACTIVATION, true);// 记录登录成功状态
                            SharedPreferencesHelper.setString(Constant.PHONE, mPhoneNumber);// 保存字符串
                            SharedPreferencesHelper.setString(Constant.DESKEY, returnapp.getDesKey());
                            SharedPreferencesHelper.setString(Constant.DESK3KEY, returnapp.getDes3Key());
                            SharedPreferencesHelper.setString(Constant.TOKEN, returnapp.getToken());
                            Intent intent = new Intent();
                            intent.setClass(StartToUseActivity.this, ChangePwdActivity.class);
                            startActivity(intent);
                            finish();
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
                        super.onFinish();
                        DialogHelper.dismissProgressDialog();
                    }
                }.setIsNeedToReturnResponseBody(true)
        );
    }
}
