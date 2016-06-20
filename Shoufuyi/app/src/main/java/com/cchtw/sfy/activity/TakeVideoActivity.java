package com.cchtw.sfy.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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
import com.cchtw.sfy.cache.FileUtils;
import com.cchtw.sfy.cache.v2.CacheManager;
import com.cchtw.sfy.uitls.Constant;
import com.cchtw.sfy.uitls.FileHelper;
import com.cchtw.sfy.uitls.SharedPreferencesHelper;
import com.cchtw.sfy.uitls.ToastHelper;
import com.cchtw.sfy.uitls.WeakAsyncTask;
import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;
import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;
import com.cchtw.sfy.uitls.dialog.DialogHelper;
import com.itech.message.APP_120008;
import com.itech.message.APP_120028;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.utils.HashCodeUtils;
import com.itech.utils.ZipDataUtils;
import com.itech.utils.encoder.Base64Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TakeVideoActivity extends BaseActivity {
    public static final String KEY_FILE_PATH = "file_path";
    private String filePath = "";
    private String mLastfilePath = "";
//    private ScalableVideoView mScalableVideoView;
    private VideoView mVideoView;
//    private ImageView mPlayImageView;
//    private ImageView mThumbnailImageView;
    private ImageView iv_video;
    private Button mBtnUpload;
    private Button btn_again;
    private Result_120023 mResult;
    private String mVideoFileId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_video);
        Intent intent = TakeVideoActivity.this.getIntent();
        Bundle bundle = intent.getExtras();
        mResult = (Result_120023) bundle.get("info");
        mVideoFileId = bundle.getString("mVideoFileId","");
        assignViews();
        initData();
        setCanBack(true);
        if (!TextUtils.isEmpty(mVideoFileId)){
            mBtnUpload.setVisibility(View.GONE);
            iv_video.setVisibility(View.GONE);
            //先读取缓存
            new ReadCacheTask(TakeVideoActivity.this).execute();
        }
    }

    /**
     * 读取缓存
     *
     */
    static class ReadCacheTask extends WeakAsyncTask<Integer, Void, byte[], TakeVideoActivity> {

        public ReadCacheTask(TakeVideoActivity target) {
            super(target);
        }

        @Override
        protected byte[] doInBackground(TakeVideoActivity target,
                                        Integer... params) {
            if (target == null) {
                return null;
            }
            if (TextUtils.isEmpty(target.getCacheKey())) {
                return null;
            }
            byte[] data = CacheManager.getCache(target.getCacheKey());

            if (data == null) {
                return null;
            }
            return data;
        }

        @Override
        protected void onPostExecute(TakeVideoActivity target,
                                     byte[] result) {
            super.onPostExecute(target, result);
            if (target == null)
                return;
            if (result != null) {
                try {
                    target.executeParserTask(result);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                target.downLoadFile();
            }
        }
    }

    private void executeParserTask( byte[] result){
        String strRead = new String(result);
        strRead = String.copyValueOf(strRead.toCharArray(), 0, result.length);
        if (FileHelper.fileIsExists(strRead)){
                Uri videoUri = Uri.parse(strRead);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();
        }else {//如果没有对应的文件，通过接口获取
            downLoadFile();
        }
    }

    private String getCacheKey(){
        return FileUtils.getCacheKey(mResult.getCreateTime(), "Video");
    }


    private void assignViews() {
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
//        mScalableVideoView = (ScalableVideoView) findViewById(R.id.video_view);
        mVideoView = (VideoView) findViewById(R.id.video_view);
//        try {
//            // 这个调用是为了初始化mediaplayer并让它能及时和surface绑定
//            mScalableVideoView.setDataSource("");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mPlayImageView = (ImageView) findViewById(R.id.playImageView);
//        mThumbnailImageView = (ImageView) findViewById(R.id.thumbnailImageView);
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

//        mPlayImageView.setOnClickListener(this);
//        mThumbnailImageView.setOnClickListener(this);
        iv_video.setOnClickListener(this);
        btn_again.setOnClickListener(this);
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
            case R.id.video_view:
//                mScalableVideoView.stop();
//                mPlayImageView.setVisibility(View.VISIBLE);
//                mThumbnailImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.playImageView:
//                try {
//                    mPlayImageView.setVisibility(View.GONE);
//                    mThumbnailImageView.setVisibility(View.GONE);
//                iv_video.setVisibility(View.GONE);
                Uri videoUri = Uri.parse(filePath);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();
//                    mScalableVideoView.setDataSource(filePath);
//                    mScalableVideoView.setLooping(true);
//                    mScalableVideoView.prepare();
//                    mScalableVideoView.start();
//                    mScalableVideoView.setVisibility(View.VISIBLE);
//                } catch (IOException e) {
//                    ToastHelper.ShowToast("播放视频异常~");
//                }
                break;
            default:
                break;
        }
    }
    private void uploadVideo() {
        UpLoadImageTask downloadTask = new UpLoadImageTask(TakeVideoActivity.this);
        downloadTask.execute();
    }

    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {

        public UpLoadImageTask(Context context) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showProgressDialog(TakeVideoActivity.this, "正在上传...", true, false);
        }

        @Override
        protected APP_120008 doInBackground(APP_120008...APP_120008) {
            // 先调用转换编码的方法将视频mp4文件转化为BASE64编码的字符串
            // 这里调用接口上传视频
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


    private String videoBase64Content = "";
    private void UpLoadAttach(APP_120008 app_120008){
        ApiRequest.requestData(app_120008, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120008 result = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);
                if ("0000".equals(result.getDetailCode())) {
                    ToastHelper.ShowToast("附件上传成功",1);
                    TakeVideoActivity.this.finish();
                }else {
//                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo()+"_VIDEO"),videoBase64Content.getBytes(),
//                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
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
//                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo()+"_VIDEO"),videoBase64Content.getBytes(),
//                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
//                ToastHelper.ShowToast(msg,1);
            }

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
            }
        });
    }

    private static int TAKEVEDIOREQUESTCODE = 80;
    private void takeVideo(){
        Intent intentTack = new Intent(TakeVideoActivity.this, NewRecordVideoActivity.class);
        intentTack.putExtra("VideoName",mResult.getMerchantId()+mResult.getCreateTime());
        startActivityForResult(intentTack, TAKEVEDIOREQUESTCODE);
    }

    /**
     * 获取视频缩略图（这里获取第一帧）
     * @param filePath
     * @return
     */
    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(1));
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKEVEDIOREQUESTCODE && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            filePath = bundle.getString(KEY_FILE_PATH);
            if (TextUtils.isEmpty(filePath)) {
                ToastHelper.ShowToast("视频路径错误");
            }else {
//                mThumbnailImageView.setImageBitmap(getVideoThumbnail(filePath));
                iv_video.setVisibility(View.GONE);
                Uri videoUri = Uri.parse(filePath);
                mVideoView.setVideoURI(videoUri);
                mVideoView.start();

//                try {
//                    mVideoView.setVideoPath(filePath);
//                    mScalableVideoView.setDataSource(filePath);
//                    mScalableVideoView.setLooping(true);
//                    mScalableVideoView.prepare();
//                    mScalableVideoView.start();
//                    mPlayImageView.setVisibility(View.GONE);
//                    mThumbnailImageView.setVisibility(View.GONE);
//                } catch (IOException e) {
//                    ToastHelper.ShowToast("播放视频异常~");
//                }
            }
