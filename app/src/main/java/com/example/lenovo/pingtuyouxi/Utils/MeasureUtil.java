package com.example.lenovo.pingtuyouxi.Utils;

import android.content.Context;

public class MeasureUtil {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;//获取屏幕分辨率
        return (int) (dpValue * scale + 0.5f);//dp乘以分辨率就是px，+0.5f四舍五入
    }
}
