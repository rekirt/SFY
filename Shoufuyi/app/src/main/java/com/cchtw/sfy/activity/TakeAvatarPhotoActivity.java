package com.cchtw.sfy.activity;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.net.Uri;import android.os.AsyncTask;import android.os.Bundle;import android.os.Environment;import android.provider.MediaStore;import android.text.TextUtils;import android.view.View;import android.widget.Button;import android.widget.ImageView;import com.alibaba.fastjson.JSON;import com.cchtw.sfy.R;import com.cchtw.sfy.api.ApiRequest;import com.cchtw.sfy.api.JsonHttpHandler;import com.cchtw.sfy.cache.FileUtils;import com.cchtw.sfy.cache.v2.CacheManager;import com.cchtw.sfy.uitls.Constant;import com.cchtw.sfy.uitls.ImageHelper;import com.cchtw.sfy.uitls.SharedPreferencesHelper;import com.cchtw.sfy.uitls.ToastHelper;import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;import com.cchtw.sfy.uitls.dialog.DialogHelper;import com.itech.message.APP_120008;import com.itech.message.APP_120028;import com.itech.message.FileMsg;import com.itech.message.Result_120023;import com.itech.utils.HashCodeUtils;import com.itech.utils.ZipDataUtils;import com.itech.utils.encoder.Base64Utils;import org.json.JSONArray;import org.json.JSONObject;import java.io.File;import java.util.ArrayList;import java.util.List;/** * * Description:用于持卡人照片 Created by Fu.H.L on Date:2015-9-22-上午12:52:34 * Copyright © 2015年 Fu.H.L All rights reserved. */public class TakeAvatarPhotoActivity extends BaseActivity {    private Result_120023 mResult;    public static final String KEY_FILE_PATH1 = "pic_file_path1";    public static final String KEY_FILE_PATH2 = "pic_file_path2";    private String mAvatarFilePath = "";    private String mPhotoFileId;    protected void onCreate(Bundle savedInstanceState) {		super.onCreate(savedInstanceState);		setContentView(R.layout.activity_take_avatar);        Intent intent = TakeAvatarPhotoActivity.this.getIntent();        Bundle bundle = intent.getExtras();        mResult = (Result_120023) bundle.get("info");        mPhotoFileId = bundle.getString("mPhotoFileId","");        assignViews();		initData();        setCanBack(true);        if (!TextUtils.isEmpty(mPhotoFileId)){            downLoadFile(mPhotoFileId);            mBtnUpload.setVisibility(View.GONE);        }	}	private ImageView iv_avatar;	private Button mBtnUpload;	private void assignViews() {        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);		mBtnUpload = (Button) findViewById(R.id.btn_upload);	}	private void initData() {		mBtnUpload.setOnClickListener(this);        iv_avatar.setOnClickListener(this);	}	@Override	public void onClick(View arg0) {        super.onClick(arg0);		switch (arg0.getId()) {            case R.id.btn_upload:                if (TextUtils.isEmpty(mAvatarFilePath)) {                    ToastHelper.ShowToast("请先拍所需照片");                } else {                    DialogHelper.showProgressDialog(TakeAvatarPhotoActivity.this, "正在上传...", true, false);                    upLoadImage();                }                break;            case R.id.iv_avatar:                selectPicFromCamera();                break;            default:                break;		}	}    private void downLoadFile(String fileId){        APP_120028 app120028 = new APP_120028();        app120028.setTrxCode("120028");        app120028.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));        FileMsg file = new FileMsg();        file.setFileId(fileId);        app120028.setFileMsg(file);        DialogHelper.showProgressDialog(TakeAvatarPhotoActivity.this, "正在下载附件...", true, false);        ApiRequest.requestData(app120028, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                APP_120028 result = JSON.parseObject(responseJsonObject.toString(), APP_120028.class);                FileMsg fileMsg = result.getFileMsg();                String contentString = "";                if (fileMsg == null) {                    return;                }                contentString = result.getFileMsg().getContent();                byte[] content = contentString.getBytes();                Bitmap bitmap = BitmapFactory.decodeByteArray(ZipDataUtils.unZipForBase64(content),                        0, ZipDataUtils.unZipForBase64(content).length);                iv_avatar.setImageBitmap(bitmap);            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFinish() {                DialogHelper.dismissProgressDialog();            }        });    }    /**     * 照相获取图片     */    private File cameraFile;    public String path;    public String imageName;    public static final int REQUEST_CODE_CAMERA = 18;    public void selectPicFromCamera() {        path = Environment.getExternalStorageDirectory() + "/"+"SFY/PIC/";        File dir = new File(path);        if(!dir.exists())            dir.mkdirs();        imageName = System.currentTimeMillis()+".jpg";        cameraFile=new File(dir, imageName);            //localTempImgDir和localTempImageFileName是自己定义的名字        Uri uri=Uri.fromFile(cameraFile);        startActivityForResult(                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),                REQUEST_CODE_CAMERA);    }    private void upLoadImage() {        UpLoadImageTask downloadTask = new UpLoadImageTask(TakeAvatarPhotoActivity.this);        downloadTask.execute();    }    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {        public UpLoadImageTask(Context context) {        }        @Override        protected void onPreExecute() {            super.onPreExecute();            DialogHelper.showProgressDialog(TakeAvatarPhotoActivity.this, "正在上传...", true, false);        }        @Override        protected APP_120008 doInBackground(APP_120008...APP_120008) {            APP_120008 attachPost = new APP_120008();            attachPost.setAccountNo(mResult.getAccountNo());            attachPost.setIdCard(mResult.getIdCard());            attachPost.setMerchantId(mResult.getMerchantId());            attachPost.setTrxCode("120008");            attachPost.setUserName(SharedPreferencesHelper.getString(Constant.PHONE,""));            List<FileMsg> list = new ArrayList<FileMsg>();            FileMsg fileMsgFont = new FileMsg();            attachPost.setVerifyItem("PHOTO");            try {                if ("" == frontBase64Content) {                    frontBase64Content = new String(                            ZipDataUtils.zipForBase64(Base64Utils                                    .fileToByte(mAvatarFilePath)));                }                fileMsgFont.setContent(frontBase64Content);                fileMsgFont.setFileName("人像照片.jpg");                fileMsgFont.setIndex("0");                fileMsgFont.setAttachSecurCode(HashCodeUtils.hashCodeVaule(                        frontBase64Content.hashCode(),                        SharedPreferencesHelper.getString(Constant.UUID, "")));                list.add(fileMsgFont);            } catch (Exception e) {                e.printStackTrace();            }            attachPost.setFileList(list);            return attachPost;        }        @Override        protected void onPostExecute(APP_120008 attachPost) {            UpLoadAttach(attachPost);        }        @Override        protected void onProgressUpdate(Integer... values) {        }        @Override        protected void onCancelled() {            super.onCancelled();        }        @Override        protected void onCancelled(APP_120008 s) {            super.onCancelled(s);        }    }    private String frontBase64Content = "";    private String backBase64Content = "";    private void UpLoadAttach(final APP_120008 post) {        ApiRequest.requestData(post, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                APP_120008 mReturnApp = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);                if ("0000".equals(mReturnApp.getDetailCode())) {                    ToastHelper.ShowToast("上传成功");                    TakeAvatarPhotoActivity.this.finish();                } else {                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO1"), frontBase64Content.getBytes(),                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO2"), backBase64Content.getBytes(),                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                    ToastHelper.ShowToast("提示：" + mReturnApp.getDetailInfo() + ",已自动保存到本地数据库");                }            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFail(String msg) {                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO1"), frontBase64Content.getBytes(),                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo() + "_BANK_CARD_PHOTO2"), backBase64Content.getBytes(),                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);                ToastHelper.ShowToast("提示:" + msg + ",已自动保存到本地数据库");            }            @Override            public void onFinish() {                DialogHelper.dismissProgressDialog();            }        });    }	@Override	protected void onDestroy() {		super.onDestroy();	}    private Uri curUrl;    @Override    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        switch (requestCode) {            case REQUESTCODE_CUTTING:                if (data != null) {                    setPicToView(data);                }                break;            case REQUEST_CODE_CAMERA:                if (cameraFile != null && cameraFile.exists()){                    curUrl = Uri.fromFile(cameraFile);                    startPhotoZoom(curUrl);                }                break;            default:                break;        }    }    private String picPath = "";    private String picName = "";    private void setPicToView(Intent picdata) {        Bundle extras = picdata.getExtras();        if (extras != null) {            Bitmap photo = extras.getParcelable("data");            picPath = Environment.getExternalStorageDirectory().toString() + "/"+"SFY/PIC";            picName = System.currentTimeMillis()+".jpg";            mAvatarFilePath = picPath+"/"+picName;            if (photo != null){                if (ImageHelper.saveBmpToSd(photo, picPath, picName)){                    iv_avatar.setImageBitmap(photo);                }            }        }    }    private static final int REQUESTCODE_CUTTING = 2;    public void startPhotoZoom(Uri uri) {        Intent intent = new Intent("com.android.camera.action.CROP");        intent.setDataAndType(uri, "image/*");        intent.putExtra("crop", true);        intent.putExtra("aspectX", 1);        intent.putExtra("aspectY", 1);        intent.putExtra("outputX", 300);        intent.putExtra("outputY", 300);        intent.putExtra("return-data", true);        intent.putExtra("noFaceDetection", true);        startActivityForResult(intent, REQUESTCODE_CUTTING);    }    @Override    public void onBackPressed() {        if (TextUtils.isEmpty(mAvatarFilePath)) {            TakeAvatarPhotoActivity.this.finish();        } else {            AlertDialogHelper.showAlertDialog(TakeAvatarPhotoActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeAvatarPhotoActivity.this.finish();                        }                    });        }    }    @Override    public void goBack(View view) {        if (TextUtils.isEmpty(mAvatarFilePath)) {            TakeAvatarPhotoActivity.this.finish();        } else {            AlertDialogHelper.showAlertDialog(TakeAvatarPhotoActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeAvatarPhotoActivity.this.finish();                        }                    });        }    }}