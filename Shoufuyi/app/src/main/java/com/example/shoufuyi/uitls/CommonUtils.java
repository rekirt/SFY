package com.example.shoufuyi.uitls;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.InputType;
import android.text.method.NumberKeyListener;

//import org.apache.http.HttpException;
//import org.apache.http.client.HttpClient;

/**
 * 
 * @author huang 避免按钮多次点击进行多次的跳转
 * 
 */
public class CommonUtils {

	private static long lastClickTime;

	public static boolean isFastDoubleClick() {

		long time = System.currentTimeMillis();

		long timeD = time - lastClickTime;

		if (0 < timeD && timeD < 2000) {

			return false;

		}

		lastClickTime = time;

		return true;

	}

	/**
	 * 返回码处理
	 * 
	 * @return
	 */
	public static boolean Msgpattern(String msg) {
		if (msg.equals("0000")) {
			return true;
		} else
			return false;

	}

	public static NumberKeyListener getKeylistener(final char[] mychar) {
		NumberKeyListener l = new NumberKeyListener() {
			@Override
			public int getInputType() {
				// TODO Auto-generated method stub
				return InputType.TYPE_CLASS_NUMBER;
			}

			@Override
			protected char[] getAcceptedChars() {
				// TODO Auto-generated method stub
				return mychar;
			}

		};
		return l;
	}

	/**
	 * 判断当前是否使用的是 WIFI网络
	 * 
	 */
	public static boolean isWifiActive(Context icontext) {
		Context context = icontext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info;
		if (connectivity != null) {
			info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 检查当前网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetworkAvailable(Activity activity) {
		Context context = activity.getApplicationContext();
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					System.out.println(i + "===状态==="
							+ networkInfo[i].getState());
					System.out.println(i + "===类型==="
							+ networkInfo[i].getTypeName());
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
