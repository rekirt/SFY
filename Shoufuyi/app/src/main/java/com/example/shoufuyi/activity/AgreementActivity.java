package com.example.shoufuyi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shoufuyi.R;
import com.itech.message.Result_120023;

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
        }
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

//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		String isSuccess = data.getStringExtra("isSuccess");// 得到新Activity//
//															// 关闭后返回的数据
//		String handWritingPath = data.getStringExtra("path");
//		if ("true".equals(isSuccess)) {
//			Intent intent = new Intent();
//			intent.putExtra("result", "0000");
//			intent.putExtra("path", handWritingPath);
//			intent.putExtra("isSuccess", true);
//			AgreementActivity.this.setResult(RESULT_OK, intent);
//			// 关闭Activity
//			AgreementActivity.this.finish();
//		} else {
//			Intent intent = new Intent();
//			intent.putExtra("path", handWritingPath);
//			intent.putExtra("result", "0001");
//			intent.putExtra("isSuccess", false);
//			AgreementActivity.this.setResult(RESULT_OK, intent);
//			// 关闭Activity
//			AgreementActivity.this.finish();
//		}
//
//	}

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

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			Intent intent = new Intent();
			intent.putExtra("result", "0001");

			AgreementActivity.this.setResult(RESULT_OK, intent);
			AgreementActivity.this.finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

}
