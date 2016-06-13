package com.cchtw.sfy.uitls.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cchtw.sfy.R;

import java.util.ArrayList;
import java.util.List;

public class ContentView extends ViewGroup {

	private int baseNum = 6;

	private int[] screenDispaly;
	
	private int d;

	private List<Point> list;
	private Context context;
	private DrawlRoute drawlRoute;
	

	public ContentView(Context context,String passWord, DrawlRoute.GestureCallBack callBack) {
		super(context);
		screenDispaly = ScreenUtils.getScreenDispaly(context);
		d = screenDispaly[0]/3;
		this.list = new ArrayList<Point>();
		this.context = context;
		addChild();
		drawlRoute = new DrawlRoute(context, list,passWord,callBack);
	}
	
	private void addChild(){
		for (int i = 0; i < 9; i++) {
			ImageView image = new ImageView(context);
			image.setBackgroundResource(R.drawable.gesture_node_normal);
			this.addView(image);

			int row = i / 3;
			int col = i % 3;

			int leftX = col*d+d/baseNum;
			int topY = row*d+d/baseNum; 
			int rightX = col*d+d-d/baseNum;
			int bottomY = row*d+d-d/baseNum;
			
			Point p = new Point(leftX, rightX, topY, bottomY, image,i+1);

			this.list.add(p);
		}
	}

	
	public void setParentView(ViewGroup parent){
		int width = screenDispaly[0];
		LayoutParams layoutParams = new LayoutParams(width, width);
		
		this.setLayoutParams(layoutParams);
		drawlRoute.setLayoutParams(layoutParams);
		parent.addView(drawlRoute);
		parent.addView(this);
		
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			int row = i/3;
			int col = i%3;
			View v = getChildAt(i);
			v.layout(col*d+d/baseNum, row*d+d/baseNum, col*d+d-d/baseNum, row*d+d-d/baseNum);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
