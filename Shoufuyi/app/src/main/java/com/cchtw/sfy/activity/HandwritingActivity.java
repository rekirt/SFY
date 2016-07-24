package com.cchtw.sfy.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.cchtw.sfy.uitls.view.handwriting.DialogListener;
import com.cchtw.sfy.uitls.view.handwriting.WritePadDialog;
import com.itech.message.APP_120008;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.utils.HashCodeUtils;
import com.itech.utils.ZipDataUtils;
import com.itech.utils.encoder.Base64Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HandwritingActivity extends BaseActivity {
	private Bitmap mSignBitmap;
	private String signPath;
	private ImageView ivSign;
	private TextView tvSign;
	private Button but_submit;
	private Result_120023 result;
	boolean issuccess = false;
	String path="";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_hand_writing);
		Intent intent = this.getIntent();
        result = (Result_120023) intent.getSerializableExtra("result");
		path = intent.getStringExtra("obj");
        initView();
        initData();
		if (!TextUtils.isEmpty(path)) {
			signPath=path;
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			ivSign.setImageBitmap(bitmap);
			tvSign.setVisibility(View.GONE);
			but_submit.setVisibility(View.VISIBLE);
			but_submit.setOnClickListener(this);
		}
		setCanBack(true);
	}

    private void initView(){
        ivSign = (ImageView) findViewById(R.id.iv_sign);
        tvSign = (TextView) findViewById(R.id.tv_sign);
        but_submit = (Button) findViewById(R.id.submit);
    }

    private void initData(){
        ivSign.setOnClickListener(this);
        tvSign.setOnClickListener(this);
        but_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.iv_sign:
            case R.id.tv_sign:
                sign();
                break;
            case R.id.submit:
                upLoadImage();
                break;
            default:
                break;
        }
    }

    private void sign(){
        WritePadDialog writeTabletDialog = new WritePadDialog(
                HandwritingActivity.this, new DialogListener() {
            @Override
            public void refreshActivity(Object object) {
                mSignBitmap = (Bitmap) object;
                signPath = createFile();
							/*
							 * BitmapFactory.Options options = new
							 * BitmapFactory.Options(); options.inSampleSize =
							 * 15; options.inTempStorage = new byte[5 * 1024];
							 * Bitmap zoombm =
							 * BitmapFactory.decodeFile(signPath, options);
							 */
                ivSign.setImageBitmap(mSignBitmap);
                tvSign.setVisibility(View.GONE);
                but_submit.setVisibility(View.VISIBLE);
                saveBitmap(mSignBitmap);
            }
        });
        writeTabletDialog.show();
    }


    private void upLoadImage() {
        UpLoadImageTask downloadTask = new UpLoadImageTask(HandwritingActivity.this);
        downloadTask.execute();
    }

    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {

        public UpLoadImageTask(Context context) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showProgressDialog(HandwritingActivity.this, "正在上传...", true, false);
        }

        @Override
        protected APP_120008 doInBackground(APP_120008...APP_120008) {
            final APP_120008 app = new APP_120008();
            app.setTrxCode("120008");
            app.setAccountNo(result.getAccountNo());
            app.setIdCard(result.getIdCard());
            app.setMerchantId(result.getMerchantId());
            app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
            app.setVerifyItem("E_SIGN");
            List<FileMsg> list = new ArrayList<FileMsg>();
            FileMsg fileMsgFont = new FileMsg();
            try {
                contentBase64 = new String(ZipDataUtils
                        .zipForBase64(Base64Utils.fileToByte(signPath)));
                fileMsgFont.setContent(contentBase64);
            } catch (Exception e) {
                e.printStackTrace();
            }
            fileMsgFont.setFileName("电子签名.jpg");
            fileMsgFont.setIndex("0");
            fileMsgFont.setAttachSecurCode(HashCodeUtils.hashCodeVaule(
                    contentBase64.hashCode(),
                    SharedPreferencesHelper.getString(Constant.UUID, "")));
            list.add(fileMsgFont);
            app.setFileList(list);
            return app;
        }

        @Override
        protected void onPostExecute(APP_120008 attachPost) {
            UpLoadAttach(attachPost);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(APP_120008 s) {
            super.onCancelled(s);
        }
    }


    private String contentBase64 = "";
	private void UpLoadAttach(APP_120008 app) {
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120008 result = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);
                if ("0000".equals(result.getDetailCode())) {
                    ToastHelper.ShowToast("上传成功");
                    HandwritingActivity.this.finish();
                } else {
                    ToastHelper.ShowToast(result.getDetailInfo());
                }
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
	}

	/** 保存方法 */
	public void saveBitmap(Bitmap bm) {
		File fileFolder = new File(Environment.getExternalStorageDirectory()
				+ "/SFY/PIC/");
		// if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
		fileFolder.mkdir();
		// }
		File jpgFile = new File(fileFolder,
				getTimeName(System.currentTimeMillis()) + ".png");

		path = jpgFile.getAbsolutePath();

		if (jpgFile.exists()) {
			jpgFile.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(jpgFile);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建手写签名文件
	 * 
	 * @return
	 */
	String _path = null;

	private String createFile() {
		ByteArrayOutputStream baos = null;
		try {
			String sign_dir = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator;
			_path = sign_dir+"SFY/PIC" + getTimeName(System.currentTimeMillis()) + ".jpg";
			baos = new ByteArrayOutputStream();
			mSignBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] photoBytes = baos.toByteArray();
			if (photoBytes != null) {
				new FileOutputStream(new File(_path)).write(photoBytes);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _path;
	}

	public static String getTimeName(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(time);
		return formatter.format(date);
	}
}