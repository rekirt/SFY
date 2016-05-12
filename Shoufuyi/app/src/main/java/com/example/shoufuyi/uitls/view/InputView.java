package com.example.shoufuyi.uitls.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shoufuyi.R;

/**
 * @author:caixh
 * @date:2014-10-8
 */
@SuppressLint("NewApi")
public class InputView extends LinearLayout implements OnFocusChangeListener,
		OnClickListener {

	// 默认参数
	private static final int DEFAULT_TEXT_SIZE = 28; // 默认的字体大小
	private static final int NORMAL_BG_RESOURCE = R.drawable.bg_edit1; // 默认背景资源
	private static final int HOVER_BG_RESOURCE = R.drawable.bg_edit2; // 聚焦背景资源
	private static final int REFRENCE_IMG_RESOURCE = R.drawable.input_refrence_img;// 背景图标资源

	// 组成的控件
	private TextView labelTv; // 前面提示label
	private AutoCompleteTextView inputEt; // 输入框
	private TextView unitTv; // 后面的单位，如金钱：元
	private ImageView refrenceIv;// 背景图片

	// 控件对应的Layout
	private LayoutParams labelLp; // label对应的LayoutParams
	private LayoutParams unitLp; // unit对应的LayoutParams
	private LayoutParams inputLp;// 输入框对应的LayoutParams

	// 输入类型参数
	private boolean special; // 是否特殊输入
	private String label_text; // 标签文本
	private Drawable label_background; // 标签背景
	private String hint; // 提示
	private String unit; // 单位
	private boolean password; // 是否密码输入
	public InputMethodManager inputMethodManager;

	/**
	 * 
	 * @param context
	 * @param attrs
	 *            设置参数
	 */
	public InputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		// 初始化参数
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.InputView);
		label_text = (String) a.getText(R.styleable.InputView_label_text);
		label_background = a.getDrawable(R.styleable.InputView_label_background);
		hint = (String) a.getText(R.styleable.InputView_hint);
		unit = (String) a.getText(R.styleable.InputView_unit);
		special = a.getBoolean(R.styleable.InputView_special, false);
		password = a.getBoolean(R.styleable.InputView_password, false);
		a.recycle();

		initViews();
		initLayout();
		initAdd();

	}

	private void initAdd() {
		this.addView(labelTv, labelLp);
		this.addView(inputEt, inputLp);
		if (unit != null) {
			this.addView(unitTv, unitLp);
		}
		if (password)
			inputEt.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		setPadding(20, 0, 0, 0);
	}

	private void initLayout() {
		setGravity(Gravity.CENTER_VERTICAL);
		setOrientation(LinearLayout.HORIZONTAL);
		setBackgroundResource(NORMAL_BG_RESOURCE);
		setClickable(true);
		setOnClickListener(this);
	}

	/**
	 * 初始化控件和布局
	 */
	private void initViews() {

		// 初始化labelTv
		labelTv = new TextView(this.getContext());
		labelTv.setTextColor(getContext().getResources().getColorStateList(
				R.color.black));
		labelTv.setGravity(Gravity.CENTER_VERTICAL);
		labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
		labelTv.setText(label_text);
		labelTv.setBackgroundDrawable(label_background);
		// 初始化unitTv
		unitTv = new TextView(this.getContext());
		unitTv.setTextColor(getContext().getResources().getColorStateList(
				R.color.black));
		unitTv.setGravity(Gravity.CENTER_VERTICAL);
		unitTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
		unitTv.setText(unit);
		unitTv.setPadding(20, 0, 20, 0);
		// 初始化inputEt
		inputEt = new AutoCompleteTextView(this.getContext());
		inputEt.setTextColor(getContext().getResources().getColorStateList(
				R.color.black));
		inputEt.setHintTextColor(getContext().getResources().getColorStateList(
				R.color.hintcolor));
		inputEt.setHint(hint);
		inputEt.setBackgroundDrawable(null);
		inputEt.setSingleLine();
		inputEt.setGravity(Gravity.CENTER_VERTICAL);
		inputEt.setPadding(20, 0, 20, 0);
		inputEt.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
		if (password)
			inputEt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		// 初始化布局
		labelLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		unitLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT);
		inputLp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
		inputLp.weight = 1;

		if (special) {
			inputEt.setEnabled(false);
			inputEt.setFocusable(false);
		}

		inputEt.setOnFocusChangeListener(this);
	}

	/**
	 * @return
	 */
	public AutoCompleteTextView getInputEt() {
		return inputEt;
	}

	public TextView getLabelTv() {
		return labelTv;
	}

	public void setHint(String hint) {
		this.hint = hint;
		inputEt.setHint(hint);
	}

	public TextView getUnitTv() {
		return unitTv;
	}

	public ImageView getRefrenceIv() {
		return refrenceIv;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			setBackgroundResource(NORMAL_BG_RESOURCE);
			inputEt.setGravity(Gravity.CENTER_VERTICAL);
			inputEt.setTextColor(getContext().getResources().getColorStateList(
					R.color.black));
			labelTv.setTextColor(getContext().getResources().getColorStateList(
					R.color.black));
		} else {
			try {
			//	DeviceService.login(getContext());
				// IMEUtil.useGoogleIME();
			} catch (Exception e) {
			}
			setBackgroundResource(HOVER_BG_RESOURCE);
			inputEt.setSelection(inputEt.getText().length());
			showSoftInput();
			inputEt.setTextColor(getContext().getResources().getColorStateList(
					R.color.black));
			labelTv.setTextColor(getContext().getResources().getColorStateList(
					R.color.black));
		}
	}

	@Override
	public void onClick(View v) {
		showSoftInput();
	}

	public void showSoftInput() {
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				inputMethodManager.showSoftInput(inputEt,
						InputMethodManager.RESULT_SHOWN);
			}
		}, 500);
	}

}
