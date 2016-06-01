package com.cchtw.sfy.uitls;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.widget.EditText;

public class PhoneWatcherUtil implements TextWatcher {
	private boolean isChanged = false;
	private String displayString = "";
	private EditText editText = null;

	public PhoneWatcherUtil(EditText edit) {
		editText = edit;
		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(13) });
		char[] mychar = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
		editText.setKeyListener(getKeylistener(mychar));
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

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (isChanged)
			return;
		isChanged = true;
		String cuttedStr = s.toString().trim().replace(" ", "");
		if (cuttedStr.equals("")) {
			displayString = "";
		} else if (!cuttedStr.substring(0, 1).equals("1")) {
			displayString = getPhoneShowText(cuttedStr.substring(1,
					cuttedStr.length()));
		} else {
			displayString = getPhoneShowText(cuttedStr);
		}
		editText.setText(displayString);
		editText.setSelection(editText.length());
		isChanged = false;
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public String getPhoneShowText(String phone) {
		String show = "";
		if (phone.length() < 4) {
			show = phone;
		} else if (phone.length() < 8) {
			show = phone.substring(0, 3);
			show += " ";
			show += phone.substring(3);
		} else {
			show = phone.substring(0, 3);
			show += " ";
			show += phone.substring(3, 7);
			show += " ";
			show += phone.substring(7);
		}
		return show;
	}
}
