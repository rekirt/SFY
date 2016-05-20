package com.example.shoufuyi.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.example.shoufuyi.uitls.view.EmptyLayout;
import com.itech.message.APP_120021;
import com.itech.message.APP_120024;
import com.itech.message.Result_120023;
import com.itech.message.VerifyGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description:签约详情 Created by Fu.H.L on Date:2015-09-21 Time:下午3:16 Copyright ©
 * 2015年 Fu.H.L All rights reserved.
 */
public class SignDetailActivity extends BaseActivity {

	private Intent intent;
	private NetworkInfo info;
	private Result_120023 mResult;
	private ConnectivityManager connectivityManager;
    private APP_120024 mSignDetail = new APP_120024();
    private APP_120021 mMerchantVerifyStyle = new APP_120021();

	private List<VerifyGroup> mVerifyGroupList = new ArrayList<VerifyGroup>();
	private boolean isIDSuccess = false;
	private boolean isBankCardSuccess = false;
	private boolean isVedioSuccess = true;
	private boolean isSignPicSuccess = false;
	private boolean isIconSuccess = false;
	private boolean issign = false;
	private StringBuffer strbuf = new StringBuffer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_detail);
		intent = SignDetailActivity.this.getIntent();
		Bundle bundle = intent.getExtras();
		mResult = (Result_120023) bundle.get("info");
		assignViews();
		initData();
		getSignDetail();
	}

	private TextView tv_merchant_name;// 用户名称
	private TextView tv_card_number;//卡号
	private TextView tv_bank_card_front_state;//银行卡正面照片状态
	private TextView tv_bank_card_back_state;//银行卡背面照片状态
	private TextView tv_deal_pwd_state;//交易密码状态
	private TextView tv_phone_number;//手机号码
	private TextView tv_card_holder;//持卡人
	private TextView tv_avatar_state;//头像
	private TextView tv_id_number_state;//身份证状态
	private TextView tv_id_number;//身份证号码
	private TextView tv_id_card_front_state;//身份证正面状态
	private TextView tv_id_card_back_state;//身份证背面状态
	private TextView tv_id_card_holder_video_state;//持卡人视频状态
	private TextView tv_agreement_pic_state;//协议照片状态
	private TextView tv_e_agreement_state;//电子协议状态
    protected EmptyLayout mErrorLayout;//错误页

	public void assignViews() {
		// 显示信息
		tv_merchant_name = (TextView) findViewById(R.id.tv_merchant_name);
		tv_card_number = (TextView) findViewById(R.id.tv_card_number);
		tv_bank_card_front_state = (TextView) findViewById(R.id.tv_bank_card_front_state);
		tv_bank_card_back_state = (TextView) findViewById(R.id.tv_bank_card_back_state);
		tv_deal_pwd_state = (TextView) findViewById(R.id.tv_deal_pwd_state);
		tv_phone_number = (TextView) findViewById(R.id.tv_phone_number);//
		tv_card_holder = (TextView) findViewById(R.id.tv_card_holder);//
		tv_avatar_state = (TextView) findViewById(R.id.tv_avatar_state);//
		tv_id_number_state = (TextView) findViewById(R.id.tv_id_number_state);//
		tv_id_number = (TextView) findViewById(R.id.tv_id_number);//
		tv_id_card_front_state = (TextView) findViewById(R.id.tv_id_card_front_state);// 采集身份证正面照片:
		tv_id_card_back_state = (TextView) findViewById(R.id.tv_id_card_back_state);// 采集身份证反面照片:
		tv_id_card_holder_video_state = (TextView) findViewById(R.id.tv_id_card_holder_video_state);//
		tv_agreement_pic_state = (TextView) findViewById(R.id.tv_agreement_pic_state);//
		tv_e_agreement_state = (TextView) findViewById(R.id.tv_e_agreement_state);//
        mErrorLayout = (EmptyLayout) findViewById(R.id.error_layout);
    }

	public void initData() {
		tv_bank_card_front_state.setOnClickListener(this);
		tv_bank_card_back_state.setOnClickListener(this);
		tv_deal_pwd_state.setOnClickListener(this);
		tv_avatar_state.setOnClickListener(this);
		tv_id_number_state.setOnClickListener(this);
		tv_id_card_front_state.setOnClickListener(this);
		tv_id_card_back_state.setOnClickListener(this);
		tv_id_card_holder_video_state.setOnClickListener(this);
		tv_agreement_pic_state.setOnClickListener(this);
		tv_e_agreement_state.setOnClickListener(this);
        mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                getSignDetail();
            }
        });

    }

    /**
     * 获取券商验证组合
     *
     */
