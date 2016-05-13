package com.example.shoufuyi.uitls;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneUtils {
	/**
	 * 
	 * @description 获取手机号码
	 * @param mActivity
	 * @return String
	 * @history created by dengwenguang on Aug 23, 2011
	 */
	public static String getMobileNum(final Activity mActivity) {
		TelephonyManager telephonyManager = (TelephonyManager) mActivity
				.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String mdn = telephonyManager.getLine1Number();
		return mdn;
	}

	/**
	 * 
	 * @description 获取手机imsi
	 * @param mActivity
	 * @return String
	 * @history created by dengwenguang on Aug 23, 2011
	 */
	public static String getImsi(final Activity mActivity) {
		TelephonyManager telephonyManager = (TelephonyManager) mActivity
				.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telephonyManager.getSubscriberId();
		return imsi;
	}

	/**
	 * 
	 * @description 获取手机ESN,CDMA手机机身号简称ESN-Electronic Serial Number的缩写
	 * @param mActivity
	 * @return String
	 * @history created by dengwenguang on Aug 23, 2011
	 */
	public static String getEsn(final Activity mActivity) {
		TelephonyManager tm = (TelephonyManager) mActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		String esn = tm.getDeviceId();
		return esn;
	}
	
	/**
	 * 
	  * @description 获取手机服务运营商
	  * @param mobileNumber
	  * @return 
	  * @return int 0:无法识别，1:中国电信；2:中国移动；3:中国联通
	  * @history created by dengwenguang on 2011-10-31
	 */
	public static int getMobileSP(String mobileNumber) {
		int spType = 0;
		if (!TextUtils.isEmpty(mobileNumber)) {
			Pattern p = Pattern.compile("^((133)|(153)|(18[0,9]))\\d{8}$");
	        Matcher m = p.matcher(mobileNumber);
			if(m.find()){
				spType = 1;
			}
			p = Pattern.compile("^((13[4-9])|(147)|(15[0-2,7-9])|(18[2,7,8]))\\d{8}$");
			m = p.matcher(mobileNumber);    
			if(m.find()){
				spType = 2;
			}
			p = Pattern.compile("^((13[0-2])|(15[5,6])|(18[5,6]))\\d{8}$");
			m = p.matcher(mobileNumber);    
			if(m.find()){
				spType = 3;
			}			
		}
		return spType;
	}
	/**
	 * 
	  * @description 是否手机号码
	  * @param phoneNumber
	  * @return 
	  * @history created by dengwenguang on 2011-10-31
	 */
	 public static boolean isPhoneNumberValid(String phoneNumber)
	  {
	     boolean isValid = false;

	     String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";

	     String expression2 ="^\\(?(\\d{2})\\)?[- ]?(\\d{4})[- ]?(\\d{5})$";

	     CharSequence inputStr = phoneNumber;

	     Pattern pattern = Pattern.compile(expression);

	     Matcher matcher = pattern.matcher(inputStr);

	     Pattern pattern2 = Pattern.compile(expression2);

	     Matcher matcher2= pattern2.matcher(inputStr);
	     if(matcher.matches()||matcher2.matches())
	     {
	     isValid = true;
	     }
	     return isValid; 
	   }
	 
	 //获取通讯录
//	 public static ArrayList<UserContactDTO> getContacts(Context context) {
//			ArrayList<UserContactDTO> list = new ArrayList<UserContactDTO>();
//			String[] columns = new String[] { Phones.NAME, Phones.NUMBER };
//			Cursor cursor = context.getContentResolver().query(Phones.CONTENT_URI,
//					columns, null, null, People.DEFAULT_SORT_ORDER);
//			while (cursor.moveToNext()) {
//				UserContactDTO bean = new UserContactDTO();
//				String phone = cursor.getString(1);
////				System.out.println("phone="+phone);
////				if(Utils.isEmpty(phone)||!PhoneUtils.isPhoneNumberValid(phone)){
//				if(Utils.isEmpty(phone)||phone.length()!=11){
//					continue;
//				}
//				bean.setMobilePhone(phone);
//				bean.setContactName(cursor.getString(0));
//				list.add(bean);
//			}
//			cursor.close();
//			return list;
//		}
	
}
