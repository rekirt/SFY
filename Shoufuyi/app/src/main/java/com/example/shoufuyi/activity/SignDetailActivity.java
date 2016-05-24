package com.example.shoufuyi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.view.EmptyLayout;
import com.itech.message.APP_120024;
import com.itech.message.Result_120023;
import com.itech.message.VerifyGroup;
import com.itech.message.VerifyItem;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * Description:签约详情 Created by Fu.H.L on Date:2015-09-21 Time:下午3:16 Copyright ©
 * 2015年 Fu.H.L All rights reserved.
 */
public class SignDetailActivity extends BaseActivity {

	private Result_120023 mResult;
    private APP_120024 mSignDetail = new APP_120024();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_detail);
        Intent intent = SignDetailActivity.this.getIntent();
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
        setCanBack(true);
    }

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
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                mSignDetail = JSON.parseObject(responseJsonObject.toString(), APP_120024.class);
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
        });
    }

    /**
     * 根据后台返回的验证组合来动态显示页面
     */
    private void updateViewState(APP_120024 app_120024){
       //要素验证
        tv_phone_number.setText(mResult.getMobile());
        tv_card_number.setText(app_120024.getAccountNo());
        tv_id_number.setText(app_120024.getIdCard());

        for (VerifyGroup verifyGroup : app_120024.getVerifyGroupList()){
            switch (verifyGroup.getVerifyGroupCode()){
                case "PORTRAIT_COMPARISON"://人像比对验证（身份证照片、客户正面头像）
                    handlePortraitComparison(verifyGroup);
                    break;
                case "BANK_CARD_PHOTO"://银行卡照片(银行卡照片)
                    handleBankCardPhoto(verifyGroup);
                    break;
                case "PAPER_PROTOCOL"://纸质协议
                    handlePaperProtocol(verifyGroup);
                    break;
                case "E_SIGN"://电子签名
                    handleESign(verifyGroup);
                    break;
                case "PHOTO_VIDEO"://人像视频验证（视频、客户正面头像）
                    handlePhotoVideo(verifyGroup);
                    break;
                case "SIX_ELEMENT"://六要素(卡号\户名\身份证号\手机号\手机验证码\支付密码)
                    handleSixElement(verifyGroup);
                    break;
                case "TWO_ELEMENT"://二要素(卡号\户名)
                    handleTwoThreeFourElement(verifyGroup);
                    break;
                case "THREE_ELEMENT"://三要素(卡号\户名\身份证号)
                    handleTwoThreeFourElement(verifyGroup);
                    break;
                case "FOUR_ELEMENT"://四要素(卡号\户名\身份证号\手机号)
                    handleTwoThreeFourElement(verifyGroup);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 处理人像比对验证
     */
    private String mFrontIdCardFileId ="";
    private String mBackIdCardFileId = "";
    private String mPhotoFileId = "";

    private void handlePortraitComparison(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_id_card_front_state.setText("未验证");
                tv_id_card_back_state.setText("未验证");
                tv_avatar_state.setText("未验证");
                break;
            case "2":
                tv_id_card_front_state.setText("验证通过");
                tv_id_card_back_state.setText("验证通过");
                tv_avatar_state.setText("验证通过");
                break;
            case "3":
                tv_id_card_front_state.setText("验证不通过");
                tv_id_card_back_state.setText("验证不通过");
                tv_avatar_state.setText("验证不通过");
                break;
            default:
                tv_id_card_front_state.setText("点击采集");
                tv_id_card_back_state.setText("点击采集");
                tv_avatar_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "IDCARD_PHOTO"://身份证正反面
                    if (verifyItem.getFileList().size() > 0)
                    mFrontIdCardFileId = verifyItem.getFileList().get(0).getFileId();
                    if (verifyItem.getFileList().size() > 1)
                    mBackIdCardFileId = verifyItem.getFileList().get(1).getFileId();
                    break;
                case "PHOTO":
                    if (verifyItem.getFileList().size() > 0)
                    mPhotoFileId = verifyItem.getFileList().get(0).getFileId();
                    break;
                default:

                    break;
            }
        }
    }

    /**
     * 处理银行卡照片
     */
    private String mFrontBankCardFileId ="";
    private String mBackBankCardFileId = "";

    private void handleBankCardPhoto(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_bank_card_front_state.setText("未验证");
                tv_bank_card_back_state.setText("未验证");
                break;
            case "2":
                tv_bank_card_front_state.setText("验证通过");
                tv_bank_card_back_state.setText("验证通过");
                break;
            case "3":
                tv_bank_card_front_state.setText("验证不通过");
                tv_bank_card_back_state.setText("验证不通过");
                break;
            default:
                tv_bank_card_front_state.setText("点击采集");
                tv_bank_card_back_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "BANK_CARD_PHOTO"://银行卡正反面
                    if (verifyItem.getFileList().size() > 0)
                        mFrontBankCardFileId = verifyItem.getFileList().get(0).getFileId();
                    if (verifyItem.getFileList().size() > 1)
                        mBackBankCardFileId = verifyItem.getFileList().get(1).getFileId();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理纸质协议
     */
    private String mPaperProtocolFileId ="";

    private void handlePaperProtocol(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_agreement_pic_state.setText("未验证");
                break;
            case "2":
                tv_agreement_pic_state.setText("验证通过");
                break;
            case "3":
                tv_agreement_pic_state.setText("验证不通过");
                break;
            default:
                tv_agreement_pic_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "PAPER_PROTOCOL":
                    if (verifyItem.getFileList().size() > 0)
                        mPaperProtocolFileId = verifyItem.getFileList().get(0).getFileId();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理电子签名
     */
    private String mESignFileId ="";

    private void handleESign(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_e_agreement_state.setText("签订");
                break;
            case "2":
                tv_e_agreement_state.setText("已签订");
                break;
            case "3":
                tv_e_agreement_state.setText("验证不通过");
                break;
            default:
                tv_e_agreement_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "E_SIGN":
                    if (verifyItem.getFileList().size() > 0)
                        mESignFileId = verifyItem.getFileList().get(0).getFileId();
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 处理人像视频验证
     */
    private String mVideoFileId ="";

    private void handlePhotoVideo(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_id_card_holder_video_state.setText("未验证");
                tv_avatar_state.setText("未验证");
                break;
            case "2":
                tv_id_card_holder_video_state.setText("验证通过");
                tv_avatar_state.setText("验证通过");
                break;
            case "3":
                tv_id_card_holder_video_state.setText("验证不通过");
                tv_avatar_state.setText("验证不通过");
                break;
            default:
                tv_id_card_holder_video_state.setText("点击采集");
                tv_avatar_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "VIDEO"://持卡人视频
                    if (verifyItem.getFileList().size() > 0)
                        mVideoFileId = verifyItem.getFileList().get(0).getFileId();
                    break;
                case "PHOTO":
                    if (verifyItem.getFileList().size() > 0)
                        mPhotoFileId = verifyItem.getFileList().get(0).getFileId();
                    break;
                default:

                    break;
            }
        }
    }

    /**
     * 处理六要素
     * @param verifyGroup
     */
    private void handleSixElement(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_deal_pwd_state.setText("未验证");
                tv_id_number_state.setText("未验证");
                break;
            case "2":
                tv_deal_pwd_state.setText("验证通过");
                tv_id_number_state.setText("验证通过");
                break;
            case "3":
                tv_deal_pwd_state.setText("验证不通过");
                tv_id_number_state.setText("验证不通过");
                break;
            default:
                tv_deal_pwd_state.setText("点击采集");
                tv_id_number_state.setText("点击采集");
                break;
        }
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "IDCARD"://身份证号

                    break;
                case "PASS"://支付密码
                    break;
                case "ACCOUNT_NO"://卡号
                    break;
                case "ACCOUNT_NAME"://户名
                    tv_merchant_name.setText(verifyItem.getVerifyItemValue());
                    break;
                case "MOBILE"://手机号
                    break;
                case "VAL_CODE"://手机验证码
                    break;
                default:

                    break;
            }
        }
    }

    /**
     * 处理二三四要素
     * @param verifyGroup
     */
    private void handleTwoThreeFourElement(VerifyGroup verifyGroup){
        for (VerifyItem verifyItem : verifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "IDCARD"://身份证号
                    break;
                case "PASS"://支付密码
                    break;
                case "ACCOUNT_NO"://卡号
                    break;
                case "ACCOUNT_NAME"://户名
                    tv_merchant_name.setText(verifyItem.getVerifyItemValue());
                    break;
                case "MOBILE"://手机号
                    break;
                case "VAL_CODE"://手机验证码
                    break;
                default:
                    break;
            }
        }
    }

    @Override
	protected void onStop() {
		super.onStop();
	}
}
