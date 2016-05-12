package com.example.shoufuyi.uitls.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.shoufuyi.R;
import com.example.shoufuyi.activity.BaseActivity;
import com.example.shoufuyi.activity.LoginActivity;
import com.example.shoufuyi.activity.MainActivity;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;

/**
 * 
 * @author huanghp
 * 
 *         字段解释： shareprefer中当"Is_exit"设置为 "1"的时候表示重新登录 "mima"当它有值的时候表示已经保存的手势密码
 *         Is_Login是用来判断有手势密码文字是显示“再次输入手势密码”还是“忘记手势密码”
 */
public class Gesture extends BaseActivity {

	private FrameLayout body_layout;
	private ContentView content;
	// 手势密码
	private String is_regserect = "";
	private TextView textshow;

	private SharedPreferences share;
	private SharedPreferences.Editor edit;
	// 允许输错手势密码的次数；
	private int number;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gesture);
		String strnum = (String) SharedPreferencesHelper.getString("number", "5");
		number = Integer.parseInt(strnum);
		body_layout = (FrameLayout) findViewById(R.id.body_layout);
		textshow = (TextView) findViewById(R.id.textshow);
		Initreg();

	}

	private void Initreg() {
		// 初始化一个显示各个点的viewGroup
		share = super.getSharedPreferences("FILENAME", Activity.MODE_PRIVATE);// 指定操作的文件名称
		is_regserect = share.getString("mima", "");
		String str_islogin = share.getString("Is_Login", "");
		if (str_islogin.equals("")) {
			if (is_regserect.equals("")) {
				textshow.setText("请设定手势");
			} else {
				textshow.setText("请再次输入手势");
			}
		} else {
			textshow.setText("忘记手势");
			textshow.setOnClickListener(textlis);
		}
		content = new ContentView(this, is_regserect, new Drawl.GestureCallBack() {

			@Override
			public void checkedSuccess() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(Gesture.this, MainActivity.class);
						intent.putExtra("ispicture", "1111");
						startActivity(intent);
						Gesture.this.finish();
					}
				}, 500);

			}

			@Override
			public void checkedFail() {
				share = Gesture.this.getSharedPreferences("FILENAME",
						Activity.MODE_PRIVATE);// 指定操作的文件名称
				String strnumber = share.getString("gesnumber", "");
				if (strnumber.equals("")) {
					number--;
				} else {
					number = Integer.parseInt(strnumber) - 1;
				}

				edit = share.edit();
				edit.putString("number", number + "");// 保存字符串
				edit.commit();
				if (number <= 0) {
					number = 0;
					edit.putString("Is_exit", "1");// 保存字符串
					edit.commit();// 提交更新
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(Gesture.this,
									LoginActivity.class);
							startActivity(intent);
							Gesture.this.finish();
						}
					}, 500);
				}
				ToastHelper.ShowToast("还允许输入" + number + "次");
			}

			@Override
			public void register() {
				Initreg();
			}
		});

		// 设置手势解锁显示到哪个布局里面
		content.setParentView(body_layout);
	}

	private OnClickListener textlis = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			edit = share.edit();
			edit.putString("Is_exit", "");// 保存字符串
			edit.commit();// 提交更新
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(Gesture.this,
							LoginActivity.class);
					startActivity(intent);
					Gesture.this.finish();
				}
			}, 500);

		}
	};

}
