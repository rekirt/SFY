package com.cchtw.sfy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;
import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;
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
    private int errorTime;
    private boolean isChangeFingerPwd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture);
        isChangeFingerPwd = getIntent().getBooleanExtra("isChangeFingerPwd",false);
		body_layout = (FrameLayout) findViewById(R.id.body_layout);
        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);
        String temp = SharedPreferencesHelper.getString(Constant.FINGERPASSWORDTIMES, "5");
        errorTime =  Integer.parseInt(temp)-1;
        oldFingerPwd = SharedPreferencesHelper.getString(Constant.MIMA, "");
        initView();
        setCanBack(true);
	}

    private String oldFingerPwd;
    private boolean haveSetFingerPwd;
	private void initView() {
		// 初始化一个显示各个点的viewGroup
		is_regserect = SharedPreferencesHelper.getString(Constant.MIMA, "");
        haveSetFingerPwd = SharedPreferencesHelper.getBoolean(Constant.HAVESETFINGERPWD, false);

		if (haveSetFingerPwd) {//如果已经设置了手势密码
            mTvShowMsg.setText("忘记手势密码");
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
                if (haveSetFingerPwd && isChangeFingerPwd){
                    SharedPreferencesHelper.setString(Constant.MIMA, "");
                    SharedPreferencesHelper.setBoolean(Constant.HAVESETFINGERPWD, false);
                    initView();
                    ToastHelper.ShowToast("请输入新的手势密码!");
                }else {
                    SharedPreferencesHelper.setBoolean(Constant.HAVESETFINGERPWD, true);
                    SharedPreferencesHelper.setString(Constant.MIMA, is_regserect);
                    Intent intent = new Intent(SetGestureActivity.this, MainActivity.class);
                    startActivity(intent);
                    SetGestureActivity.this.finish();
                }
            }

            @Override
            public void checkedFail() {
                errorTime--;
                if (errorTime < 1) {
                    SharedPreferencesHelper.setString("Is_exit", "1");
                    SharedPreferencesHelper.setBoolean(Constant.ISLOGIN,false);
                    Intent intent = new Intent(SetGestureActivity.this,ChangePwdActivity.class);
                    startActivity(intent);
                    SetGestureActivity.this.finish();
                }else {
                    if (haveSetFingerPwd){
                        SharedPreferencesHelper.setInt(Constant.GESNUMBER, errorTime);
                        ToastHelper.ShowToast("今天还允许输入错误" + errorTime + "次");
                    }else {
                        ToastHelper.ShowToast("两次手势不一致!");
                    }
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        SharedPreferencesHelper.setBoolean(Constant.ISLOGIN, false);// 保存字符串
        Intent intent = new Intent(SetGestureActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(oldFingerPwd)){
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {

                            SetGestureActivity.this.finish();
                        }
                    });
        }else {
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            SharedPreferencesHelper.setString(Constant.MIMA, oldFingerPwd);
                            SetGestureActivity.this.finish();
                        }
                    });
        }
    }

    @Override
    public void goBack(View view) {
        if (TextUtils.isEmpty(oldFingerPwd)){
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃设置手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {

                            SetGestureActivity.this.finish();
                        }
                    });
        }else {
            AlertDialogHelper.showAlertDialog(SetGestureActivity.this,
                    "提示：", "确定放弃修改手势密码?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            SharedPreferencesHelper.setString(Constant.MIMA, oldFingerPwd);
                            SetGestureActivity.this.finish();
                        }
                    });
        }
    }
}