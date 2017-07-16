package com.example.lenovo.pingtuyouxi.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ResizeUtil {

    public static Bitmap resizeBitmap(float newWidth,Bitmap bitmap) {
        Matrix matrix = new Matrix();

        matrix.postScale(
                newWidth / bitmap.getWidth(),
                newWidth / bitmap.getWidth());

        return Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix, true);
    }

}
