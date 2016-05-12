package com.example.shoufuyi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.CommonUtils;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.itech.message.APP_120031;
import com.itech.message.APP_120032;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartToUseActivity extends Activity implements OnClickListener{
	private Button butregister, butsent;
	private EditText editcode, editphone;
	private String strcode, strphone;

	// 通过设备读取的手机号码
	private APP_120032 returnapp;
    private SharedPreferencesHelper sharedPreferencesHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(this, R.layout.activity_start_to_use, null);
        setContentView(view);
        sharedPreferencesHelper =  SharedPreferencesHelper.getInstance();
		initView();
        initData();
	}

	private void initView() {
		butregister = (Button) findViewById(R.id.zhucejihuo);
        editcode = (EditText) findViewById(R.id.edt_code);
        editphone = (EditText) findViewById(R.id.edt_phone);
        butsent = (Button) findViewById(R.id.btn_get_code);
		editphone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
		char[] mychar = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
		editphone.setKeyListener(CommonUtils.getKeylistener(mychar));
		strcode = editcode.getText().toString().trim();
		strphone = editphone.getText().toString().trim();
		strphone=strphone.replace(" ", "");
	}

	private void initData(){
        butsent.setOnClickListener(this);
        butregister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btn_get_code:
                if (IsDuplication()){
                    getCode();
                }
                break;
            case R.id.zhucejihuo:
                if (IsDuplication()){
                    StartToUse();
                }
                break;
            default:
                break;
        }
    }
    /**
     * 判断是否短时间内重复点击
     */
    private long lastClick = 0;

    public boolean IsDuplication() {
        if (System.currentTimeMillis() - lastClick <= 2000) {
            return true;
        }
        lastClick = System.currentTimeMillis();
        return false;
    }


    // 获得验证码
    private void getCode(){
        Pattern p = Pattern.compile("[1][3-8]\\d{9}");
        Matcher m = p.matcher(strphone);
            if (m.matches()) {
                DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求...", false, false);
                // 发送报文获取验证码
                APP_120031 app = new APP_120031();
                app.setMobile(strphone);
                app.setUserName(strphone);
                app.setType("2");
                ApiRequest.getMsgCode(app, strphone, new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        APP_120031 returnapp = JSON.parseObject(
                                responseJsonObject.toString(),
                                APP_120031.class);
                        if (returnapp.getDetailCode().equals(
                                "0000")) {
                            ToastHelper.ShowToast("短信发送成功");
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
                        super.onFinish();
                        DialogHelper.dismissProgressDialog();
                    }
                });
            } else {
                ToastHelper.ShowToast("请输入正确格式的手机号码");
                editphone.setText("");
            }
    }

    // 启用
    private void StartToUse(){
        final APP_120032 app = new APP_120032();
        app.setUserName(strphone);
        String strserect = editcode.getText().toString().trim();
        if (strserect.equals("")) {
            ToastHelper.ShowToast("验证码不能为空");
            return;
        }
        try {
            app.setUserPass(null, strserect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DialogHelper.showProgressDialog(StartToUseActivity.this, "正在请求...", true, true);
        ApiRequest.requestData(app, strphone, new JsonHttpHandler() {
                    @Override
                    public void onDo(JSONObject responseJsonObject) {
                        returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120032.class);
                        sharedPreferencesHelper.setString("jihuo", "0000");// 记录登录成功状态
                        sharedPreferencesHelper.setString("phone", strphone);// 保存字符串
                        sharedPreferencesHelper.setString("deskey", returnapp.getDesKey());
                        sharedPreferencesHelper.setString("des3key", returnapp.getDes3Key());
                        sharedPreferencesHelper.setString("token", returnapp.getToken());
                        sharedPreferencesHelper.setString("uuid", Constant.getUUID());
                        Intent intent = new Intent();
                        intent.setClass(StartToUseActivity.this, ChangePwdActivity.class);
                        startActivity(intent);
                        finish();
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
                }
        );
    }
}
