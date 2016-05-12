package com.example.shoufuyi.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.itech.utils.SequenceUtil;

import java.util.UUID;

/**
 * 启动页面
 */
public class SplashActivity extends Activity {

    private static final int sleepTime = 1500;
    final SharedPreferencesHelper sharedPreferencesHelper =SharedPreferencesHelper.getInstance();
    private AlphaAnimation start_anima;
    View view;
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//这只没有标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置启动页面全屏
        view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);
        //保存UUID
		if (TextUtils.isEmpty(getUUID())) {
            sharedPreferencesHelper.setString(Constant.UUID, "2" + SequenceUtil.globalSequenceFor32());
		} else {
            sharedPreferencesHelper.setString(Constant.UUID, "1" + getUUID());
        }
        start_anima = new AlphaAnimation(0.3f, 1.0f);
        start_anima.setDuration(sleepTime);
        if (view != null) {
            view.startAnimation(start_anima);
        }
        start_anima.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                redirectTo();
            }
        });


	}

    private void redirectTo(){
        String is_regserect = sharedPreferencesHelper.getString("mima","");

        String number = sharedPreferencesHelper.getString("number", "");
        // 如果退出字段是1就跳转到登录界面
        String is_exit = sharedPreferencesHelper.getString("Is_exit", "");
        // 激活状态
        String is_login = sharedPreferencesHelper.getString("jihuo", "");
        // 是否修改密码状态
        String is_gaimima = sharedPreferencesHelper.getString("gaimima", "");
        //如果帐号存在，表示已登录
        if (is_exit.equals("1") || number.equals("0")) {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (is_login.equals("")) {
                Intent intent = new Intent(SplashActivity.this,StartToUseActivity.class);
                startActivity(intent);
                finish();
            } else if (is_login.equals("0000") && is_gaimima.equals("")) {
                Intent intent = new Intent(SplashActivity.this,ChangePwdActivity.class);
                startActivity(intent);
                finish();
            } else if (is_login.equals("0000")
                    && is_gaimima.equals("0000")) {
                // 是否修改密码状态
                Intent intent = new Intent(SplashActivity.this, Gesture.class);
                startActivity(intent);
                finish();
            } else if (is_login.equals("0000") && is_gaimima.equals("1111")) {
                sharedPreferencesHelper.setString("gaimima", "0000");
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    /**
     * 获取手机的唯一标识码
     * @return uniqueId 唯一标识码
     */
	private String getUUID() {

        TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice, tmSerial, androidId;

		tmDevice = String.valueOf(telephonyManager.getDeviceId());

		tmSerial = String.valueOf(telephonyManager.getSimSerialNumber());

		androidId =String.valueOf(android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID));

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

		String uniqueId = deviceUuid.toString();
		uniqueId = uniqueId.replace("-", "");
		return uniqueId;
	}
}
