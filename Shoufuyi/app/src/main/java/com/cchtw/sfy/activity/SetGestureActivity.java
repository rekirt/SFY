package com.cchtw.sfy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.view.ContentView;
import com.cchtw.sfy.uitls.view.DrawlRoute;

/**
 * 
 * @author huanghp
 *字段解释： shareprefer中当"Is_exit"设置为 "1"的时候表示重新登录 "mima"当它有值的时候表示已经保存的手势密码
 *         Is_Login是用来判断有手势密码文字是显示“再次输入手势密码”还是“忘记手势密码”
 */
public class SetGestureActivity extends BaseActivity {

	private FrameLayout body_layout;
	private ContentView content;
	// 手势密码
	private String is_regserect = "";
	private TextView mTvShowMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture);

		body_layout = (FrameLayout) findViewById(R.id.body_layout);
        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);

        initView();
        initData();
	}

	private void initView() {
		// 初始化一个显示各个点的viewGroup
		is_regserect = SharedPreferencesHelper.getString(Constant.MIMA, "");
//		boolean islogin = SharedPreferencesHelper.getBoolean(Constant.ISLOGIN, false);
		boolean haveSetFingerPwd = SharedPreferencesHelper.getBoolean(Constant.HAVESETFINGERPWD, false);

		if (haveSetFingerPwd) {//如果已经设置了手势密码
			if (is_regserect.equals("")) {
				mTvShowMsg.setText("请输入原来手势密码");
			} else {
                mTvShowMsg.setText("请再次输入手势");
			}
			mTvShowMsg.setText("忘记手势");
			mTvShowMsg.setOnClickListener(this);
		} else {//如果是没有设置过手势密码
			if (is_regserect.equals("")) {
				mTvShowMsg.setText("请输入手势密码");
			} else {
				mTvShowMsg.setText("请再次输入手势密码");
			}
		}

		content = new ContentView(this, is_regserect, new DrawlRoute.GestureCallBack() {

			@Override
			public void checkedSuccess() {
				SharedPreferencesHelper.setBoolean(Constant.HAVESETFINGERPWD, true);
				Intent intent = new Intent(SetGestureActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
			}

			@Override
			public void checkedFail() {
				int errorTime = SharedPreferencesHelper.getInt(Constant.FINGERPASSWORDTIMES, 5);
                int number = errorTime - 1;
				if (number < 1) {
                    SharedPreferencesHelper.setString("Is_exit", "1");
                    SharedPreferencesHelper.setBoolean(Constant.ISLOGIN,false);
                    Intent intent = new Intent(SetGestureActivity.this,ChangePwdActivity.class);
                    startActivity(intent);
                    finish();
				}else {
                    SharedPreferencesHelper.setInt(Constant.GESNUMBER, number);
                    ToastHelper.ShowToast("今天还允许输入错误" + number + "次");
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

    private void initData(){
        setCanBack(true);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        SharedPreferencesHelper.setBoolean(Constant.ISLOGIN, false);// 保存字符串
        Intent intent = new Intent(SetGestureActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
