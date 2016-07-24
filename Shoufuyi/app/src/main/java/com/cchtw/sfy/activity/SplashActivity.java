package com.cchtw.sfy.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.cchtw.sfy.R;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.itech.utils.SequenceUtil;

import java.util.UUID;

/**
 * 启动页面
 */
public class SplashActivity extends BaseActivity {

    private static final int sleepTime = 1000;
    final SharedPreferencesHelper sharedPreferencesHelper =SharedPreferencesHelper.getInstance();
    private AlphaAnimation start_anima;
    View view;
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置启动页面全屏
        view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);
        //保存UUID
		if (TextUtils.isEmpty(getUUID())) {
            sharedPreferencesHelper.setString(Constant.UUID, "2" + SequenceUtil.globalSequenceFor32());
		} else {
            sharedPreferencesHelper.setString(Constant.UUID, "1" + getUUID());
        }

	}

    @Override
    protected void onResume() {
        super.onResume();
        start_anima = new AlphaAnimation(0.3f, 1.0f);
        start_anima.setDuration(sleepTime);
        if (view != null) {
            view.startAnimation(start_anima);
        }
        start_anima.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                redirectTo();
            }
        });
    }

    private void redirectTo(){

        //-----为了测试先模拟已启用---------------
        if (Constant.ISTEST){
            SharedPreferencesHelper.setBoolean(Constant.ACTIVATION, true);// 记录登录成功状态
            SharedPreferencesHelper.setString(Constant.PHONE, "13430990001");// 保存字符串
            SharedPreferencesHelper.setString(Constant.DESKEY, "AN9WB25JUNAOYBABG9ROPRXM");
            SharedPreferencesHelper.setString(Constant.DESK3KEY, "OVESGD8LE8WT7V8RXZAPII4E");
            SharedPreferencesHelper.setString(Constant.MERCHANT, "001053110000001");
            SharedPreferencesHelper.setString(Constant.TOKEN, "6330130f48d840bdb0bd18b2dedf141b");
            SharedPreferencesHelper.setBoolean(Constant.ISLOGIN, true);// 保存字符串

            SharedPreferencesHelper.setString("LoginSerect","123456");// 保存字符串
            SharedPreferencesHelper.setBoolean(Constant.GAIMIMA, true);
            SharedPreferencesHelper.setString("Is_exit", "1");
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
            //-----为了测试先模拟已启用---------------
        }else{
            String number = sharedPreferencesHelper.getString(Constant.NUMBER, "");
            // 如果退出字段是1就跳转到登录界面
            boolean haveChangePwd = sharedPreferencesHelper.getBoolean(Constant.GAIMIMA, false);
            //
            boolean is_login = sharedPreferencesHelper.getBoolean(Constant.ISLOGIN, false);
            // 激活状态
            boolean isActive  = sharedPreferencesHelper.getBoolean(Constant.ACTIVATION, false);
            //如果帐号存在，表示已登录
            if (is_login) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (isActive && haveChangePwd) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }else if (isActive && !haveChangePwd) {
                Intent intent = new Intent(SplashActivity.this, ChangePwdActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
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
