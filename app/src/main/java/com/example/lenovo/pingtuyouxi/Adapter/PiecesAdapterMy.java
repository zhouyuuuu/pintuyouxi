package com.example.lenovo.pingtuyouxi.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.Utils.MeasureUtil;
import com.example.lenovo.pingtuyouxi.Utils.ScreenUtil;
import com.example.lenovo.pingtuyouxi.mode.ImagePiece;
import java.util.List;

public class PiecesAdapterMy extends MyBaseAdapter<ImagePiece> {

    private Context mContext ;

    public PiecesAdapterMy(Context context, List<ImagePiece> imagePieces) {
        super(context,imagePieces);
        mContext = context;
    }

    @Override
    Bitmap HandleDataList(List<ImagePiece> dataList, int position) {
        return dataList.get(position).bitmap;
    }

    @Override
    View inflateView(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item2,parent,false);
        RecyclerView.LayoutParams lps = (RecyclerView.LayoutParams) v.getLayoutParams();
        Bitmap bitmap1 = dataList.get(0).bitmap;
        if (bitmap1 == null){
            bitmap1 = dataList.get(1).bitmap;
        }
        int value1 = bitmap1.getWidth();
        int value2 = bitmap1.getHeight();
        float value = value1/(value2*1.0f);
        int ScreenWidth = ScreenUtil.getScreenSize(parent.getContext()).widthPixels-parent.getPaddingLeft()-parent.getPaddingRight()-4;
        int nheight = MeasureUtil.dip2px(mContext,400);
        int pintusize = (int) Math.sqrt(dataList.size());
        if (value>ScreenWidth*1.0f/nheight){
            lps.width = (int) (ScreenWidth*1.0f/pintusize+0.5f);
            lps.height = (int) (value2*1.0f/value1*lps.width+0.5f);
//            FrameLayout.LayoutParams flps = (FrameLayout.LayoutParams) parent.getLayoutParams();
//            flps.topMargin = (nheight-lps.height*pintusize)/2;
//            parent.setLayoutParams(flps);

        }else {
            lps.height = (int) ((MeasureUtil.dip2px(mContext,400)-parent.getPaddingTop()-parent.getPaddingBottom()-4)/Math.sqrt(dataList.size())+0.5f);
            lps.width = (int) (lps.height*value+0.5f);
        }
        v.setLayoutParams(lps);
        return v;
    }

    @Override
    BaseHolder NewHolder(View v) {
        return new PiecesHolder(v);
    }

    private class PiecesHolder extends BaseHolder {
        PiecesHolder(View itemView) {
            super(itemView);
        }

        @Override
        void onClickEvent(View v, int position) {
            if (onItemClickListener!=null){
                onItemClickListener.onItemClick(v,position);
            }
        }
    }
}
