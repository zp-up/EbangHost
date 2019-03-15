package com.sctjsj.ebanghost.util;

import android.content.Context;

public class DpUtils {

	public static int dpToPx(Context context, float value){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (value * scale + 0.5f);
	}
	public static int pxToDp(Context context, float value){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (value / scale + 0.5f);
	}
	//地图转换距离m-km
	public static int mCaculate(float f){
		int m=(int)f;
		int km=m/1000;
		return km;
	}
}
