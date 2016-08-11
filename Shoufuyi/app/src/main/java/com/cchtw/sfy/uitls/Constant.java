package com.cchtw.sfy.uitls;

import android.os.Environment;

import com.itech.utils.SequenceUtil;

public class Constant {
	public static String USER = "USER";
	public static String url = "http://www.itisfs.com/SfyRelayServices_v1/appRevServer";
	//密码控件publickey
	public static String publickey = "30818902818100e772a7ba31dc574f7adb3a0b8a05bc7780146fed534b72e2c921ab5e11791608d44212f323a3c233f8721cf8546ade8c4dc8162b79005489ee821b4d3875eb048f762359c077094cc013e1f85fb45068500d1e4b31a060eed42aede6f2872f3f4110adc443be174410618bf4b75e5122ea7e17ed3c1dd5929d3ae84c1c1c12950203010001";
	//public static String publickey = "30818902818100bc173c063de2fe606dba269ca956876754c07785b79d989445794e977e1198688d4eb426fd368ba554e3b143be57c2ee1c2634eb28c5867c57816786b2fb947d40f935debe39a34c383b1766f38181e5adb58e020dd6e5b22d457131731b65a7104f9a8e83c9cd3d583801bb1b9155220872671ef10b8f0d5ad82c579545101d0203010001";
	public static String secretType = "1";
	public static String reqCode = "01";
	public static String token = SequenceUtil.TOKEN;
	// 签约信息查询三种状态
	public static String UNSIGN = "1";
	public static String EFFECT = "2";
	public static String UNEFFECT = "3";
	public static String Deskey = "";
	public static String PICHASHCODE = "PICHASHCODE";
	public static String BANKFRONTHASHCODE = "BANKFRONTHASHCODE";
	public static String BANKBACKHASHCODE = "BANKBACKHASHCODE";
	public static String IDCARDFRONTHASHCODE = "IDCARDFRONTHASHCODE";
	public static String IDCARDBACKHASHCODE = "IDCARDBACKHASHCODE";
	public static String VIDEOHASHCODE = "VIDEOHASHCODE";
	public static String SIGNHASHCODE = "SIGNHASHCODE";
	public static int time = 91;

	public static String UUID = "uuid";
	public static String three="THREE_ELEMENT";
	public static String four="FOUR_ELEMENT";
	public static String five="FIVE_ELEMENT";
	public static String six="SIX_ELEMENT";

	public static String devcode = "5BMA5BEE6ZO26IG";
//	public static String devcode = "5YYX5LQS5PAH6YC";

	public static String banknumber="";

	public static String SHARED_PREFERENCE_NAME = TDevice.getVersionName()+"SFY_SHARED_PREFERENCE_NAME";
    public static String ACTIVATION = "jihuo";
    public static String PHONE = "phone";

    public static String DESKEY = "deskey";
    public static String DESK3KEY = "des3key";
    public static String TOKEN = "token";

    public static String NUMBER = "number";
    public static String ISEXIT = "Is_exit";
    public static String GAIMIMA = "gaimima";
//	public static String MIMA = "mima";
//	public static String MERCHANT = "merchant";
	public static String LOGIN = "login";
//	public static String ISLOGIN = "Is_Login";
	public static String LOGINSERECT = "LoginSerect";

	public static String FINGERPASSWORD = "FINGERPASSWORD";//手势密码
	public static String HAVESETFINGERPWD = "HAVESETFINGERPWD";

	public static final long CACHE_EXPIRE_DAY = 3600000*200;//两百天
    public static final long CACHE_EXPIRE_OND_DAY =3600000*(Integer.parseInt(SharedPreferencesHelper.getString(Constant.VEDIOANDPHOTOCACHELONG, "10")));//默认十天

//运行参数
	public static String VEDIOANDPHOTOCACHELONG = "VEDIOANDPHOTOCACHELONG";//视频图片缓存时长
	public static String FINGERPASSWORDTIMES = "FINGERPASSWORDTIMES";//手势密码次数
	public static String VEDIOLONG = "VEDIOLONG";//视频时长
	public static String TIMEOUT = "ITMEOUT";//超时时长
	public static String PAGESIZE = "PAGESIZE";//每页数据量
    public static String PRIVILAGE = "privilage";//用户权限

	public static String BANKCRADNUMBER = "BANKCRADNUMBER";//识别后银行卡号码
	public static String IDCRADNUMBER = "IDCRADNUMBER";//识别后ID卡号码

	public static boolean ISTEST = false;//是否是测试

	//基本路径
	public final static String BASE_DIR = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/SFY/";
	//缓存路径
	public final static String CACHE_DIR = BASE_DIR + ".cache/";
}
