package com.cchtw.sfy.uitls.view.datepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends ScrollView{

	private int width = 100;
	public WheelView(Context context) {
		super(context);
		init(context);
	}

	public WheelView(Context context,int width) {
		super(context);
		this.width = width;
		init(context);
	}

	public WheelView(Context context, AttributeSet attr){
		super(context, attr);
		init(context);
	}
	
	public WheelView(Context context, AttributeSet attr, int defStyle){
		super(context, attr, defStyle);
		init(context);
	}
	
	public static interface OnScrollListener{
		public void onRefreshDayWheelView();
	}
	
	private List<String> items = null;
	private Context mContext = null;
	private LinearLayout viewsLayout = null;
	/**想要显示的的个数，默认是三个*/
	private int displayItemCount = 3;
	/**一个item的高度*/
	private int itemHeight = 0;
	private int initialY = 0;
	/**当前选中的位置*/
	public int currentIndex = 1;
	/**滑动监听器*/
	private OnScrollListener mOnScrollListener = null;
	
	/**处理松手后滑动到对应的位置上*/
	private Runnable scrollTask = new Runnable() {
		public void run() {
			int newY = getScrollY();
			if(initialY == newY){  //滑动停止
				int position = initialY / itemHeight + 1 ;
				int remain = initialY % itemHeight;
				if(remain == 0){
					currentIndex = position;
				} else {//下一行
					if(remain > itemHeight/2){
						currentIndex = position +1;
						scrollerToItem(currentIndex);
					} else {//停在当前行
						currentIndex = position ;
						scrollerToItem(currentIndex);
					}
				}
				
				if(mOnScrollListener != null){
					mOnScrollListener.onRefreshDayWheelView();
				}
			} else {	//滑动没有停止
				initialY = getScrollY();
				WheelView.this.postDelayed(scrollTask, 50);
			}
			Log.i("Garment0424", "currentIndex:" + currentIndex);
		}
	};
	
	/**
	 * 获得添加的所有数据
	 * @return
	 */
	public List<String> getItems(){
		return this.items;
	}
	
	/**
	 * 加载数据
	 */
	public void setItems(List<String> itemList){
		if(items == null){
			items = new ArrayList<String>();
		}
		items.clear();
		items.addAll(itemList);
		/**数据的第一项和最后一项添加空字符串，以便数据都能显示到中间选中的位置*/
		items.add(0, "");
		items.add("");
		initWheelViewDatas();
		refreshWheelView(currentIndex);
	}
	
	/**
	 * 初始化WheelView的布局
	 * 本次只是添加了一个LinearLayout布局
	 * @author garment
	 * @since 2016/04/24
	 * @param context
	 */
	private void init(Context context){
		this.mContext = context;
		viewsLayout = new LinearLayout(mContext);
		viewsLayout.setOrientation(LinearLayout.VERTICAL);
		this.addView(viewsLayout);
	}
	
	/**
	 * 把数据添加到布局当中
	 */
	private void initWheelViewDatas(){
		if(viewsLayout != null){
			viewsLayout.removeAllViews();
		}
		for(String item : items){
			viewsLayout.addView(createView(item));
		}
		//设置WheelView控件的宽和高
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
		this.setLayoutParams(new LinearLayout.LayoutParams(width, itemHeight*displayItemCount));
	}
	
	private TextView createView(String text){
		TextView textView = new TextView(mContext);
		textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setSingleLine();
		textView.setText(text);
		textView.setTextSize(20);
		textView.setGravity(Gravity.CENTER);
		textView.setPadding(20, 20, 20, 20);
		if(itemHeight == 0){
			itemHeight = getViewMeasuredHeight(textView);
		}
		return textView;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		refreshWheelView(t);
	}
	
	/**
	 * 根据滑动的位置刷新WheelView文字的颜色
	 * @param t
	 */
	private void refreshWheelView(int t){
		int position = t / itemHeight + 1;
		int remain = t % itemHeight;
		if(remain == 0){
			position = t / itemHeight +1;
		} else {
			if(remain > itemHeight/2){
				position = position  + 1;
			} 
		}
		
		int childSize = viewsLayout.getChildCount();
		for(int i = 0; i < childSize; i ++){
			TextView tv = (TextView) viewsLayout.getChildAt(i);
			if (tv == null) {
				return;
			}
			if(i == position){
				tv.setTextColor(Color.WHITE);
			} else {
				tv.setTextColor(Color.BLACK);
			}
		}
	}
	
	public void refreshWheelViewByPosition(int position){
		int childSize = viewsLayout.getChildCount();
		for(int i = 0; i < childSize; i ++){
			TextView tv = (TextView) viewsLayout.getChildAt(i);
			if (tv == null) {
				return;
			}
			if(i == position){
				tv.setTextColor(Color.WHITE);
			} else {
				tv.setTextColor(Color.BLACK);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_UP){
			startScrollTask();
		}
		return super.onTouchEvent(ev);
	}
	
	private void startScrollTask(){
		initialY = getScrollY();
		this.postDelayed(scrollTask, 50);
	}
	
	private int getViewMeasuredHeight(View view) {
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }
	/**控件的宽度*/
	private int viewWidth = 0;
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		viewWidth = w;
		setBackgroundDrawable(null);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private Paint paint = null;
	
	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		// TODO Auto-generated method stub
		if(paint == null){
			paint  = new Paint();
		}
		background = new Drawable() {
			
			@Override
			public void setColorFilter(ColorFilter cf) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAlpha(int alpha) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public int getOpacity() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public void draw(Canvas canvas) {
				// TODO Auto-generated method stub
				canvas.drawLine(0, itemHeight, viewWidth, itemHeight, paint);
				canvas.drawLine(0, 2*itemHeight, viewWidth, 2*itemHeight, paint);
			}
		};
		
		super.setBackgroundDrawable(background);
	}
	
	/**
	 * 滑动到指定的位置
	 * @param pos
	 */
	private void scrollerToItem(int pos){
		smoothScrollTo(0, (pos-1) * itemHeight);
	}
	
	/**
	 * 指定到某个位置
	 * @param pos
	 */
	public void setCurrentItem(int pos){
		currentIndex = pos;
		Log.i("Garment0424"," setCurrentItem current:" + currentIndex);
		scrollerToItem(currentIndex);
		refreshWheelViewByPosition(currentIndex);
	}
	
	/**
	 * 获取当前选中位置的对象
	 * @return
	 */
	public String getCurrentItem(){
		return items.get(currentIndex);
	}
	
	public void registerOnScrollListener(OnScrollListener onScrollListener){
		mOnScrollListener = onScrollListener;
	}
	
	public int getCurrentIndex(){
		return this.currentIndex;
	}
	
}
