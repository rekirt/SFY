package com.cchtw.sfy.activity;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.os.AsyncTask;import android.os.Bundle;import android.text.TextUtils;import android.view.View;import android.widget.Button;import android.widget.ImageView;import com.alibaba.fastjson.JSON;import com.cchtw.sfy.R;import com.cchtw.sfy.api.ApiRequest;import com.cchtw.sfy.api.JsonHttpHandler;import com.cchtw.sfy.cache.FileUtils;import com.cchtw.sfy.cache.v2.CacheManager;import com.cchtw.sfy.uitls.BitmapHelper;import com.cchtw.sfy.uitls.Constant;import com.cchtw.sfy.uitls.ImageHelper;import com.cchtw.sfy.uitls.SharedPreferencesHelper;import com.cchtw.sfy.uitls.ToastHelper;import com.cchtw.sfy.uitls.WeakAsyncTask;import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;import com.cchtw.sfy.uitls.dialog.DialogHelper;import com.itech.message.APP_120008;import com.itech.message.APP_120028;import com.itech.message.FileMsg;import com.itech.message.Result_120023;import com.itech.utils.HashCodeUtils;import com.itech.utils.ZipDataUtils;import com.itech.utils.encoder.Base64Utils;import org.json.JSONArray;import org.json.JSONObject;import java.io.File;import java.io.IOException;import java.io.UnsupportedEncodingException;import java.util.ArrayList;import java.util.List;/** * * Description:用于采集银行卡照片 Created by Fu.H.L on Date:2015-9-22-上午12:52:34 * Copyright © 2015年 Fu.H.L All rights reserved. */public class TakeBankCardPhotoActivity extends BaseActivity {    private String returnAction = "android.intent.action.MAIN";    private String resultAction = "";    private Result_120023 mResult;    public static final String KEY_FILE_PATH1 = "pic_file_path1";    public static final String KEY_FILE_PATH2 = "pic_file_path2";    private String mBankCardFrontFilePath = "";    private String mBankCardBackFilePath = "";    private String mFrontBankCardFileId;    private String mBackBankCardFileId;    private static final int TAKE_PICTURE1 = 0x000001;    private static final int TAKE_PICTURE2 = 0x000002;    protected void onCreate(Bundle savedInstanceState) {		super.onCreate(savedInstanceState);		setContentView(R.layout.activity_take_bankpic);        Intent intent = TakeBankCardPhotoActivity.this.getIntent();        Bundle bundle = intent.getExtras();        mResult = (Result_120023) bundle.get("info");        mFrontBankCardFileId = bundle.getString("mFrontBankCardFileId", "");        mBackBankCardFileId = bundle.getString("mBackBankCardFileId", "");		assignViews();		initData();        setCanBack(true);        if (!TextUtils.isEmpty(mFrontBankCardFileId) && !TextUtils.isEmpty(mBackBankCardFileId)){            mBtnUpload.setVisibility(View.GONE);            mIvBack.setEnabled(false);            mIvFront.setEnabled(false);            //先读取缓存            new ReadCacheTask(TakeBankCardPhotoActivity.this).execute();        }	}    /**     * 读取缓存     *     */    static class ReadCacheTask extends WeakAsyncTask<Integer, Void, byte[], TakeBankCardPhotoActivity> {        public ReadCacheTask(TakeBankCardPhotoActivity target) {            super(target);        }        @Override        protected byte[] doInBackground(TakeBankCardPhotoActivity target,                                        Integer... params) {            if (target == null) {                return null;            }            if (TextUtils.isEmpty(target.getCacheKey())) {                return null;            }            byte[] data = CacheManager.getCache(target.getCacheKey());            if (data == null) {                return null;            }            return data;        }        @Override        protected void onPostExecute(TakeBankCardPhotoActivity target,                                     byte[] result) {            super.onPostExecute(target, result);            if (target == null)                return;            if (result != null) {                try {                    target.executeParserTask(result);                    return;                } catch (Exception e) {                    e.printStackTrace();                }            }else{                target.downLoadFile(1);                target.downLoadFile(2);            }        }    }    private void executeParserTask( byte[] result){        String strRead = new String(result);        strRead = String.copyValueOf(strRead.toCharArray(), 0, result.length);        String[] path =strRead.split(":");        if (fileIsExists(path[0]) && fileIsExists(path[1])){            ImageHelper.displayImage("file://" + path[0], mIvFront);            ImageHelper.displayImage("file://" + path[1], mIvBack);        }else {//如果没有对应的文件，通过接口获取            downLoadFile(1);            downLoadFile(2);        }    }    public boolean fileIsExists(String path){        File f=new File(path);        if(!f.exists()){            return false;        }        return true;    }	private ImageView mIvFront;	private ImageView mIvBack;	private Button mBtnUpload;	private void assignViews() {		mIvFront = (ImageView) findViewById(R.id.iv_front);		mIvBack = (ImageView) findViewById(R.id.iv_back);		mBtnUpload = (Button) findViewById(R.id.btn_upload);	}	private void initData() {		mBtnUpload.setOnClickListener(this);		mIvBack.setOnClickListener(this);		mIvFront.setOnClickListener(this);	}    private int mDownloadCount = 0;    private void downLoadFile(final int index){        APP_120028 app120028 = new APP_120028();        app120028.setTrxCode("120028");        app120028.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));        FileMsg file = new FileMsg();        if (index ==1){            file.setFileId(mFrontBankCardFileId);        }else {            file.setFileId(mBackBankCardFileId);        }        app120028.setFileMsg(file);        if (index == 1){            DialogHelper.showProgressDialog(TakeBankCardPhotoActivity.this, "正在下载附件...", true, false);        }        ApiRequest.requestData(app120028, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                APP_120028 result = JSON.parseObject(responseJsonObject.toString(), APP_120028.class);                FileMsg fileMsg = result.getFileMsg();                String contentString = "";                if (fileMsg == null) {                    return;                }                contentString = result.getFileMsg().getContent();                byte[] content = contentString.getBytes();                Bitmap bitmap = BitmapFactory.decodeByteArray(ZipDataUtils.unZipForBase64(content),                        0, ZipDataUtils.unZipForBase64(content).length);                if (index == 1) {                    mIvFront.setImageBitmap(bitmap);                    try {                        mBankCardFrontFilePath = BitmapHelper.saveBitmap(bitmap, mResult.getCreateTime() + "BANKCard1");                    } catch (IOException e) {                        e.printStackTrace();                    }                } else {                    mIvBack.setImageBitmap(bitmap);                    try {                        mBankCardBackFilePath = BitmapHelper.saveBitmap(bitmap, mResult.getCreateTime() + "BANKCard2");                    } catch (IOException e) {                        e.printStackTrace();                    }                    if (!TextUtils.isEmpty(mBankCardFrontFilePath) && !TextUtils.isEmpty(mBankCardBackFilePath) ){                        try {                            CacheManager.setCache(getCacheKey(), (mBankCardFrontFilePath+":"+mBankCardBackFilePath).getBytes("gb2312"),                                    Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                        } catch (UnsupportedEncodingException e) {                            e.printStackTrace();                        }                    }                }                mDownloadCount++;            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFinish() {                if (mDownloadCount == 2) {                    DialogHelper.dismissProgressDialog();                }            }        });    }    private String getCacheKey(){        return FileUtils.getCacheKey(mResult.getCreateTime(), "BANKCard");    }	@Override	public void onClick(View arg0) {        super.onClick(arg0);		switch (arg0.getId()) {            case R.id.btn_upload:                if (TextUtils.isEmpty(mBankCardFrontFilePath) || TextUtils.isEmpty(mBankCardBackFilePath)) {                    ToastHelper.ShowToast("请先拍完所需照片");                } else {                    upLoadImage();                }                break;            case R.id.iv_front:                takeBankCardPhoto(TAKE_PICTURE1);                break;            case R.id.iv_back:				takeBankCardPhoto(TAKE_PICTURE2);                break;            default:                break;		}	}    private void upLoadImage() {        UpLoadImageTask downloadTask = new UpLoadImageTask(TakeBankCardPhotoActivity.this);        downloadTask.execute();    }    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {        public UpLoadImageTask(Context context) {        }        @Override        protected void onPreExecute() {            super.onPreExecute();            DialogHelper.showProgressDialog(TakeBankCardPhotoActivity.this, "正在上传...", true, false);        }        @Override        protected APP_120008 doInBackground(APP_120008...APP_120008) {            APP_120008 attachPost = new APP_120008();            attachPost.setAccountNo(mResult.getAccountNo());            attachPost.setIdCard(mResult.getIdCard());            attachPost.setMerchantId(mResult.getMerchantId());            attachPost.setTrxCode("120008");            attachPost.setUserName(SharedPreferencesHelper.getString(Constant.PHONE,""));            List<FileMsg> list = new ArrayList<FileMsg>();            FileMsg fileMsgFont = new FileMsg();            FileMsg fileMsgBack = new FileMsg();            attachPost.setVerifyItem("BANK_CARD_PHOTO");            try {                if ("" == frontBase64Content) {                    frontBase64Content = new String(                            ZipDataUtils.zipForBase64(Base64Utils                                    .fileToByte(mBankCardFrontFilePath)));                }                fileMsgFont.setContent(frontBase64Content);                fileMsgFont.setFileName("正面");                fileMsgFont.setIndex("0");                fileMsgFont.setAttachSecurCode(HashCodeUtils.hashCodeVaule(                        frontBase64Content.hashCode(),                        SharedPreferencesHelper.getString(Constant.UUID, "")));                list.add(fileMsgFont);                if ("" == backBase64Content) {                    backBase64Content = new String(                            ZipDataUtils.zipForBase64(Base64Utils                                    .fileToByte(mBankCardBackFilePath)));                }                fileMsgBack.setContent(backBase64Content);                fileMsgBack.setFileName("反面");                fileMsgBack.setIndex("1");                fileMsgBack.setAttachSecurCode(HashCodeUtils.hashCodeVaule(                        backBase64Content.hashCode(),                        SharedPreferencesHelper.getString(Constant.UUID, "")));                list.add(fileMsgBack);            } catch (Exception e) {                e.printStackTrace();            }            attachPost.setFileList(list);            return attachPost;        }        @Override        protected void onPostExecute(APP_120008 attachPost) {            UpLoadAttach(attachPost);        }        @Override        protected void onProgressUpdate(Integer... values) {        }        @Override        protected void onCancelled() {            super.onCancelled();        }        @Override        protected void onCancelled(APP_120008 s) {            super.onCancelled(s);        }    }    private String frontBase64Content = "";    private String backBase64Content = "";    private void UpLoadAttach(final APP_120008 post) {        ApiRequest.requestData(post, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                APP_120008 mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);                if ("0000".equals(mReturnApp.getDetailCode())) {                    ToastHelper.ShowToast("上传成功",1);                    TakeBankCardPhotoActivity.this.finish();                } else {//                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO1"), frontBase64Content.getBytes(),//                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);//                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO2"), backBase64Content.getBytes(),//                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                    ToastHelper.ShowToast("提示："+mReturnApp.getDetailInfo(),1);                }            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFail(String msg) {//                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO1"), frontBase64Content.getBytes(),//                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);//                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO2"), backBase64Content.getBytes(),//                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                ToastHelper.ShowToast("提示:"+msg,1);            }            @Override            public void onFinish() {                DialogHelper.dismissProgressDialog();            }        });    }    private void takeBankCardPhoto(int requestCode){        Intent intentTack = new Intent(TakeBankCardPhotoActivity.this, CardScanCameraActivity.class);        intentTack.putExtra("devCode", Constant.devcode);        intentTack.putExtra("CopyrightInfo", "");        intentTack.putExtra("ReturnAciton", returnAction);        intentTack.putExtra("ResultAciton", resultAction);        startActivityForResult(intentTack, requestCode);    }	@Override	protected void onDestroy() {		super.onDestroy();	}    @Override    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        if (requestCode == TAKE_PICTURE1 && resultCode == RESULT_OK){            mBankCardFrontFilePath = data.getStringExtra("Path");            if (TextUtils.isEmpty(mBankCardFrontFilePath)) {                ToastHelper.ShowToast("图片路径错误");            }else {//                ImageHelper.displayImage("file://" + mBankCardFrontFilePath, mIvFront);                int l = data.getIntExtra("l", -1);                int t = data.getIntExtra("t", -1);                int w = data.getIntExtra("w", -1);                int h = data.getIntExtra("h", -1);                Bitmap bitmap = null;                try {                    bitmap = BitmapFactory.decodeFile(mBankCardFrontFilePath);                    bitmap = Bitmap.createBitmap(bitmap, l, t, w, h);                } catch (Exception e) {                    e.printStackTrace();                }                if (bitmap != null) {                    mIvFront.setImageBitmap(bitmap);                }            }        }else if (requestCode == TAKE_PICTURE2 && resultCode == RESULT_OK){            Bundle bundle = data.getExtras();            mBankCardBackFilePath = bundle.getString("Path");            if (TextUtils.isEmpty(mBankCardBackFilePath)) {                ToastHelper.ShowToast("图片路径错误");            }else {//                ImageHelper.displayImage("file://" + mBankCardBackFilePath, mIvBack);                int l = data.getIntExtra("l", -1);                int t = data.getIntExtra("t", -1);                int w = data.getIntExtra("w", -1);                int h = data.getIntExtra("h", -1);                Bitmap bitmap = null;                try {                    bitmap = BitmapFactory.decodeFile(mBankCardBackFilePath);                    bitmap = Bitmap.createBitmap(bitmap, l, t, w, h);                } catch (Exception e) {                    e.printStackTrace();                }                if (bitmap != null) {                    mIvBack.setImageBitmap(bitmap);                }            }        }    }    @Override    public void onBackPressed() {        if (TextUtils.isEmpty(mBankCardFrontFilePath) && TextUtils.isEmpty(mBankCardBackFilePath)) {            TakeBankCardPhotoActivity.this.finish();        } else if (!TextUtils.isEmpty(mFrontBankCardFileId) && !TextUtils.isEmpty(mBackBankCardFileId)){            TakeBankCardPhotoActivity.this.finish();        }else {            AlertDialogHelper.showAlertDialog(TakeBankCardPhotoActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeBankCardPhotoActivity.this.finish();                        }                    });        }    }    @Override    public void goBack(View view) {        if (TextUtils.isEmpty(mBankCardFrontFilePath) && TextUtils.isEmpty(mBankCardBackFilePath)) {            TakeBankCardPhotoActivity.this.finish();        } else if (!TextUtils.isEmpty(mFrontBankCardFileId) && !TextUtils.isEmpty(mBackBankCardFileId)){            TakeBankCardPhotoActivity.this.finish();        }else {            AlertDialogHelper.showAlertDialog(TakeBankCardPhotoActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeBankCardPhotoActivity.this.finish();                        }                    });        }    }}