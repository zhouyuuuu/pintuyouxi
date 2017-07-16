package com.example.lenovo.pingtuyouxi.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.mode.UrlAndPic;

import java.util.List;


public class gridAdapterMy extends MyBaseAdapter<UrlAndPic> {
    public gridAdapterMy(Context context,List<UrlAndPic> dataList) {
        super(context,dataList);
    }

    @Override
    Bitmap HandleDataList(List<UrlAndPic> dataList, int position) {
        Bitmap bitmap = dataList.get(position).bitmap;
//        if (bitmap == null){
//            return dataList.get(position).bitmap= BitmapFactory.decodeFile(dataList.get(position).url);
//        }
        return bitmap;
    }

    @Override
    View inflateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item,parent,false);
    }

    @Override
    BaseHolder NewHolder(View v) {
        return new gridHolder(v);
    }

    private class gridHolder extends BaseHolder {
        gridHolder(View itemView) {
            super(itemView);
        }

        @Override
        void onClickEvent(View v, int position) {
            onItemClickListener.onItemClick(v, position);
        }
    }
}
