package com.example.lenovo.pingtuyouxi.Utils;

import android.graphics.Bitmap;
import com.example.lenovo.pingtuyouxi.mode.ImagePiece;
import java.util.ArrayList;
import java.util.List;

public class BitmapSplitter {

    public static List<ImagePiece> split(Bitmap bitmap, int xPiece, int yPiece) {//实现图片分割，宽分为x段，高分为y段

        List<ImagePiece> pieces = new ArrayList<>(xPiece * yPiece);

        int width = bitmap.getWidth();//图片总宽
        int height = bitmap.getHeight();//图片总高

        int pieceWidth = width / xPiece;//每段宽度
        int pieceHeight = height / yPiece;//每段高度

        for (int i = 0; i < yPiece; i++) {//遍历行，变量为纵坐标
            for (int j = 0; j < xPiece; j++) {//遍历行元素，变量为横坐标

                ImagePiece piece = new ImagePiece();

                piece.index = j + i * xPiece;

                int xValue = j * pieceWidth;
                int yValue = i * pieceHeight;

                piece.bitmap = Bitmap.createBitmap(bitmap, xValue, yValue,
                        pieceWidth, pieceHeight);
                pieces.add(piece);
            }
        }
        return pieces;
    }

}
