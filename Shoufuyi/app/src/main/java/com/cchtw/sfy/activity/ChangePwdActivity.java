package com.cchtw.sfy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.ActivityCollector;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120034;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChangePwdActivity extends BaseActivity {
	private EditText mEditOld;
	private EditText mEditNew;
	private EditText mEditSure;
	private SharedPreferencesHelper sharedPreferencesHelper;
	private Button butsubmit;
	// 商户手机号
	private String userPhone;
	private APP_120034 returnapp;
	private String deskey;
	private String des3key;
	private TextView texttitle;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_password);
		initView();
		initData();
	}

	private void initView() {
		mEditOld = (EditText) findViewById(R.id.edt_old_pwd);
		mEditNew = (EditText) findViewById(R.id.edt_new_pwd);
		mEditSure = (EditText) findViewById(R.id.edt_new_pwd_again);
		butsubmit = (Button) findViewById(R.id.btn_modify);
	}

	private void initData(){
		sharedPreferencesHelper = SharedPreferencesHelper.getInstance();// 指定操作的文件名称
        userPhone = sharedPreferencesHelper.getString(Constant.PHONE, "");
        deskey = sharedPreferencesHelper.getString(userPhone+Constant.DESKEY, "");
		des3key = sharedPreferencesHelper.getString(userPhone+Constant.DESK3KEY, "");
		butsubmit.setOnClickListener(this);
		setCanBack(true);
	}

	private void modifyPwd(){
    // 修改登录密码
        APP_120034 app = new APP_120034();
        try {
            DialogHelper.showProgressDialog(ChangePwdActivity.this, "正在请求...", true, false);
            final String newPwd = mEditNew.getText().toString();
            app.setUserName(userPhone);
            app.setOriPass(des3key, mEditOld.getText().toString());
            app.setNewPass(des3key, newPwd);
            ApiRequest.requestData(app, userPhone, new JsonHttpHandler(ChangePwdActivity.this) {
                @Override
                public void onDo(JSONObject responseJsonObject) {
                    returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120034.class);
                    if ("0000".equals(returnapp.getDetailCode())){
                        ToastHelper.ShowToast(returnapp.getDetailInfo());
                        SharedPreferencesHelper.setString(returnapp.getUserName() + "LoginSerect", newPwd);// 保存字符串
                        SharedPreferencesHelper.setBoolean(returnapp.getUserName() + Constant.GAIMIMA, true);
                        AccountHelper.logout();
                        Intent intent_login = new Intent();
                        intent_login.setClass(ChangePwdActivity.this,LoginActivity.class);
                        intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
                        startActivity(intent_login);
                        finish();
                        ActivityCollector.finishAll();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private boolean isPrepare() {
        if (TextUtils.isEmpty(mEditOld.getText().toString())) {
            ToastHelper.ShowToast("旧密码不能为空");
            return false;
        }
        if (mEditNew.getText().toString().length() < 6) {
            ToastHelper.ShowToast("新密码长度不能少于6位");
            return false;
        }
		if (!mEditNew.getText().toString().equals(mEditSure.getText().toString())) {
            ToastHelper.ShowToast("两次输入密码不相同");
            return false;
		}

        if (mEditOld.getText().toString().equals(mEditSure.getText().toString())){
            ToastHelper.ShowToast("新密码与原密码不可以相同哦~");
            return false;
        }
		return true;
	}

	@Override
	public void onClick(View view) {
        super.onClick(view);
        if (isPrepare()){
            modifyPwd();
        }
	}
}
