package com.sctjsj.lazyhost.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;

import com.sctjsj.lazyhost.R;


/**
 * 类说明：
 * <p>
 * 作  者：小蜗（Richard） on 2017/11/20.
 */
public class DimenUtil {
    /**
     * 获取屏幕的宽度（单位：px）
     *
     * @return 屏幕宽
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的高度（单位：px）
     *
     * @return 屏幕高
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    public static int dp2px(Context context,int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    public static int dp2px(Context context,float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context
                .getResources()
                .getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param sp
     */
    public static int sp2px(Context context,int sp) {
        float density = context
                .getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (sp * density + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 测量字符串长度和高度
     *
     * @param text
     * @return
     */
    public static Rect getTextBounds(String text) {
        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }


    /**
     * px --> dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int getActionbarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        int actionBarHeight = 0;

        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    //获取手机沉浸式状态栏高度
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
