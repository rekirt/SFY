package com.example.shoufuyi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.wintone.bankcard.BankCardRecogUtils;
import com.wintone.view.BankCardEditTextWatcher;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ShowResultActivity extends BaseActivity {
	private static final int resultBitmapOfW = 400;
	private static final int resultBitmapOfH = 80;

	private Button ok_show;
	private ImageView word_show;
	private ImageView showbitmap;
	private EditText num1_show;
	private EditText num2_show;
	private EditText num3_show;
	private EditText num4_show;
	private EditText num5_show;
	private TextView surplusTimes;
	private LinearLayout lin_edit;
	private int editTextSize;
	private String disResult = "";
	private int resultBitmapArray[];
	private String bitmapPath;
	private int[] bitmapCut = new int[4];
	Bitmap bitmap = null;
	private TextView bank_name_view;
	private TextView bank_code_view;
	private TextView card_name_view;
	private TextView card_type_view;
	private int width;
	private int height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 竖屏
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//
		// 横屏
		setContentView(R.layout.activity_show_result);
		Intent intent = getIntent();
		resultBitmapArray = intent.getIntArrayExtra("PicR");
		char results[] = intent.getCharArrayExtra("StringR");
		int success = intent.getIntExtra("Success", 0);

		findView(success);

		if (success == 2) {
			if (results != null) {
				String resultS = String.valueOf(results);
				String[] temp = null;
//				temp = resultS.split(" ");
				temp = resultS.split("[^0-9]");
				HiddenView(temp.length, temp);
				setBankInfo();
				buttonLayoutSetup(width, card_type_view);
			}
			if (resultBitmapArray != null) {
				Bitmap ResultBitmap = Bitmap.createBitmap(resultBitmapArray,
						resultBitmapOfW, resultBitmapOfH, Config.ARGB_8888);
				showbitmap.setImageBitmap(ResultBitmap);
			}
		} else if (success == 3) {
			bitmapPath = intent.getStringExtra("Path");
			int l = intent.getIntExtra("l", -1);
			int t = intent.getIntExtra("t", -1);
			int w = intent.getIntExtra("w", -1);
			int h = intent.getIntExtra("h", -1);
			bitmapCut[0] = l;
			bitmapCut[1] = t;
			bitmapCut[2] = w;
			bitmapCut[3] = h;

			try {
				bitmap = BitmapFactory.decodeFile(bitmapPath);
				bitmap = Bitmap.createBitmap(bitmap, l, t, w, h);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (bitmap != null) {
				showbitmap.setImageBitmap(bitmap);
			}
			buttonLayoutSetup(width, height);
		}
        setCanBack(true);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
				|| event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			setBankInfo();
			buttonLayoutSetup(width, card_type_view);
			lin_edit.clearFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(lin_edit.getWindowToken(), 0);
		}

		return super.dispatchKeyEvent(event);
	}

	private void setBankInfo() {
		StringBuffer sb = new StringBuffer(num1_show.getText() + ""
				+ num2_show.getText() + "" + num3_show.getText() + ""
				+ num4_show.getText() + "" + num5_show.getText());
		String temp = null;
		String bankName = null;
		String bankCode = null;
		String cardName = null;
		String cardType = null;
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == ' ') {
				sb.deleteCharAt(i);
			}
			temp = sb.toString();
		}
		if (temp != null) {
			if (temp != null && !temp.equals("")) {
				String[] results = new BankCardRecogUtils(this).getBankInfo(Constant.devcode, temp);
				if (results[0].equals(" ")) {
					bankName = getResources().getString(
							R.string.unknow_card_type);
					LayoutParams layoutParams = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.BELOW, R.id.lin_edit);
					layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
							RelativeLayout.TRUE);
					bank_name_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							(int) (editTextSize * 0.65));
					bank_name_view.setLayoutParams(layoutParams);
				} else if (results[0].equals("-1")) {
					Toast.makeText(ShowResultActivity.this, results[1],
							Toast.LENGTH_LONG).show();
				} else {
					bankName = getResources().getString(R.string.bank_name)
							+ " " + results[0];
					bankCode = getResources().getString(R.string.bank_code)
							+ " " + results[2];
					cardName = getResources().getString(R.string.card_name)
							+ " " + results[1];
					cardType = getResources().getString(R.string.card_type)
							+ " " + results[3];
				}
				Log.i("TAG", Arrays.toString(results));
				bank_code_view.setText(bankCode);
				bank_name_view.setText(bankName);
				card_name_view.setText(cardName);
				card_type_view.setText(cardType);
			}
		} else {
			bank_name_view.setText("");
			bank_code_view.setText("");
			card_name_view.setText("");
			card_type_view.setText("");
		}
	}

	private void findView(final int success) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;
		height = displayMetrics.heightPixels;
		boolean isFatty = false;
		if (height * 3 == width * 4) {
			isFatty = true;
		}

		word_show = (ImageView) findViewById(R.id.word_show);
		showbitmap = (ImageView) findViewById(R.id.showbitmap);
		num1_show = (EditText) findViewById(R.id.num1_show);
		BankCardEditTextWatcher myNum1_show = new BankCardEditTextWatcher(
				num1_show);
		num1_show.addTextChangedListener(myNum1_show);
		num2_show = (EditText) findViewById(R.id.num2_show);
		num3_show = (EditText) findViewById(R.id.num3_show);
		num4_show = (EditText) findViewById(R.id.num4_show);
		num5_show = (EditText) findViewById(R.id.num5_show);
		lin_edit = (LinearLayout) findViewById(R.id.lin_edit);
		surplusTimes = (TextView) findViewById(R.id.surplusTimes);
		bank_name_view = (TextView) findViewById(R.id.bank_name_view);
		bank_code_view = (TextView) findViewById(R.id.bank_code_view);
		card_name_view = (TextView) findViewById(R.id.card_name_view);
		card_type_view = (TextView) findViewById(R.id.card_type_view);
		ok_show = (Button) findViewById(R.id.OK_show);

		int back_w = (int) (height * 0.066796875);
		int back_h = (int) (back_w * 1);
		LayoutParams layoutParams = new LayoutParams(
				back_w, back_h);
		editTextSize = back_h / 2;

		if (success == 2) {
			int word_show_w = (int) (height * 0.299609375);
			int word_show_h = (int) (word_show_w * 0.08213820078226857887874837027379);
			layoutParams = new LayoutParams(word_show_w,
					word_show_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			layoutParams.topMargin = (int) (height * 0.1921875);
			layoutParams.leftMargin = (int) (height * 0.047265625);
			word_show.setLayoutParams(layoutParams);
		} else if (success == 3) {
			word_show.setBackgroundResource(R.drawable.please_word);
			int word_show_w = (int) (height * 0.17421875);
			int word_show_h = (int) (word_show_w * 0.14125560538116591928251121076233);
			layoutParams = new LayoutParams(word_show_w,
					word_show_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			layoutParams.topMargin = (int) (height * 0.1921875);
			layoutParams.leftMargin = (int) (height * 0.047265625);
			word_show.setLayoutParams(layoutParams);
			num1_show.setBackgroundResource(R.drawable.edt_bg);
			num1_show.setTextColor(Color.BLACK);
		}
		if (success == 2) {

			int showbitmap_w = (int) (width * 1);
			int showbitmap_h = (int) (showbitmap_w * 0.2);
			layoutParams = new LayoutParams(showbitmap_w,
					showbitmap_h);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			layoutParams.topMargin = (int) (height * 0.23828125);
			showbitmap.setLayoutParams(layoutParams);
		} else if (success == 3) {

			int showbitmap_w = (int) (width * 0.83888888888888888888888888888889);
			int showbitmap_h = (int) (showbitmap_w / 1.58577);
			layoutParams = new LayoutParams(showbitmap_w,
					showbitmap_h);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			layoutParams.topMargin = (int) (height * 0.23984375);
			showbitmap.setLayoutParams(layoutParams);
		}
		if (success == 2) {
			int lin_edit_w = (int) (width * 1);
			layoutParams = new LayoutParams(lin_edit_w,
					LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			if (isFatty)
				layoutParams.topMargin = (int) (height * 0.4875);
			else
				layoutParams.topMargin = (int) (height * 0.3875);
			lin_edit.setLayoutParams(layoutParams);

		} else if (success == 3) {
			int lin_edit_w = (int) (width * 0.83888888888888888888888888888889);
			layoutParams = new LayoutParams(lin_edit_w,
					LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.BELOW, R.id.showbitmap);
			layoutParams.topMargin = (int) (height * 0.0234375);
			lin_edit.setLayoutParams(layoutParams);

			num2_show.setVisibility(View.GONE);
			num3_show.setVisibility(View.GONE);
			num4_show.setVisibility(View.GONE);
			num5_show.setVisibility(View.GONE);
			int num1_show_w = (int) (width * 0.83888888888888888888888888888889);
			LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
					num1_show_w, LayoutParams.WRAP_CONTENT);
			num1_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							23) });
			num1_show.setGravity(Gravity.CENTER);
			num1_show.setKeyListener(new DigitsKeyListener(false, true));
			num1_show.setHint(R.string.card_num_hint);
			num1_show.setLayoutParams(layoutParam);
		}
		layoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.OK_show);
		layoutParams.topMargin = editTextSize / 3;
		surplusTimes.setLayoutParams(layoutParams);
		surplusTimes.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				editTextSize * 2 / 3);
		layoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.lin_edit);
		layoutParams.leftMargin = (int) (width * 0.3);
		bank_name_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				(int) (editTextSize * 0.65));
		bank_name_view.setLayoutParams(layoutParams);
		layoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.bank_name_view);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.bank_name_view);
		bank_code_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				(int) (editTextSize * 0.65));
		bank_code_view.setLayoutParams(layoutParams);
		layoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.bank_name_view);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.bank_code_view);
		card_name_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				(int) (editTextSize * 0.65));
		card_name_view.setLayoutParams(layoutParams);
		layoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.bank_name_view);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.card_name_view);
		card_type_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				(int) (editTextSize * 0.65));
		card_type_view.setLayoutParams(layoutParams);

		ok_show.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}

				StringBuffer sb = new StringBuffer(num1_show.getText() + " "
						+ num2_show.getText() + " " + num3_show.getText() + " "
						+ num4_show.getText().toString());

                boolean isNumber = isNumeric(num5_show.getText().toString());
                if (isNumber){
                    sb = sb.append(num5_show.getText());
                }
