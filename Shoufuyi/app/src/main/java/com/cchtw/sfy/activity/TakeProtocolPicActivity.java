package com.cchtw.sfy.activity;import android.app.AlertDialog;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.net.Uri;import android.os.AsyncTask;import android.os.Bundle;import android.os.Environment;import android.provider.MediaStore;import android.support.v4.view.ViewPager;import android.view.View;import android.view.ViewGroup;import android.view.ViewGroup.LayoutParams;import android.view.Window;import android.widget.Button;import android.widget.RelativeLayout;import android.widget.TextView;import com.alibaba.fastjson.JSON;import com.cchtw.sfy.R;import com.cchtw.sfy.api.ApiRequest;import com.cchtw.sfy.api.JsonHttpHandler;import com.cchtw.sfy.uitls.BitmapHelper;import com.cchtw.sfy.uitls.Constant;import com.cchtw.sfy.uitls.SharedPreferencesHelper;import com.cchtw.sfy.uitls.ToastHelper;import com.cchtw.sfy.uitls.dialog.AlertDialogHelper;import com.cchtw.sfy.uitls.dialog.ChooseDialogDoClickHelper;import com.cchtw.sfy.uitls.dialog.DialogHelper;import com.cchtw.sfy.uitls.dialog.ProgressDialogDoClickHelper;import com.cchtw.sfy.uitls.view.photo.Bimp;import com.cchtw.sfy.uitls.view.photo.MyPageAdapter;import com.cchtw.sfy.uitls.view.photo.PhotoView;import com.cchtw.sfy.uitls.view.photo.PicChamfer;import com.cchtw.sfy.uitls.view.photo.ViewPagerFixed;import com.itech.message.APP_120008;import com.itech.message.APP_120028;import com.itech.message.FileMsg;import com.itech.message.Result_120023;import com.itech.utils.HashCodeUtils;import com.itech.utils.ZipDataUtils;import com.itech.utils.encoder.Base64Utils;import com.loopj.android.http.RequestHandle;import org.json.JSONArray;import org.json.JSONObject;import java.io.File;import java.io.IOException;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.Date;import java.util.List;import cz.msebera.android.httpclient.Header;/** *  * Description: Created by Fu.H.L on Date:2015-9-20-上午12:59:11 Copyright © 2015年 * Fu.H.L All rights reserved. */public class TakeProtocolPicActivity extends BaseActivity {	private Result_120023 result;	private ArrayList<String> mArrayListProtocolFileId;    private String mPhoneNumber;    @Override	protected void onCreate(Bundle savedInstanceState) {		super.onCreate(savedInstanceState);		requestWindowFeature(Window.FEATURE_NO_TITLE);		setContentView(R.layout.activity_protocol);        mPhoneNumber = SharedPreferencesHelper.getString(Constant.PHONE,"");        result = (Result_120023) getIntent().getSerializableExtra("result");        mArrayListProtocolFileId = getIntent().getStringArrayListExtra("mArrayListProtocolFileId");		initView();		initData();	}	private ViewPagerFixed mViewPagerFixed;	private Button mBtnPic;	private Button mBtnOk;	private RelativeLayout mBottomLayout;    private TextView tv_page;	private void initView() {		mBtnPic = (Button) findViewById(R.id.btn_pic);		mBtnOk = (Button) findViewById(R.id.btn_ok);		mViewPagerFixed = (ViewPagerFixed) findViewById(R.id.view_pager_fixed);		mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);        tv_page = (TextView) findViewById(R.id.tv_page);        tv_page.setText("");	}	private MyPageAdapter myPageAdapter;	private ArrayList<View> listViews = null;	private void initData() {		mBtnPic.setOnClickListener(this);		mBtnOk.setOnClickListener(this);        listViews = new ArrayList<View>();        myPageAdapter = new MyPageAdapter(listViews);        mViewPagerFixed.setOnPageChangeListener(pageChangeListener);        mViewPagerFixed.setAdapter(myPageAdapter);        if (mArrayListProtocolFileId.size()>0) {            for (int i = 0; i < mArrayListProtocolFileId.size(); i++) {                if (i==0){                    DialogHelper.showProgressDialog(TakeProtocolPicActivity.this, "正在下载附件，请稍候...", new ProgressDialogDoClickHelper() {                                @Override                                public void doClick() {                                    if (mRequestHandleDownload != null) {                                        mRequestHandleDownload.cancel(true);                                    }                                }                            },                            true, false);                }                if (i == mArrayListProtocolFileId.size() - 1) {                    downLoadAttach(i,mArrayListProtocolFileId.get(i), true);                } else {                    downLoadAttach(i,mArrayListProtocolFileId.get(i), false);                }            }            mBottomLayout.setVisibility(View.INVISIBLE);        }        mViewPagerFixed.setLongClickable(true);        setCanBack(true);	}    private void changeViews(ArrayList<String> listViewsPath) {		listViews.clear();		if (listViewsPath != null && !(listViewsPath.isEmpty())) {			for (int i = 0; i < listViewsPath.size(); i++) {				PicChamfer.rotatePic(new File(listViewsPath.get(i)));				try {					Bitmap bm = Bimp.revitionImageSize(listViewsPath.get(i));					PhotoView img = new PhotoView(this);					img.setBackgroundColor(0xff000000);                    img.setImageBitmap(bm);					img.setLayoutParams(new ViewGroup.LayoutParams(							LayoutParams.MATCH_PARENT,							LayoutParams.MATCH_PARENT));					listViews.add(img);				} catch (IOException e) {					e.printStackTrace();				}			}		}		myPageAdapter.setListViews(listViews);        DialogHelper.dismissProgressDialog();	}	// 当前的位置	private int location = 0;	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {		public void onPageSelected(int arg0) {			location = arg0;            tv_page.setText((location+1)+"/"+listViews.size());        }		public void onPageScrolled(int arg0, float arg1, int arg2) {		}		public void onPageScrollStateChanged(int arg0) {		}	};	public APP_120028 mResult;	public boolean isSuccess = false;    private RequestHandle mRequestHandleDownload;    private int sum = 0;	synchronized public void downLoadAttach(final int index,final String fileId, final boolean isNotiChangeView) {        APP_120028 appFront = new APP_120028();        appFront.setTrxCode("120028");        appFront.setUserName(mPhoneNumber);        FileMsg file = new FileMsg();        file.setFileId(fileId);        appFront.setFileMsg(file);        mRequestHandleDownload = ApiRequest.requestData(appFront, mPhoneNumber, new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                sum= sum+1;                mResult = JSON.parseObject(responseJsonObject.toString(), APP_120028.class);                if ("0000".equals(mResult.getRetCode())) {                    try {                        byte[] content = mResult.getFileMsg().getContent().getBytes();                        Bitmap bitmap = BitmapFactory.decodeByteArray(                                ZipDataUtils.unZipForBase64(content), 0,                                ZipDataUtils.unZipForBase64(content).length);                        listViewsPath.add(index,BitmapHelper.saveBitmap(bitmap,getTimeName(System.currentTimeMillis())));                        if (sum == mArrayListProtocolFileId.size()) {                            changeViews(listViewsPath);                        }                    }catch (IOException e) {                        e.printStackTrace();                    }                } else {                    ToastHelper.ShowToast(mResult.getErrMsg());                }            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {                super.onFailure(statusCode, headers, responseBody, error);            }            @Override            public void onFinish() {                if (sum == mArrayListProtocolFileId.size()) {                    DialogHelper.dismissProgressDialog();                }            }        });	}	@Override	public void onClick(View view) {		switch (view.getId()) {		case R.id.btn_pic:			photo();			break;		case R.id.btn_ok:// 提交			if (listViewsPath != null && listViewsPath.size() > 0) {				upLoadImage();			} else {				ToastHelper.ShowToast("请先拍摄协议影像照片");			}			break;		case R.id.img_right:            remove();			break;		default:			break;		}	}    private void remove(){        if (listViews.size() > 0) {            mViewPagerFixed.removeAllViews();            listViews.remove(location);            listViewsPath.remove(location);            if (listViews.size() > 0) {                myPageAdapter.setListViews(listViews);                myPageAdapter.notifyDataSetChanged();            }else {                mBtnPic.setText("拍照");            }            tv_page.setText((location+1)+"/"+listViews.size());        }    }	private String path;	private String imageName;	private static final int TAKE_PICTURE = 0x000001;	public void photo() {		String state = Environment.getExternalStorageState();		path = Environment.getExternalStorageDirectory() + "/" + "SFY/PIC/";		File dir = new File(path);		if (!dir.exists())			dir.mkdirs();		imageName = getTimeName(System.currentTimeMillis()) + ".jpg";		if (state.equals(Environment.MEDIA_MOUNTED)) {			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);			File f = new File(dir, imageName); // localTempImgDir和localTempImageFileName是自己定义的名字			Uri uri = Uri.fromFile(f);			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);			intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 2);			startActivityForResult(intent, TAKE_PICTURE);		}	}    private void upLoadImage() {        UpLoadImageTask downloadTask = new UpLoadImageTask(TakeProtocolPicActivity.this);        downloadTask.execute();    }    class UpLoadImageTask extends AsyncTask<APP_120008,Integer,APP_120008> {        public UpLoadImageTask(Context context) {        }        @Override        protected void onPreExecute() {            super.onPreExecute();            DialogHelper.showProgressDialog(TakeProtocolPicActivity.this, "正在操作，请稍候...", new ProgressDialogDoClickHelper() {                        @Override                        public void doClick() {                            if(requestHandle != null){                                requestHandle.cancel(true);                            }                        }                    },                    true, false);        }        @Override        protected APP_120008 doInBackground(APP_120008...APP_120008) {            APP_120008 attachPost = new APP_120008();            attachPost.setAccountNo(result.getAccountNo());            attachPost.setIdCard(result.getIdCard());            attachPost.setMerchantId(result.getMerchantId());            attachPost.setTrxCode("120008");            attachPost.setUserName(mPhoneNumber);            attachPost.setVerifyItem("PAPER_PROTOCOL");            List<FileMsg> list = new ArrayList<FileMsg>();            for (int i = 0; i < listViewsPath.size(); i++) {                FileMsg fileMsgFont = new FileMsg();                try {                    String base64Content = new String(                            ZipDataUtils.zipForBase64(Base64Utils.fileToByte(listViewsPath.get(i))));                    fileMsgFont.setContent(base64Content);                    fileMsgFont.setFileName("纸质协议" + i + ".jpg");                    fileMsgFont.setIndex(i+1 + "");                    fileMsgFont.setAttachSecurCode(HashCodeUtils.hashCodeVaule(                            base64Content.hashCode(), SharedPreferencesHelper.getString(Constant.UUID, "")));                    list.add(fileMsgFont);                } catch (Exception e) {                    e.printStackTrace();                }            }            attachPost.setFileList(list);            return attachPost;        }        @Override        protected void onPostExecute(APP_120008 attachPost) {            UpLoadAttach(attachPost);        }        @Override        protected void onProgressUpdate(Integer... values) {        }        @Override        protected void onCancelled() {            super.onCancelled();        }        @Override        protected void onCancelled(APP_120008 s) {            super.onCancelled(s);        }    }    private RequestHandle requestHandle;    private void UpLoadAttach(final APP_120008 attachPost) {        requestHandle = ApiRequest.requestData(attachPost, mPhoneNumber, new JsonHttpHandler() {            @Override            public void onDo(JSONObject responseJsonObject) {                APP_120008 result = JSON.parseObject(responseJsonObject.toString(), APP_120008.class);                if ("0000".equals(result.getDetailCode())) {                    ToastHelper.ShowToast("上传成功",1);                    TakeProtocolPicActivity.this.finish();                } else {                    ToastHelper.ShowToast("提示："+result.getDetailInfo(),1);                }            }            @Override            public void onDo(JSONArray responseJsonArray) {            }            @Override            public void onDo(String responseString) {            }            @Override            public void onFinish() {                DialogHelper.dismissProgressDialog();            }            @Override            public void onFail(String msg) {                ToastHelper.ShowToast("提示:"+msg,1);            }        });    }	public static String getTimeName(long time) {		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");		Date date = new Date(time);		return formatter.format(date);	}	private ArrayList<String> listViewsPath = new ArrayList<String>();	@Override	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		switch (requestCode) {		case TAKE_PICTURE:			if (resultCode == RESULT_OK) {				try {					PicChamfer.rotatePic(new File(path + imageName));					Bitmap bm = Bimp.revitionImageSize(path + imageName);					PhotoView img = new PhotoView(this);					img.setBackgroundColor(0xff000000);					img.setImageBitmap(bm);					img.setLayoutParams(new ViewGroup.LayoutParams(                            LayoutParams.MATCH_PARENT,                            LayoutParams.MATCH_PARENT));                    img.setOnLongClickListener(new View.OnLongClickListener() {                        @Override                        public boolean onLongClick(View view) {                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);                            builder.setTitle("提示");                            builder.setMessage("是否删除该图片");                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {                                @Override                                public void onClick(DialogInterface dialog, int which) {                                    dialog.dismiss();                                    remove();                                }                            });                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {                                @Override                                public void onClick(DialogInterface dialog, int which) {                                    dialog.dismiss();                                }                            });                            builder.setCancelable(true);                            builder.create().show();                            return true;                        }                    });					listViews.add(img);                    String mPicPath = BitmapHelper.saveBitmap(BitmapHelper.getimage(path + imageName),imageName);                    listViewsPath.add(mPicPath);				} catch (Exception e) {					e.printStackTrace();				}			}			if (!listViews.isEmpty()) {				myPageAdapter.setListViews(listViews);			}			break;		}		if (listViews.size() > 0) {			location = listViews.size() - 1;		}		mViewPagerFixed.setCurrentItem(location);        tv_page.setText((location+1)+"/"+listViews.size());        mBtnPic.setText("拍下一页");	}	@Override	public void onBackPressed() {		if (listViewsPath.size()>0 && mArrayListProtocolFileId.size()<1){            AlertDialogHelper.showAlertDialog(TakeProtocolPicActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeProtocolPicActivity.this.finish();                        }                    });        }else {            TakeProtocolPicActivity.this.finish();        }	}    @Override    public void goBack(View view) {        if (listViewsPath.size()>0 && mArrayListProtocolFileId.size()<1){            AlertDialogHelper.showAlertDialog(TakeProtocolPicActivity.this,                    "提示：", "图片还未上传,是否退出?", new ChooseDialogDoClickHelper() {                        @Override                        public void doClick(DialogInterface dialog,                                            int which) {                            TakeProtocolPicActivity.this.finish();                        }                    });        }else {            TakeProtocolPicActivity.this.finish();        }    }}