//            if (!TextUtils.isEmpty(mLastfilePath)){
//                FileUtil.deleteFile(mLastfilePath);
//            }
        }
    }


    private void downLoadFile(){
        APP_120028 app120028 = new APP_120028();
        app120028.setTrxCode("120028");
        app120028.setUserName(SharedPreferencesHelper.getString(Constant.PHONE, ""));
        FileMsg file = new FileMsg();
        file.setFileId(mVideoFileId);
        app120028.setFileMsg(file);
        DialogHelper.showProgressDialog(TakeVideoActivity.this, "正在下载附件...", true, false);
        ApiRequest.requestData(app120028, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
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
                try {
                    String path = Environment
                            .getExternalStorageDirectory()
                            + "/" + "SFY/Video/";
                    File dir = new File(path);
                    if (!dir.exists())
                        dir.mkdirs();
                    String videoName = mResult.getMerchantId()+mResult.getAccountNo()+".mp4";
                    FileHelper.getFile(ZipDataUtils
                                    .unZipForBase64(content),
                            path, videoName);
                    filePath = path + videoName;
                    if (TextUtils.isEmpty(filePath)) {
                        ToastHelper.ShowToast("视频路径错误");
                    }else {
                        Uri videoUri = Uri.parse(filePath);
                        mVideoView.setVideoURI(videoUri);
                        mVideoView.start();
                        if (!TextUtils.isEmpty(filePath)){
                            try {
                                CacheManager.setCache(getCacheKey(), filePath.getBytes("gb2312"),
                                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
//                        mThumbnailImageView.setImageBitmap(getVideoThumbnail(filePath));
//                        iv_video.setVisibility(View.GONE);
//                        mVideoView.setVideoPath(filePath);

//                        try {
//                            mScalableVideoView.setDataSource(filePath);
//                            mScalableVideoView.setLooping(true);
//                            mScalableVideoView.prepare();
//                            mScalableVideoView.start();
//                            mPlayImageView.setVisibility(View.GONE);
//                            mThumbnailImageView.setVisibility(View.GONE);
//                        } catch (IOException e) {
//                            ToastHelper.ShowToast("播放视频异常~");
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
