package com.example.lenovo.pingtuyouxi.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

abstract class BaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


    ImageView imageView;



    BaseHolder(View itemView) {
        super(itemView);
        //初始化控件以及实现点击监听
        imageView = (ImageView) itemView;
        imageView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        onClickEvent(v,getLayoutPosition());
    }

    abstract void onClickEvent(View v,int position);
}
