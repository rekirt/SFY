package com.cchtw.sfy.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120028;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.utils.ZipDataUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AgreementActivity extends BaseActivity {
	private TextView textview;
	private Button butagree;
	private CheckBox checkbox;
	private TextView textname, textphone, textcard, textid;
    private LinearLayout ll_footbar;
	private Result_120023 mResult;
	// 是否已经同意协议了；
	private boolean isshow;
    private String mESignFileId;
	private ImageView iv_sign_name;
	String path;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_agreement);
		Bundle bundle = getIntent().getExtras();
		mResult = (Result_120023) bundle.get("info");
        mESignFileId =  bundle.getString("mESignFileId");
		init();
		setCanBack(true);
	}

	private void init() {
		textname = (TextView) findViewById(R.id.textname);
		textphone = (TextView) findViewById(R.id.textphone);
		textcard = (TextView) findViewById(R.id.textcard);
		textid = (TextView) findViewById(R.id.textid);
		textview = (TextView) findViewById(R.id.text);
        ll_footbar = (LinearLayout) findViewById(R.id.ll_footbar);
        textname.setText(textname.getText().toString()+ mResult.getAccountName());
		textphone.setText(textphone.getText().toString() + mResult.getMobile());
		textcard.setText(textcard.getText().toString() + mResult.getIdCard());
		textid.setText(textid.getText().toString() + mResult.getAccountNo());
        iv_sign_name = (ImageView) findViewById(R.id.iv_sign_name);

        String test = readStream(this.getResources()
				.openRawResource(R.raw.test));
		textview.setText(test);

        butagree = (Button) findViewById(R.id.butdeal);
		checkbox = (CheckBox) findViewById(R.id.check);

        butagree.setVisibility(View.VISIBLE);
		checkbox.setVisibility(View.VISIBLE);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton v, boolean arg1) {
				CheckBox cb = (CheckBox) findViewById(v.getId());
				if (cb.isChecked()) {
					butagree.setClickable(true);
					butagree.setOnClickListener(agreelis);
				} else {
					butagree.setClickable(false);
				}

			}
		});
        if (!TextUtils.isEmpty(mESignFileId)){
            ll_footbar.setVisibility(View.GONE);
            iv_sign_name.setVisibility(View.VISIBLE);
            downLoadFile(mESignFileId);
        }
	}

    private void downLoadFile(String fileId){
        APP_120028 app120028 = new APP_120028();
        app120028.setTrxCode("120028");
        app120028.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        FileMsg file = new FileMsg();
        file.setFileId(fileId);
        app120028.setFileMsg(file);
        DialogHelper.showProgressDialog(AgreementActivity.this, "正在加载...", true, false);
        ApiRequest.requestData(app120028, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler(AgreementActivity.this) {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120028 result = JSON.parseObject(responseJsonObject.toString(), APP_120028.class);
                FileMsg fileMsg = result.getFileMsg();
                String contentString = "";
                if (fileMsg == null) {
                    return;
                }
                contentString = result.getFileMsg().getContent();
                byte[] content = contentString.getBytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(ZipDataUtils.unZipForBase64(content),
                        0, ZipDataUtils.unZipForBase64(content).length);
                iv_sign_name.setImageBitmap(bitmap);
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

	private OnClickListener agreelis = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("result", mResult);
            intent.putExtras(bundle);
            intent.putExtra("obj", path);
            intent.setClass(AgreementActivity.this, HandwritingActivity.class);
            startActivity(intent);
            AgreementActivity.this.finish();
			}
	};

	private String readStream(InputStream is) {
		String res;
		try {
			byte[] buf = new byte[is.available()];
			is.read(buf);
			res = new String(buf, "GBK");
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			res = "";
		}
		return (res);
	}
}
