package com.example.shoufuyi.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.cchtw.videorecorderlib.utils.FileUtil;
import com.example.shoufuyi.R;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.cache.FileUtils;
import com.example.shoufuyi.cache.v2.CacheManager;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.AlertDialogHelper;
import com.example.shoufuyi.uitls.dialog.ChooseDialogDoClickHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.itech.message.APP_120008;
import com.itech.message.FileMsg;
import com.itech.message.Result_120023;
import com.itech.utils.HashCodeUtils;
import com.itech.utils.ZipDataUtils;
import com.itech.utils.encoder.Base64Utils;
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TakeVideoActivity extends BaseActivity {
    public static final String KEY_FILE_PATH = "file_path";
    private String filePath = "";
    private String mLastfilePath = "";
    private ScalableVideoView mScalableVideoView;
    private ImageView mPlayImageView;
    private ImageView mThumbnailImageView;
    private ImageView iv_video;
    private Button mBtnUpload;
    private Button btn_again;
    private Result_120023 mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_video);
        Intent intent = TakeVideoActivity.this.getIntent();
        Bundle bundle = intent.getExtras();
        mResult = (Result_120023) bundle.get("info");
        assignViews();
        initData();
        setCanBack(true);
    }


    private void assignViews() {
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mScalableVideoView = (ScalableVideoView) findViewById(R.id.video_view);
//        try {
//            // 这个调用是为了初始化mediaplayer并让它能及时和surface绑定
//            mScalableVideoView.setDataSource("");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mPlayImageView = (ImageView) findViewById(R.id.playImageView);
        mThumbnailImageView = (ImageView) findViewById(R.id.thumbnailImageView);
        iv_video = (ImageView) findViewById(R.id.iv_video);
        btn_again = (Button) findViewById(R.id.btn_again);
    }

    private void initData() {
        mBtnUpload.setOnClickListener(this);
        mPlayImageView.setOnClickListener(this);
        mThumbnailImageView.setOnClickListener(this);
        iv_video.setOnClickListener(this);
        btn_again.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(filePath)){
            btn_again.setVisibility(View.GONE);
        }else {
            btn_again.setVisibility(View.VISIBLE);
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
                mScalableVideoView.stop();
                mPlayImageView.setVisibility(View.VISIBLE);
                mThumbnailImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.playImageView:
                try {
                    mScalableVideoView.setDataSource(filePath);
                    mScalableVideoView.setLooping(true);
                    mScalableVideoView.prepare();
                    mScalableVideoView.start();
                    mPlayImageView.setVisibility(View.GONE);
                    mThumbnailImageView.setVisibility(View.GONE);
                } catch (IOException e) {
                    ToastHelper.ShowToast("播放视频异常~");
                }
                break;
            default:
                break;
        }
    }

    private String videoBase64Content = "";
    private void uploadVideo(){
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
        DialogHelper.showProgressDialog(TakeVideoActivity.this, "正在上传...", true, false);
        ApiRequest.requestData(app_120008, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120008 result = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);
                if ("0000".equals(result.getDetailCode())) {
                    ToastHelper.ShowToast("附件上传成功");
                    TakeVideoActivity.this.finish();
                }else {
                    CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo()+"_VIDEO"),videoBase64Content.getBytes(),
                            Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
                    ToastHelper.ShowToast(result.getDetailInfo());

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
                CacheManager.setCache(FileUtils.getCacheKey(mResult.getIdCard(), mResult.getAccountNo()+"_VIDEO"),videoBase64Content.getBytes(),
                        Constant.CACHE_EXPIRE_OND_DAY, CacheManager.TYPE_INTERNAL);
                ToastHelper.ShowToast("已自动保存视频到本地数据库");
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
                mThumbnailImageView.setImageBitmap(getVideoThumbnail(filePath));
                iv_video.setVisibility(View.GONE);
                try {
                    mScalableVideoView.setDataSource(filePath);
                    mScalableVideoView.setLooping(true);
                    mScalableVideoView.prepare();
                    mScalableVideoView.start();
                    mPlayImageView.setVisibility(View.GONE);
                    mThumbnailImageView.setVisibility(View.GONE);
                } catch (IOException e) {
                    ToastHelper.ShowToast("播放视频异常~");
                }
            }
            if (!TextUtils.isEmpty(mLastfilePath)){
                FileUtil.deleteFile(mLastfilePath);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(filePath)) {
            TakeVideoActivity.this.finish();
        } else {
            AlertDialogHelper.showAlertDialog(TakeVideoActivity.this,
                    "提示：", "视频还未上传,是否退出?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            TakeVideoActivity.this.finish();
                        }
                    });
        }
    }

    @Override
    public void goBack(View view) {
        if (TextUtils.isEmpty(filePath)) {
            TakeVideoActivity.this.finish();
        } else {
            AlertDialogHelper.showAlertDialog(TakeVideoActivity.this,
                    "提示：", "视频还未上传,是否退出?", new ChooseDialogDoClickHelper() {

                        @Override
                        public void doClick(DialogInterface dialog,
                                            int which) {
                            TakeVideoActivity.this.finish();
                        }
                    });
        }
    }
}
