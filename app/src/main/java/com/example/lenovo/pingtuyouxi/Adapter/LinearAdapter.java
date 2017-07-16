package com.example.lenovo.pingtuyouxi.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.mode.RankInfo;
import java.util.List;



public class LinearAdapter extends RecyclerView.Adapter<LinearAdapter.RankHolder> {

    private List<RankInfo> rankInfos;

    public LinearAdapter(List<RankInfo> rankInfos) {
        this.rankInfos = rankInfos;
    }

    @Override
    public RankHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_rankitem,parent,false);
        return new RankHolder(v);
    }

    @Override
    public void onBindViewHolder(RankHolder holder, int position) {
        holder.timeTextView.setText(rankInfos.get(position).getTime());
        holder.pageTextView.setText(String.valueOf(rankInfos.get(position).getPageCount()));
        holder.timeCountTv.setText(String.valueOf(Math.round(rankInfos.get(position).getTimeCount()*10)*1.0f/10));
        holder.ptsizeTv.setText(String.valueOf(rankInfos.get(position).getPintusize()));
    }

    @Override
    public int getItemCount() {
        return rankInfos.size();
    }

    class RankHolder extends RecyclerView.ViewHolder {

        TextView timeTextView;
        TextView pageTextView;
        TextView timeCountTv;
        TextView ptsizeTv;

        RankHolder(View itemView) {
            super(itemView);
            timeTextView = (TextView) itemView.findViewById(R.id.time_tv);
            pageTextView = (TextView) itemView.findViewById(R.id.pageRank_tv);
            timeCountTv = (TextView) itemView.findViewById(R.id.timeRank_tv);
            ptsizeTv = (TextView) itemView.findViewById(R.id.ptsize_tv);
        }
    }

}
