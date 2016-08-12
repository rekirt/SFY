package com.cchtw.sfy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.view.ContentView;
import com.cchtw.sfy.uitls.view.DrawlRoute;

//通过输入手势打开到首页
public class GestureLoginActivity extends BaseActivity {

	private FrameLayout body_layout;
	private ContentView content;
	// 手势密码
	private String is_regserect = "";
    private int errorTime;
    private TextView tv_forget;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_login);
		body_layout = (FrameLayout) findViewById(R.id.body_layout);
        String temp = AccountHelper.getUserFingerPwdTimes();
        errorTime =  Integer.parseInt(temp)-1;
        initView();
        setCanBack(false);
	}

	private void initView() {
		is_regserect = AccountHelper.getUserFingerPwd();
        tv_forget = (TextView) findViewById(R.id.tv_forget);
        tv_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GestureLoginActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        content = new ContentView(this, is_regserect, new DrawlRoute.GestureCallBack() {

            @Override
            public void checkedSuccess() {
                    Intent intent = new Intent(GestureLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    GestureLoginActivity.this.finish();
            }

            @Override
            public void checkedFail() {
                errorTime--;
                if (errorTime < 1) {
                    AccountHelper.setUser(null);
                    Intent intent = new Intent(GestureLoginActivity.this,LoginActivity.class);
                    startActivity(intent);
                    GestureLoginActivity.this.finish();
                }else {
                    AccountHelper.setUserFingerPwdTimes(errorTime);
                    ToastHelper.ShowToast("今天还允许输入错误" + errorTime + "次");
                }
            }

            @Override
            public void register() {
                initView();
            }
        });
        // 设置手势解锁显示到哪个布局里面
        content.setParentView(body_layout);
	}
}
