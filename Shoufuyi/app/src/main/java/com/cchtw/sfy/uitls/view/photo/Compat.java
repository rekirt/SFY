package com.cchtw.sfy.uitls.view.photo;import android.os.Build.VERSION;import android.os.Build.VERSION_CODES;import android.view.View;/**   *  * Description: * Created by Fu.H.L on  * Date:2015-9-20-上午12:41:34 * Copyright © 2015年 Fu.H.L All rights reserved. */public class Compat {	private static final int SIXTY_FPS_INTERVAL = 1000 / 60;		public static void postOnAnimation(View view, Runnable runnable) {		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {			SDK16.postOnAnimation(view, runnable);		} else {			view.postDelayed(runnable, SIXTY_FPS_INTERVAL);		}	}}