//                String[] result = sb.toString().split("[^0-9]");
				SharedPreferencesHelper.setString(Constant.BANKCRADNUMBER,sb.toString());
				ShowResultActivity.this.finish();
			}
		});

	}

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
    private void buttonLayoutSetup(int width, int height) {
		LayoutParams layoutParams;
		int ok_show_w = (int) (width * 0.81041666666666666666666666666667);
		int ok_show_h = (int) (ok_show_w * 0.1533847472150814053127677806341);
		layoutParams = new LayoutParams(ok_show_w, ok_show_h);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.lin_edit);
		layoutParams.topMargin = (int) (height * 0.09375);
		ok_show.setLayoutParams(layoutParams);
	}

	private void buttonLayoutSetup(int width, View view) {
		LayoutParams layoutParams;
		int ok_show_w = (int) (width * 0.81041666666666666666666666666667);
		int ok_show_h = (int) (ok_show_w * 0.1533847472150814053127677806341);
		layoutParams = new LayoutParams(ok_show_w, ok_show_h);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, view.getId());
		ok_show.setLayoutParams(layoutParams);
	}

	private void HiddenView(int num, String s[]) {
		switch (num) {
		case 1:
			String $s = s[0];
			char[] ch = $s.toCharArray();
			char n = ' ';
			char[] $ch = { ch[0], ch[1], ch[2], ch[3], n, ch[4], ch[5], ch[6],
					ch[7], n, ch[8], ch[9], ch[10], ch[11], n, ch[12], ch[13],
					ch[14], ch[15], n, ch[16], ch[17], ch[18] };
			String resultS = String.valueOf($ch);
			String[] temp = null;
			temp = resultS.split(" ");
			HiddenView(temp.length, temp);
			break;
		case 2:
			num1_show.setText(s[0]);
			num2_show.setText(s[1]);
			num1_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num2_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num1_show.setTypeface(Typeface.DEFAULT_BOLD);
			num2_show.setTypeface(Typeface.DEFAULT_BOLD);
			num1_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[0].length()) });
			num2_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[1].length()) });
			num1_show.setTextColor(Color.BLACK);
			num2_show.setTextColor(Color.BLACK);
			num1_show.setBackgroundResource(R.drawable.edt_bg);
			num2_show.setBackgroundResource(R.drawable.edt_bg);
			num3_show.setVisibility(View.GONE);
			num4_show.setVisibility(View.GONE);
			num5_show.setVisibility(View.GONE);
			break;
		case 3:
			num1_show.setText(s[0]);
			num2_show.setText(s[1]);
			num3_show.setText(s[2]);
			num1_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num2_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num3_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num1_show.setTypeface(Typeface.DEFAULT_BOLD);
			num2_show.setTypeface(Typeface.DEFAULT_BOLD);
			num3_show.setTypeface(Typeface.DEFAULT_BOLD);
			num1_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[0].length()) });
			num2_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[1].length()) });
			num3_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[2].length()) });
			num1_show.setTextColor(Color.BLACK);
			num2_show.setTextColor(Color.BLACK);
			num3_show.setTextColor(Color.BLACK);
			num1_show.setBackgroundResource(R.drawable.edt_bg);
			num2_show.setBackgroundResource(R.drawable.edt_bg);
			num3_show.setBackgroundResource(R.drawable.edt_bg);
			num4_show.setVisibility(View.GONE);
			num5_show.setVisibility(View.GONE);
			break;
		case 4:
			num1_show.setText(s[0]);
			num2_show.setText(s[1]);
			num3_show.setText(s[2]);
			num4_show.setText(s[3]);
			num1_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num2_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num3_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num4_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num1_show.setTypeface(Typeface.DEFAULT_BOLD);
			num2_show.setTypeface(Typeface.DEFAULT_BOLD);
			num3_show.setTypeface(Typeface.DEFAULT_BOLD);
			num4_show.setTypeface(Typeface.DEFAULT_BOLD);
			num1_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[0].length()) });
			num2_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[1].length()) });
			num3_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[2].length()) });
			num4_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[3].length()) });
			num1_show.setTextColor(Color.BLACK);
			num2_show.setTextColor(Color.BLACK);
			num3_show.setTextColor(Color.BLACK);
			num4_show.setTextColor(Color.BLACK);
			num1_show.setBackgroundResource(R.drawable.edt_bg);
			num2_show.setBackgroundResource(R.drawable.edt_bg);
			num3_show.setBackgroundResource(R.drawable.edt_bg);
			num4_show.setBackgroundResource(R.drawable.edt_bg);
			num5_show.setVisibility(View.GONE);
			break;
		default:
			num1_show.setText(s[0]);
			num2_show.setText(s[1]);
			num3_show.setText(s[2]);
			num4_show.setText(s[3]);
			num5_show.setText(s[4]);
			num1_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num2_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num3_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num4_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num5_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize);
			num1_show.setTypeface(Typeface.DEFAULT_BOLD);
			num2_show.setTypeface(Typeface.DEFAULT_BOLD);
			num3_show.setTypeface(Typeface.DEFAULT_BOLD);
			num4_show.setTypeface(Typeface.DEFAULT_BOLD);
			num5_show.setTypeface(Typeface.DEFAULT_BOLD);
			num1_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[0].length()) });
			num2_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[1].length()) });
			num3_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[2].length()) });
			num4_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[3].length()) });
			num5_show
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							s[4].length()) });
			num1_show.setTextColor(Color.BLACK);
			num2_show.setTextColor(Color.BLACK);
			num3_show.setTextColor(Color.BLACK);
			num4_show.setTextColor(Color.BLACK);
			num5_show.setTextColor(Color.BLACK);
			num1_show.setBackgroundResource(R.drawable.edt_bg);
			num2_show.setBackgroundResource(R.drawable.edt_bg);
			num3_show.setBackgroundResource(R.drawable.edt_bg);
			num4_show.setBackgroundResource(R.drawable.edt_bg);
			num5_show.setBackgroundResource(R.drawable.edt_bg);
			break;
		}
		for (int i = 0; i < s.length; i++) {
			disResult += s[i];
		}
	}
}
