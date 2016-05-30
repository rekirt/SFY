package com.example.shoufuyi.uitls.wintone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoufuyi.R;
import com.example.shoufuyi.activity.MainActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * 
 * 项目名称：excelIODemo 类名称：FileManageActivity 类描述：该类主要实现文件管理器的浏览功能（支持导入和导出） 创建人：黄震
 * 创建时间：2014-11-9 下午8:03:47 修改人：黄震 修改时间：2014-11-9 下午8:03:47 修改备注：
 * 
 * @version
 * 
 */
public class FileManageActivity extends Activity {
    private List<Map<String, Object>> mData;
    private String mDir = Environment.getExternalStorageDirectory().toString();
    private ListView filename_list;

    private RelativeLayout relativeLayout;
    private int srcHeight, srcWidth;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private List<String> mDirs = new ArrayList<String>();
    private Intent intent1;
    private String btn_type;

    private String path = Environment.getExternalStorageDirectory().toString()
            + "/";
    private Bitmap bitmap;
    private ArrayList<HashMap<String, String>> thumbnailList;
    private ContentResolver cr;
    private int nMainId = 2;

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        srcHeight = displayMetrics.heightPixels;
        srcWidth = displayMetrics.widthPixels;
        findView();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filemanage);
        Intent intent = this.getIntent();
        Bundle bl = intent.getExtras();
        String title = bl.getString("title");
        nMainId = intent.getIntExtra("nMainId", 2);

        Uri uri = intent.getData();
        mDir = uri.getPath();
        setTitle(title);
        mData = getData();
        // 获得android设备中所有缩略图的IMAGE_ID和对应的缩略图的路径放入列表thumbnailList中
        thumbnailList = new ArrayList<HashMap<String, String>>();
        cr = getContentResolver();
        String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
                Thumbnails.DATA };
        Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
                null, null, null);
        thumbnailList = getColumnData(cursor);
    }

    /**
     * 
     * @Title: findView
     * @Description: UI组件初始化
     * @return void 返回类型
     * @throws
     */
    private void findView() {
        filename_list = (ListView) this.findViewById(R.id.filename_list);
        relativeLayout = (RelativeLayout) this
                .findViewById(R.id.relativeLayout);
        RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, srcHeight);
        relativeLayout.setLayoutParams(lParams);
        MyAdapter adapter = new MyAdapter(this);
        filename_list.setAdapter(adapter);
        filename_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {

                // TODO Auto-generated method stub
                // 如果是目录 点击 继续递归 查看该路径下的所有文件和文件夹，或者返回上一目录
                if ((Integer) mData.get(position).get("img") == R.drawable.ex_folder) {
                    mDir = (String) mData.get(position).get("info");
                    path = mDir;
                    mDirs.add(mDir);
                    mData = getData();
                    MyAdapter adapter = new MyAdapter(FileManageActivity.this);
                    filename_list.setAdapter(adapter);
                } else {

                    // 如果是文件点击后 将该文件路径回传给上一页面
                    finishWithResult((String) mData.get(position).get("info"));

                }
            }
        });
    }

    // 获取当前路径下的所有文件夹和文件
    private List<Map<String, Object>> getData() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        File f = new File(mDir);
        File[] files = f.listFiles();

        if (!mDir.equals(Environment.getExternalStorageDirectory().toString())) {
            map = new HashMap<String, Object>();
            map.put("title", getString(R.string.backLastDir));
            map.put("info", f.getParent());
            map.put("img", R.drawable.ex_folder);
            list.add(map);

        }
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                map = new HashMap<String, Object>();
                map.put("title", files[i].getName());
                map.put("info", files[i].getPath());

                if (files[i].isDirectory())// 是否是目录

                {
                    map.put("img", R.drawable.ex_folder);

                } else if (files[i].getName().length() >= 4) {
                    if (files[i]
                            .getName()
                            .substring(files[i].getName().length() - 4,
                                    files[i].getName().length()).equals(".jpg")
                            || files[i]
                                    .getName()
                                    .substring(files[i].getName().length() - 4,
                                            files[i].getName().length())
                                    .equals(".JPG")) {
                        map.put("img", 1000);
                    } else {
                        // 如果文件名的长度大于4但不是以.jpg结尾的还是将文件图片赋给它
                        map.put("img", R.drawable.ex_doc);
                        // map.put("img", bitmap1);
                    }
                } else {
                    map.put("img", R.drawable.ex_doc);
                    // map.put("img", bitmap1);
                }

                list.add(map);
            }
        }
        return list;
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;

    }

    /**
     * 
     * 
     * 项目名称：excelIODemo 类名称：MyAdapter 类描述：自定义 Adapter 创建人：黄震 创建时间：2014-11-9
     * 下午8:05:02 修改人：黄震 修改时间：2014-11-9 下午8:05:02 修改备注：
     * 
     * @version
     * 
     */
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

        }

        public int getCount() {
            return mData.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.filemanage_listview,
                        null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                // holder.info = (TextView)
                // convertView.findViewById(R.id.info);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 当上下滑动的时候将将要不显示的图片和数据清空
            holder.img.setImageBitmap(null);
            holder.img.setBackgroundResource(0);
            if ((Integer) mData.get(position).get("img") == 1000) {
                // Android thumbnail 缩略图的获取及与原始图片的映射关系
                String thumbnailPath = "";
                String media_id = "";
                String[] projection1 = { Media._ID, Media.DATA };
                String[] whereArgs = { (String) mData.get(position).get("info") };// 修改条件的参数
                // 先根据原始图片的路径来查询在数据库的表image中Media._ID的值
                Cursor cursor1 = cr.query(Media.EXTERNAL_CONTENT_URI,
                        projection1, Media.DATA + "=?", whereArgs, null);
                if (cursor1 != null) {
                    while (cursor1.moveToNext()) {
                        media_id = cursor1.getString(cursor1
                                .getColumnIndex(Media._ID));
                    }
                    cursor1.close();
                }
                // 根据原始图像的Media._ID来依次查询缩略图像的存储路径
                for (int i = 0; i < thumbnailList.size(); i++) {
                    if (media_id.equals(thumbnailList.get(i).get("image_id"))) {
                        thumbnailPath = thumbnailList.get(i).get("path");
                        break;
                    }

                }
                if (thumbnailPath != null && !thumbnailPath.equals("")) {
                    // 如果存储路径不为空并且不等于空值，则证明有缩略图，我们将缩略图代替原始图显示到界面中
                    String[] str = thumbnailPath.split("/");
                    byte[] data = Stream2Byte(thumbnailPath);
                    if(data!=null)
                    bitmap = BitmapFactory
                            .decodeByteArray(data, 0, data.length);
                    if (bitmap == null) {
                        holder.img.setBackgroundResource(R.drawable.picture);
                    } else {
                        holder.img.setImageBitmap(bitmap);
                    }
                    
                } else if (thumbnailPath.equals("")) {
                    // 如果缩略图中没有记载，则我们显示原始图像但显示前需将图像进一步压缩，防止OOM
                    File file = new File((String) mData.get(position).get(
                            "info"));
                    // 图片的处理
                    FileInputStream fis = null;
                    try {

                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inInputShareable = true;
                        opts.inPurgeable = true;
                        // 不读取像素数组到内存中，仅读取图片的信息
                        opts.inJustDecodeBounds = true;
                        fis = new FileInputStream(file);
                        BitmapFactory.decodeStream(fis, null, opts);
                        // 从Options中获取图片的分辨率
                        int imageHeight = opts.outHeight;
                        int imageWidth = opts.outWidth;
                        int scaleX = imageWidth / 640;
                        int scaleY = imageHeight / 480;
                        int scale = 1;
                        // 采样率依照最大的方向为准
                        if (scaleX > scaleY && scaleY >= 1) {
                            scale = scaleX;
                        }
                        if (scaleX < scaleY && scaleX >= 1) {
                            scale = scaleY;
                        }

                        BitmapFactory.Options opts1 = new BitmapFactory.Options();
                        opts1.inInputShareable = true;
                        opts1.inPurgeable = true;
                        // 采样率
                        opts1.inSampleSize = scale;
                        fis = new FileInputStream(file);
                        byte[] data = Stream2Byte((String) mData.get(position)
                                .get("info"));
                        long time = System.currentTimeMillis();

                        bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length, opts1);
                        if (bitmap == null) {
                            // 如果bitmap为空则证明该图片的大小为0，则显示已经准备好的图片
                            holder.img
                                    .setBackgroundResource(R.drawable.picture);
                        } else {
                            // 如果不为空，则显示本身缩略图像
                            // System.out.println("position:" + position);
                            holder.img.setImageBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // 如果该文件不是照片，则将其显示成文本形式。
                holder.img.setBackgroundResource((Integer) mData.get(position)
                        .get("img"));
            }
            holder.title.setText((String) mData.get(position).get("title"));
            return convertView;
        }
    }

    private void finishWithResult(String path) {
        Bundle conData = new Bundle();
        conData.putString("results", "Thanks Thanks");
        conData.putInt("nMainId", nMainId);
        Intent intent = new Intent();
        intent.putExtras(conData);
        Uri startDir = Uri.fromFile(new File(path));
        intent.setDataAndType(startDir, "image/*");
        setResult(RESULT_OK, intent);
        finish();
    }

    // 监听返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent(FileManageActivity.this,
                    MainActivity.class);
            FileManageActivity.this.finish();
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 
     * @Title: showMess
     * @Description: TODO 用于显示提示信息
     * @param @param s 设定文件
     * @return void 返回类型
     * @throws
     */
    private void showMess(String s) {
        Toast toast = Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 20, 30);
        toast.show();
    }

    /*
     * 将数据流转换成字节数组
     */
    public byte[] Stream2Byte(String infile) {

        BufferedInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(infile));
            out = new ByteArrayOutputStream(1024);
            byte[] temp = new byte[1024];
            int size = 0;
            while ((size = in.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] content = null;
        if (out != null)
            try {
                content = out.toByteArray();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return content;
    }

    private ArrayList<HashMap<String, String>> getColumnData(Cursor cur) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (cur.moveToFirst()) {
            int _id;
            int image_id;
            String image_path;
            int _idColumn = cur.getColumnIndex(Thumbnails._ID);
            int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
            int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

            do {
                // Get the field values
                _id = cur.getInt(_idColumn);
                image_id = cur.getInt(image_idColumn);
                image_path = cur.getString(dataColumn);

                // Do something with the values.
                // System.out.println(_id + " image_id:" + image_id + " path:"
                // + image_path + "---");
                HashMap hash = new HashMap();
                hash.put("image_id", image_id + "");
                hash.put("path", image_path);
                list.add(hash);

            } while (cur.moveToNext());
            cur.close();

        }
        return list;
    }
};
