package com.example.shoufuyi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
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
		deskey = sharedPreferencesHelper.getString(Constant.DESKEY, "");
		des3key = sharedPreferencesHelper.getString(Constant.DESK3KEY, "");
        userPhone = sharedPreferencesHelper.getString(Constant.PHONE, "");
		butsubmit.setOnClickListener(this);
		setCanBack(true);
	}

	private void modifyPwd(){
// 修改登录密码
        APP_120034 app = new APP_120034();
        app.setUserName(userPhone);
        try {
            DialogHelper.showProgressDialog(ChangePwdActivity.this,
                    "正在请求...", true, true);
            final String newPwd = mEditNew.getText().toString();
            app.setOriPass(des3key, mEditOld.getText().toString());
            app.setNewPass(des3key,newPwd);
            ApiRequest.requestData(app, userPhone, new JsonHttpHandler() {
                @Override
                public void onDo(JSONObject responseJsonObject) {
                    returnapp = JSON.parseObject(responseJsonObject.toString(), APP_120034.class);
                    SharedPreferencesHelper.setString("LoginSerect",newPwd);// 保存字符串
                    SharedPreferencesHelper.setString("gaimima", "0000");
                    SharedPreferencesHelper.setString("Is_exit", "1");
                    Intent intent = new Intent(ChangePwdActivity.this, LoginActivity.class);
                    startActivity(intent);
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
