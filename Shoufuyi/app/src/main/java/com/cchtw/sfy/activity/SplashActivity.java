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
import com.cchtw.sfy.uitls.AccountHelper;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.cache.ACache;
import com.itech.utils.SequenceUtil;

import java.util.UUID;

/**
 * 启动页面
 */
public class SplashActivity extends BaseActivity {

    private static final int sleepTime = 1000;
    private AlphaAnimation start_anima;
    View view;
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置启动页面全屏
        view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);
        //保存UUID
		if (TextUtils.isEmpty(getUUID())) {
            SharedPreferencesHelper.setString(Constant.UUID, "2" + SequenceUtil.globalSequenceFor32());
		} else {
            SharedPreferencesHelper.setString(Constant.UUID, "1" + getUUID());
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
        boolean is_login = AccountHelper.isLogin();
        boolean haveSetFingerPwd = AccountHelper.haveSetFingerPwd();
        ACache aCache = ACache.get(SplashActivity.this);
        byte[] gesturePassword= aCache.getAsBinary(AccountHelper.getUserName()+ Constant.GESTURE_PASSWORD);

        if (is_login && (gesturePassword != null)) {
            Intent intent = new Intent(SplashActivity.this, GestureLoginActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }else if (is_login) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
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
