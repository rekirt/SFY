package com.cchtw.sfy.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.alibaba.fastjson.JSON;
import com.cchtw.sfy.R;
import com.cchtw.sfy.api.ApiRequest;
import com.cchtw.sfy.api.JsonHttpHandler;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.FileHelper;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.cache.ACache;
import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;
import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.cchtw.sfy.uitls.dialog.ProgressDialogDoClickHelper;
import com.itech.message.APP_120008;
import com.itech.message.APP_120028;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.utils.HashCodeUtils;
import com.itech.utils.ZipDataUtils;
import com.itech.utils.encoder.Base64Utils;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TakeVideoActivity extends BaseActivity {
    public static final String KEY_FILE_PATH = "file_path";
    private String filePath = "";
    private String mLastfilePath = "";
    private VideoView mVideoView;
    private ImageView iv_video;
    private Button mBtnUpload;
    private Button btn_again;
    private Result_120023 mResult;
    private String mVideoFileId;
    private String videoBase64Content = "";
    RequestHandle requestHandle;
    private static int TAKEVEDIOREQUESTCODE = 80;
    private RequestHandle mRequestHandleDownload;
    private ACache aCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_video);
        Intent intent = TakeVideoActivity.this.getIntent();
        Bundle bundle = intent.getExtras();
        mResult = (Result_120023) bundle.get("info");
        mVideoFileId = bundle.getString("mVideoFileId","");
        aCache = ACache.get(TakeVideoActivity.this);
        assignViews();
        initData();
        setCanBack(true);
        starToDownload();
    }

    private void assignViews() {
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        iv_video = (ImageView) findViewById(R.id.iv_video);
        btn_again = (Button) findViewById(R.id.btn_again);
    }

    private void initData() {
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        mBtnUpload.setOnClickListener(this);
        iv_video.setOnClickListener(this);
        btn_again.setOnClickListener(this);
    }

    private void starToDownload(){
        if (!TextUtils.isEmpty(mVideoFileId)){
            mBtnUpload.setVisibility(View.GONE);
            iv_video.setVisibility(View.GONE);
            downLoadFile();
        }

        if (!TextUtils.isEmpty(mVideoFileId)){
            mBtnUpload.setVisibility(View.GONE);
            iv_video.setVisibility(View.GONE);
            byte[] bytes = aCache.getAsBinary(mResult.getIdCard() + mVideoFileId);
            if (bytes != null){
                playVideo(bytes);
            }else {
                DialogHelper.showProgressDialog(TakeVideoActivity.this, "正在下载附件，请稍候...", new ProgressDialogDoClickHelper() {
                            @Override
                            public void doClick() {
                                if (mRequestHandleDownload != null) {
                                    mRequestHandleDownload.cancel(true);
                                }
                            }
                        },
                        true, false);
                downLoadFile();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(filePath) && TextUtils.isEmpty(mVideoFileId)){
            btn_again.setVisibility(View.VISIBLE);
        }else {
            btn_again.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View arg0) {
        super.onClick(arg0);
        switch (arg0.getId()) {
            case R.id.btn_upload:
                if (TextUtils.isEmpty(filePath)){
                    ToastHelper.ShowToast("请先拍摄视频。");
                }else{
                    uploadVideo();
                }
                break;
            case R.id.iv_video:
                takeVideo();
                break;
            case R.id.btn_again:
                mLastfilePath = filePath;
                takeVideo();
                break;
            case R.id.playImageView:
                Uri videoUri = Uri.parse(filePath);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();
                break;
            default:
                break;
        }
    }

    private void uploadVideo() {
        DialogHelper.showProgressDialog(TakeVideoActivity.this, "正在上传...", new ProgressDialogDoClickHelper() {
                    @Override
                    public void doClick() {
                        if(requestHandle != null){
                            requestHandle.cancel(true);
                        }
                    }
                },
                true, false);
        UpLoadImageTask downloadTask = new UpLoadImageTask(TakeVideoActivity.this);
        downloadTask.execute();
    }

    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {

        public UpLoadImageTask(Context context) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected APP_120008 doInBackground(APP_120008...APP_120008) {
            final APP_120008 app_120008 = new APP_120008();
            app_120008.setAccountNo(mResult.getAccountNo());
            app_120008.setIdCard(mResult.getIdCard());
            app_120008.setMerchantId(mResult.getMerchantId());
            app_120008.setTrxCode("120008");
            app_120008.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
            app_120008.setVerifyItem("VIDEO");
            List<FileMsg> list = new ArrayList<FileMsg>();
            FileMsg fileMsgFont = new FileMsg();
            try {
                if ("" == videoBase64Content) {
                    videoBase64Content = new String(ZipDataUtils.zipForBase64(Base64Utils.fileToByte(filePath)));
                }
                fileMsgFont.setContent(videoBase64Content);
                fileMsgFont.setFileName("video.mp4");
                fileMsgFont.setIndex("0");
                fileMsgFont.setAttachSecurCode(HashCodeUtils.hashCodeVaule(videoBase64Content.hashCode(),
                        SharedPreferencesHelper.getString(Constant.UUID, "")));
                list.add(fileMsgFont);
            } catch (Exception e) {
                e.printStackTrace();
            }
            app_120008.setFileList(list);
            return app_120008;
        }

        @Override
        protected void onPostExecute(APP_120008 attachPost) {
            UpLoadAttach(attachPost);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(APP_120008 s) {
            super.onCancelled(s);
        }
    }


    private void UpLoadAttach(final APP_120008 app_120008){
        requestHandle =  ApiRequest.requestData(app_120008, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler(TakeVideoActivity.this) {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120008 result = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);
                if ("0000".equals(result.getDetailCode())) {
                    ToastHelper.ShowToast("附件上传成功",1);
                    if (result.getFileList().size()>0){
                        byte[] content = app_120008.getFileList().get(0).getContent().getBytes();
                        aCache.put(mResult.getIdCard() + result.getFileList().get(0).getFileId(), content, ACache.TIME_CACHE);
                    }
                    TakeVideoActivity.this.finish();
                }else {
                    ToastHelper.ShowToast(result.getDetailInfo(),1);
                }
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFail(String msg) {
                ToastHelper.ShowToast("提示:"+msg,1);
            }

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private void takeVideo(){
        Intent intentTack = new Intent(TakeVideoActivity.this, NewRecordVideoActivity.class);
        intentTack.putExtra("VideoName",mResult.getMerchantId()+mResult.getCreateTime());
        startActivityForResult(intentTack, TAKEVEDIOREQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKEVEDIOREQUESTCODE && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            filePath = bundle.getString(KEY_FILE_PATH);
            if (TextUtils.isEmpty(filePath)) {
                ToastHelper.ShowToast("视频路径错误");
            }else {
                iv_video.setVisibility(View.GONE);
                Uri videoUri = Uri.parse(filePath);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();
            }
        }
    }

    private void downLoadFile(){
        APP_120028 app120028 = new APP_120028();
        app120028.setTrxCode("120028");
        app120028.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        FileMsg file = new FileMsg();
        file.setFileId(mVideoFileId);
        app120028.setFileMsg(file);
        mRequestHandleDownload = ApiRequest.requestData(app120028, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler(TakeVideoActivity.this) {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120028 result = JSON.parseObject(responseJsonObject.toString(), APP_120028.class);
                FileMsg fileMsg = result.getFileMsg();
                String contentString = "";
                if (fileMsg == null) {
                    return;
                }
                contentString = result.getFileMsg().getContent();
                byte[] content = contentString.getBytes();
                playVideo(content);
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

    private void playVideo(byte[] content){
        try {
            String path = Environment.getExternalStorageDirectory() + "/" + "SFY/Video/";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            String videoName = mResult.getMerchantId()+mResult.getAccountNo()+".mp4";
            FileHelper.getFile(ZipDataUtils.unZipForBase64(content), path, videoName);
            filePath = path + videoName;
            if (TextUtils.isEmpty(filePath)) {
                ToastHelper.ShowToast("视频路径错误");
                aCache.remove(mResult.getIdCard() + mVideoFileId);
            }else {
                Uri videoUri = Uri.parse(filePath);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();
                aCache.put(mResult.getIdCard() + mVideoFileId, content, ACache.TIME_CACHE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            aCache.remove(mResult.getIdCard() + mVideoFileId);
        }
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(filePath) && TextUtils.isEmpty(mVideoFileId)){
            AlertDialogHelper.showAlertDialog(TakeVideoActivity.this,
                    "提示：", "视频还未上传,是否退出?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            TakeVideoActivity.this.finish();
                        }
                    });
        }else {
            TakeVideoActivity.this.finish();
        }
    }

    @Override
    public void goBack(View view) {
       if (!TextUtils.isEmpty(filePath) && TextUtils.isEmpty(mVideoFileId)){
            AlertDialogHelper.showAlertDialog(TakeVideoActivity.this,
                    "提示：", "视频还未上传,是否退出?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            TakeVideoActivity.this.finish();
                        }
                    });
        }else {
           TakeVideoActivity.this.finish();
       }
    }
}
