package com.cchtw.sfy.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.cchtw.sfy.uitls.view.EmptyLayout;
import com.itech.message.APP_120001;
import com.itech.message.APP_120003;
import com.itech.message.APP_120024;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.message.VerifyGroup;
import com.itech.message.VerifyItem;

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

	private Result_120023 mResult;
    private APP_120024 mSignDetail = new APP_120024();
    private String mPhoneNumber;
    private String mVideoFileId ="";
    private String mESignFileId ="";
    private String mFrontIdCardFileId ="";
    private String mBackIdCardFileId = "";
    private String mFrontBankCardFileId ="";
    private String mBackBankCardFileId = "";
    private String mPhotoFileId = "";
    private ArrayList<String> mArrayListProtocolFileId = new ArrayList<String>();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_detail);
        Intent intent = SignDetailActivity.this.getIntent();
		Bundle bundle = intent.getExtras();
		mResult = (Result_120023) bundle.get("info");
        mPhoneNumber = SharedPreferencesHelper.getString(Constant.PHONE, "");
		assignViews();
		initData();
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
    private EmptyLayout mErrorLayout;//错误页
    private Button btn_submit;

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
        btn_submit = (Button) findViewById(R.id.btn_submit);//

    }

	public void initData() {
        btn_submit.setOnClickListener(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        getSignDetail();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.tv_bank_card_front_state:
            case R.id.tv_bank_card_back_state:
                gotoTakeBankCardPhoto();
                break;
            case R.id.tv_id_card_front_state:
            case R.id.tv_id_card_back_state:
                gotoTakeIDCardPhoto();
                break;
            case R.id.tv_id_card_holder_video_state:
                gotoTakeVideo();
                break;
            case R.id.tv_agreement_pic_state:
                gotoTakeProtocolPhoto();
                break;
            case R.id.tv_e_agreement_state:
                if (pregotoAgreement()){
                    gotoAgreement();
                }
                break;
            case R.id.tv_deal_pwd_state:
            case R.id.tv_id_number_state:
                gotoElementVerify();
                break;
            case R.id.tv_avatar_state:
                gotoTakeAvatarPhoto();
                break;
            case R.id.btn_submit:
                if (preSubmitSign()){
                    submitSign();
                }
                break;
            default:
                break;
        }
    }

    private boolean preSubmitSign(){
        if (TextUtils.isEmpty(mVideoFileId) || TextUtils.isEmpty(mESignFileId)
                || TextUtils.isEmpty(mFrontIdCardFileId) || TextUtils.isEmpty(mBackIdCardFileId)
                || TextUtils.isEmpty(mFrontBankCardFileId)|| TextUtils.isEmpty(mBackBankCardFileId)
                || TextUtils.isEmpty(mPhotoFileId) || mArrayListProtocolFileId.size()>0){
            ToastHelper.ShowToast("请先采集全所需附件以及签订电子协议");
            return false;
        }
        return true;
    }
    private void submitSign(){
        //应该先进行签约要素验证
        newSign();//正式提交签约
    }

    // 提交签约

    private StringBuffer strbuf = new StringBuffer();
    private void newSign() {
        final APP_120003 app = new APP_120003();
        app.setTrxCode("120003");
        app.setMerchantId(mResult.getMerchantId());
        app.setAccountNo(mResult.getAccountNo());
        app.setIdCard(mResult.getIdCard());
        app.setUserName(mPhoneNumber);
        DialogHelper.upDateProgressDialog(SignDetailActivity.this, "正在提交签约...", true, false);
        ApiRequest.requestData(app, mPhoneNumber, new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120003 mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120003.class);
                    if (mReturnApp != null && "0000".equals(mReturnApp.getDetailCode())) {
                        ToastHelper.ShowToast("提交成功，签约正在处理中!");
                        SignDetailActivity.this.finish();
                    } else {
                        if (mReturnApp != null) {
                            List<VerifyGroup> list = mReturnApp.getVerifyGroupList();
                            for (VerifyGroup v : list) {
                                if (!v.getDetailCode().equals("0000")) {
                                    if (v.getVerifyGroupCode().equals(
                                            "PORTRAIT_COMPARISON")) {
                                        strbuf.append(
                                                "人像对比"
                                                        + v.getDetailInfo())
                                                .append("\n");
                                    }

                                    else if (v.getVerifyGroupCode()
                                            .equals("BANK_CARD_PHOTO")) {
                                        strbuf.append(
                                                "银行卡"
                                                        + v.getDetailInfo())
                                                .append("\n");

                                    } else if (v.getVerifyGroupCode()
                                            .equals("PAPER_PROTOCOL")) {
                                        strbuf.append(
                                                "纸质协议"
                                                        + v.getDetailInfo())
                                                .append("\n");

                                    } else if (v.getVerifyGroupCode()
                                            .equals("E_SIGN")) {
                                        strbuf.append(

                                                "电子签名" + v.getDetailInfo())
                                                .append("\n");

                                    } else if (v.getVerifyGroupCode()
                                            .equals("PHOTO_VIDEO")) {
                                        strbuf.append(
                                                "视频"
                                                        + v.getDetailInfo())
                                                .append("\n");

                                    } else if (v.getVerifyGroupCode()
                                            .equals(Constant.six)) {
                                        strbuf.append(
                                                "六要素"
                                                        + v.getDetailInfo())
                                                .append("\n");
                                    } else if (v.getVerifyGroupCode()
                                            .equals("PHOTO")) {
                                        strbuf.append(
                                                "头像照片"
                                                        + v.getDetailInfo())
                                                .append("\n");

                                    } else if (v.getVerifyGroupCode()
                                            .equals(Constant.five)) {
                                        strbuf.append(
                                                "五要素"
                                                        + v.getDetailInfo())
                                                .append("\n");
                                    } else if (v.getVerifyGroupCode()
                                            .equals(Constant.four)) {
                                        strbuf.append(
                                                "四要素"
                                                        + v.getDetailInfo())
                                                .append("\n");

                                    }
                                }
                            }
                        }
                        showfail();
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
    private void showfail() {
        Dialog dialog = new AlertDialog.Builder(SignDetailActivity.this)
                .setTitle("签约失败") // 创建标题
                .setMessage(strbuf.toString()) // 表示对话框中的内容
//                .setIcon(R.drawable.fail) // 设置LOGO
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create(); // 创建了一个对话框
        dialog.show(); // 显示对话框
        strbuf = new StringBuffer();

    }

    private boolean pregotoAgreement(){
        if (TextUtils.isEmpty(mVideoFileId) || TextUtils.isEmpty(mFrontIdCardFileId) || TextUtils.isEmpty(mBackIdCardFileId)
                || TextUtils.isEmpty(mFrontBankCardFileId)|| TextUtils.isEmpty(mBackBankCardFileId)
                || TextUtils.isEmpty(mPhotoFileId) || mArrayListProtocolFileId.size()>0){
            ToastHelper.ShowToast("请先采集全所需附件");
            return false;
        }
        return true;
    }

    private void gotoAgreement(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mResult);
        bundle.putString("mESignFileId", mESignFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, AgreementActivity.class);
        startActivity(intent);
    }

    private void gotoTakeProtocolPhoto(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("result", mResult);
        bundle.putStringArrayList("mArrayListProtocolFileId", mArrayListProtocolFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, TakeProtocolPicActivity.class);
        startActivity(intent);
    }

    private void gotoTakeAvatarPhoto(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mResult);
        bundle.putString("mPhotoFileId",mPhotoFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, TakeAvatarPhotoActivity.class);
        startActivity(intent);
    }

    private void gotoElementVerify(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        APP_120001 mReturn = new APP_120001();
        mReturn.setVerifyGroupList(mSignDetail.getVerifyGroupList());
        mReturn.setAccountName(mResult.getAccountName());
        mReturn.setAccountNo(mResult.getAccountNo());
        mReturn.setIdCard(mResult.getIdCard());
        mReturn.setMerchantId(mResult.getMerchantId());
        mReturn.setMobile(mResult.getMobile());
        mReturn.setTrxCode("120001");
        bundle.putSerializable("return", mReturn);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, ElementVerificationActivity.class);
        startActivity(intent);
    }

    private void gotoTakeVideo(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mResult);
        bundle.putString("mVideoFileId",mVideoFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, TakeVideoActivity.class);
        startActivity(intent);
    }

    private void gotoTakeBankCardPhoto(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mResult);
        bundle.putString("mFrontBankCardFileId", mFrontBankCardFileId);
        bundle.putString("mBackBankCardFileId", mBackBankCardFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, TakeBankCardPhotoActivity.class);
        startActivity(intent);
    }

    private void gotoTakeIDCardPhoto(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mResult);
        bundle.putString("mFrontIdCardFileId", mFrontIdCardFileId);
        bundle.putString("mBackIdCardFileId", mBackIdCardFileId);
        intent.putExtras(bundle);
        intent.setClass(SignDetailActivity.this, TakeIDCardPhotoActivity.class);
        startActivity(intent);
    }

    /**
     * 获取签约详情
     */
    private void getSignDetail() {
        btn_submit.setEnabled(false);//更新详情前设置提交按钮不可交互
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

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
                btn_submit.setEnabled(true);//更新详情后恢复提交按钮可交互
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
    private void handlePortraitComparison(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_id_card_front_state.setText("点击采集");
                tv_id_card_back_state.setText("点击采集");
                tv_avatar_state.setText("点击采集");
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
                break;
        }
        getPortraitComparisonFileId(verifyGroup);
    }

    /**
     * 获取身份证照片对于的文件id
     * @param mVerifyGroup 验证组
     */
    private void getPortraitComparisonFileId(VerifyGroup mVerifyGroup){
        for (VerifyItem verifyItem : mVerifyGroup.getVerifyItemList()){
            switch (verifyItem.getVerifyItemCode()){
                case "IDCARD_PHOTO"://身份证正反面
                    if (verifyItem.getFileList().size() > 0)
                        mFrontIdCardFileId = verifyItem.getFileList().get(0).getFileId();
                    if (verifyItem.getFileList().size() > 1)
                        mBackIdCardFileId = verifyItem.getFileList().get(1).getFileId();
                    if (!TextUtils.isEmpty(mFrontIdCardFileId) && !TextUtils.isEmpty(mBackIdCardFileId)){
                        tv_id_card_front_state.setText("点击查看");
                        tv_id_card_back_state.setText("点击查看");
                    }
                    break;
                case "PHOTO":
                    if (verifyItem.getFileList().size() > 0)
                        mPhotoFileId = verifyItem.getFileList().get(0).getFileId();
                    if (!TextUtils.isEmpty(mPhotoFileId)){
                        tv_avatar_state.setText("点击查看");
                    }
                    break;
                default:

                    break;
            }
        }
    }
    /**
     * 处理银行卡照片
     */

    private void handleBankCardPhoto(VerifyGroup verifyGroup){
        //1：点击采集 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_bank_card_front_state.setText("点击采集");
                tv_bank_card_back_state.setText("点击采集");
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
                    if (!TextUtils.isEmpty(mFrontBankCardFileId) && !TextUtils.isEmpty(mBackBankCardFileId)){
                        tv_bank_card_front_state.setText("点击查看");
                        tv_bank_card_back_state.setText("点击查看");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理纸质协议
     */

    private void handlePaperProtocol(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_agreement_pic_state.setText("点击采集");
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
                    for (FileMsg fileMsg : verifyItem.getFileList()){
                        mArrayListProtocolFileId.add(fileMsg.getFileId());
                    }
                    break;
                default:
                    break;
            }
        }
        if (mArrayListProtocolFileId.size()>0){
            tv_agreement_pic_state.setText("点击查看");
        }
    }

    /**
     * 处理电子签名
     */

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
                tv_e_agreement_state.setText("签订");
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
        if (!TextUtils.isEmpty(mESignFileId)){
            tv_e_agreement_state.setText("点击查看");
        }
    }


    private void handlePhotoVideo(VerifyGroup verifyGroup){
        //1：未验证 2：验证通过 3：验证不通过
        switch (verifyGroup.getVerifyState()){
            case "1":
                tv_id_card_holder_video_state.setText("点击采集");
                tv_avatar_state.setText("点击采集");
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
                    if(!TextUtils.isEmpty(mVideoFileId)){
                        tv_id_card_holder_video_state.setText("点击查看");
                    }
                    break;
                case "PHOTO":
                    if (verifyItem.getFileList().size() > 0)
                        mPhotoFileId = verifyItem.getFileList().get(0).getFileId();
                    if (!TextUtils.isEmpty(mPhotoFileId)){
                        tv_avatar_state.setText("点击查看");
                    }
                    break;
                default:

                    break;
            }
        }
    }

    /**
     * 处理六要素
     * @param verifyGroup 验证组合
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
                tv_deal_pwd_state.setEnabled(false);
                tv_id_number_state.setEnabled(false);
                tv_deal_pwd_state.setTextColor(getResources().getColor(R.color.red));
                tv_id_number_state.setTextColor(getResources().getColor(R.color.red));
                break;
            case "3":
                tv_deal_pwd_state.setText("验证不通过");
                tv_id_number_state.setText("验证不通过");
                break;
            default:
                tv_deal_pwd_state.setText("未验证");
                tv_id_number_state.setText("未验证");
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
