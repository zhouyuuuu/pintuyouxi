package com.example.lenovo.pingtuyouxi.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;


public abstract class MyBaseAdapter<T> extends RecyclerView.Adapter{

    Context context;
    List<T> dataList;//图片列表
    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(View v,int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    MyBaseAdapter(Context context,List<T> dataList){
        this.dataList = dataList;
        this.context = context;
    }



    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //在此方法中需要加载item布局后赋给Holder并返回holder
        View v = inflateView(parent);
        return NewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //在此方法中需要将holder中的控件与所给的数据关联起来
        ((BaseHolder)holder).imageView.setImageBitmap(HandleDataList(dataList,position));
    }

    abstract Bitmap HandleDataList(List<T> dataList, int position);

    @Override
    public int getItemCount() {
        //返回数据列表的大小就行了
        return dataList.size();
    }



    abstract View inflateView(ViewGroup parent);
    abstract BaseHolder NewHolder(View v);


}
