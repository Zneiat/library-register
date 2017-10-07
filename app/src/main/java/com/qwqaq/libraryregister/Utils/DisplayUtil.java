package com.qwqaq.libraryregister.utils;

import android.content.Context;

/**
 * Created by Zneia on 2017/4/19.
 */

public class DisplayUtil
{
    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变 
     *
     * @param pxValue 值
     * @return int
     */
    public static int pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * dip/dp 转 px
     *
     * @param dipValue 值
     * @return int
     */
    public static int dipToPx(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px 转 sp
     *
     * @param pxValue 值
     * @return int
     */
    public static int pxToSp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * sp 转 px
     *
     * @param spValue 值
     * @return int
     */
    public static int spToPx(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
