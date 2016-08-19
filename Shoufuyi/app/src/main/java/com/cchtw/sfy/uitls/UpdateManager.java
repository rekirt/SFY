package com.cchtw.sfy.uitls;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ProgressBar;

import com.itech.message.APP_Version;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateManager {

	private Context mContext;

	// 提示语
	private String updateMsg = "有最新的软件包哦，亲快下载吧~";

	// 返回的安装包url
	private String apkUrl;

	// 访问服务器上的app版本
	private String versionurl;

	private Dialog noticeDialog;

	private Dialog downloadDialog;
	/* 下载包安装路径 */
	private static final String savePath = "/sdcard/updatedemo/";

	private static String saveFileName = savePath + "ecard.apk";

	/* 进度条与通知ui刷新的handler和msg常量 */
	private ProgressBar mProgress;

	private static final int DOWN_UPDATE = 1;

	private static final int DOWN_OVER = 2;

	private static final int FILENOFOUND = 3;

	private int progress;

	private Thread downLoadThread;

	private boolean interceptFlag = false;

	private String baseURL;

	ProgressDialog proDia;

	private boolean isupdate;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				proDia.setProgress(progress);
				break;
			case DOWN_OVER:
				if (proDia != null)
					proDia.dismiss();
				installApk();
				break;
			case FILENOFOUND:
				if(downloadDialog!=null)
				downloadDialog.dismiss();
				ToastHelper.ShowToast("服务器没有对应apk文件", mContext);
				break;
			default:
				break;
			}
		};
	};


//	static class MyHandler extends Handler{
//		private WeakReference<UpdateManager> outer;
//
//		public MyHandler(WeakReference<UpdateManager> outer) {
//			this.outer = outer;
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//
//		}
//	}

	public UpdateManager(Context context) {
		this.mContext = context;

	}

	// 外部接口让主Activity调用
	public void checkUpdateInfo(APP_Version version) {
		showNoticeDialog(version);
	}

	@SuppressWarnings("unused")
	private void showResponseResult(HttpResponse response) {
		if (null == response) {
			return;
		}

		HttpEntity httpEntity = response.getEntity();
		try {
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String result = "";
			String line = "";
			while (null != (line = reader.readLine())) {
				result += line;

			}

			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void showNoticeDialog(APP_Version version) {
		String oldCode = "";
		String newCode = "";
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			oldCode = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// getPackageName()是你当前类的包名，0代表是获取版本信息

		newCode = version.getVersion();
		if (isUpdate(oldCode, newCode)) {
			apkUrl = version.getDownPath();
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setTitle("软件版本更新");
			if (version.getDescribe() != null)
				builder.setMessage(updateMsg + "版本说明：" + version.getDescribe());
			builder.setPositiveButton("下载", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showDownloadDialog();
				}
			});
			if (!version.isForceUpgrade()) {
				builder.setNegativeButton("以后再说", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
			isupdate = version.isForceUpgrade();
			builder.setCancelable(false);

			noticeDialog = builder.create();
			noticeDialog.show();
		} else {
			ToastHelper.ShowToast(version.getErrMsg());
		}
		Looper.loop();

	}

	/**
	 *
	 * @param oldVersion 旧版本
	 * @param newVewsion 新版本
	 * @return
	 */
	public boolean isUpdate(String oldVersion, String newVewsion) {
		int oldV = Integer.parseInt(oldVersion.replace(".", ""));
		int newV = Integer.parseInt(newVewsion.replace(".", ""));
		if (newV <= oldV)
			return false;
		else
			return true;
	}

	@SuppressWarnings("deprecation")
	private void showDownloadDialog() {
		if (proDia == null) {
			proDia = new ProgressDialog(mContext);
			proDia.setTitle("新版本正在下载");
			proDia.setMessage("请耐心等待");
			proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 水平进度条
			proDia.setMax(100); // 设置进度的最大值
			proDia.setProgress(0); // 从进度30开始

			if (!isupdate) {
				proDia.setButton("后台处理", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        proDia.dismiss(); // 关闭对话框
                    }
                });
			}

			proDia.onStart(); // 启动进度

			proDia.show(); // 显示对话框
			proDia.setCancelable(false);
			downloadApk();
		} else {
			proDia.show(); // 显示对话框
		}
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Looper.prepare();
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdir();
				}
				String fileName = apkUrl.substring(apkUrl.lastIndexOf("/"));
				saveFileName = savePath + fileName;
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);
				int count = 0;
				byte buf[] = new byte[1024];

				do {

					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					// 更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);

					if (numread <= 0) {
						// 下载完成通知安装
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// 点击取消就停止下载.
				fos.close();
				is.close();
				Looper.loop();
			} catch (FileNotFoundException e) {
				mHandler.sendEmptyMessage(FILENOFOUND);
                proDia.dismiss(); // 关闭对话框
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};

	/**
	 * 下载apk
	 * 
	 */

	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装apk
	 * 
	 */
	private void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);

	}
}