//	private void getMerchantStyle(){
//        DialogHelper.showProgressDialog(SignDetailActivity.this, "正在获取券商验证组合...", true, false);
//        final APP_120021 app = new APP_120021();
//        app.setTrxCode("120021");
//        app.setMerchantId(mResult.getMerchantId());
//        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
//        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
//            @Override
//            public void onDo(JSONObject responseJsonObject) {
//                mMerchantVerifyStyle = JSON.parseObject(responseJsonObject.toString(), APP_120021.class);
//                mVerifyGroupList = mSignDetail.getVerifyGroupList();// 签约详情中的附件组合
//                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            }
//
//            @Override
//            public void onDo(JSONArray responseJsonArray) {
//
//            }
//
//            @Override
//            public void onDo(String responseString) {
//
//            }
//
//            @Override
//            public void onFail(String msg) {
//                super.onFail(msg);
//                mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//            }
//
//            @Override
//            public void onFinish() {
//                DialogHelper.dismissProgressDialog();
//            }
//        });
//    }


    /**
     * 获取签约详情
     */
    private void getSignDetail() {
        final APP_120024 app = new APP_120024();
        app.setTrxCode("120024");
        app.setMerchantId(mResult.getMerchantId());
        app.setAccountNo(mResult.getAccountNo());
        app.setIdCard(mResult.getIdCard());
        app.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        DialogHelper.showProgressDialog(SignDetailActivity.this, "正在获取签约详情...", true, false);
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                mSignDetail = JSON.parseObject(responseJsonObject.toString(), APP_120024.class);
                mVerifyGroupList = mSignDetail.getVerifyGroupList();// 签约详情中的附件组合
//                    if (mVerifyGroupList != null) {
//                        getAttachValues(mVerifyGroupList);
//                    }
                updateViewState(mSignDetail);
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFail(String msg) {
                super.onFail(msg);
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
            }

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    /**
     * 根据后台返回的验证组合来动态显示页面
     * @param verifyGroupList 验证组合
     */
    private void setVerifyGroup(List<VerifyGroup> verifyGroupList) {
        for (VerifyGroup group : verifyGroupList) {
            switch (group.getVerifyGroupCode()) {
                case "TWO_ELEMENT":
                   // ElementNumber = 5;
//                    changeSixElementVerifyState(group.getVerifyState());
                    break;
                case "THREE_ELEMENT":
//                    ElementNumber = 4;
//                    changeFourElementVerifyState(group.getVerifyState());
                    break;
                case "FOUR_ELEMENT":
//                    ElementNumber = 6;
//                    isPWDNeed = true;
//                    changeSixElementVerifyState(group.getVerifyState());
                    break;
                case "SIX_ELEMENT":
//                    ElementNumber = 6;
//                    isPWDNeed = true;
//                    changeSixElementVerifyState(group.getVerifyState());
                    break;
                case "CARD_PASS":
//                    ElementNumber = 6;
//                    isPWDNeed = true;
//                    changeSixElementVerifyState(group.getVerifyState());
                    break;

                case "PORTRAIT_COMPARISON"://人像对比验证
//                    List<VerifyItem> list = group.getVerifyItemList();
//                    for (VerifyItem item : list) {
//                        if (item.getVerifyItemCode().equals("PHOTO")) {
//                            handlePhoto(group);
//                        } else if (item.getVerifyItemCode().equals("IDCARD_PHOTO")) {
//                            handlePortraitComparison(group);
//
//                        }
//                    }

                    break;
                // 人像视频验证(包括视频和头像)
                case "PHOTO_VIDEO":
//                    List<VerifyItem> list1 = group.getVerifyItemList();
//                    for (VerifyItem item : list1) {
//                        if (item.getVerifyItemCode().equals("PHOTO")) {
//                            handlePhoto(group);
//                        } else if (item.getVerifyItemCode().equals("VIDEO")) {
//                            handlePhotoVideo(group);
//                        }
//                    }
                    break;
                // 电子签名
                case "E_SIGN":
//                    isSignPicNeed = true;
//                    changeEsignState(group.getVerifyState());

                    break;
                // 银行卡照片
                case "BANK_CARD_PHOTO":
//                    handleBankCardPhoto(group);
                    break;
                // 纸质协议
                case "PAPER_PROTOCOL":
//                    handlePaperProtocol(group);
                    break;
                case "PASS":
//                    isPWDNeed = true;
//                    changeCardPassWordVerifyState(group.getVerifyState());
                    break;
                default:
                    break;
            }
        }

    }

//    private TextView tv_merchant_name;// 用户名称
//    private TextView tv_card_number;//卡号
//    private TextView tv_bank_card_front_state;//银行卡正面照片状态
//    private TextView tv_bank_card_back_state;//银行卡背面照片状态
//    private TextView tv_deal_pwd_state;//交易密码状态
//    private TextView tv_phone_number;//手机号码
//    private TextView tv_card_holder;//持卡人
//    private TextView tv_avatar_state;//头像
//    private TextView tv_id_number_state;//身份证状态
//    private TextView tv_id_number;//身份证号码
//    private TextView tv_id_card_front_state;//身份证正面状态
//    private TextView tv_id_card_back_state;//身份证背面状态
//    private TextView tv_id_card_holder_video_state;//持卡人视频状态
//    private TextView tv_agreement_pic_state;//协议照片状态
//    private TextView tv_e_agreement_state;//电子协议状态
    private void updateViewState(APP_120024 app_120024){
       //要素验证
        tv_phone_number.setText(mResult.getMobile());
        tv_card_number.setText(app_120024.getVerifyGroupList().get(0).getVerifyItemList().get(0).getVerifyItemValue());
        tv_merchant_name.setText(app_120024.getVerifyGroupList().get(0).getVerifyItemList().get(1).getVerifyItemValue());
        tv_id_number.setText(app_120024.getVerifyGroupList().get(0).getVerifyItemList().get(2).getVerifyItemValue());
        //1：未验证 2：验证通过 3：验证不通过
        switch (app_120024.getVerifyGroupList().get(0).getVerifyState()){
            case "1":
                tv_id_number_state.setText("未验证");
                break;
            case "2":
                tv_id_number_state.setText("验证通过");
                break;
            case "3":
                tv_id_number_state.setText("验证不通过");
                break;
            default:
                break;
        }


        //人像比对验证

    }

	@Override
	protected void onStop() {
		super.onStop();
	}
}
