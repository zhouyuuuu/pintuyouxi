package com.example.lenovo.pingtuyouxi.Activitys;


import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import com.example.lenovo.pingtuyouxi.Adapter.LinearAdapter;
import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.Utils.MeasureUtil;
import com.example.lenovo.pingtuyouxi.Utils.ScreenUtil;
import com.example.lenovo.pingtuyouxi.mode.RankInfo;
import org.litepal.crud.DataSupport;
import java.util.ArrayList;
import java.util.List;


public class RankActivity extends Activity implements View.OnClickListener {

    private List<RankInfo> rankInfos = new ArrayList<>();
    private LinearAdapter linearAdapter;
    private PopupWindow popupWindow;
    private int pintusize = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        int intentData = getIntent().getIntExtra("pintusize",0);
        if(intentData != 0){
            pintusize = intentData;
            rankInfos = DataSupport.where("pintusize = ?",String.valueOf(pintusize)).order("timeCount asc").find(RankInfo.class);
        }else {
            rankInfos = DataSupport.findAll(RankInfo.class);
        }

        InitView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void InitView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rank_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearAdapter = new LinearAdapter(rankInfos);
        recyclerView.setAdapter(linearAdapter);

        Button button2 = (Button) findViewById(R.id.bt_two);
        Button button3 = (Button) findViewById(R.id.bt_three);
        Button button4 = (Button) findViewById(R.id.bt_four);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_window,null);
        popupWindow = new PopupWindow(contentView,MeasureUtil.dip2px(this,120),MeasureUtil.dip2px(this,80));
        popupWindow.setElevation(MeasureUtil.dip2px(this,10));
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        contentView.findViewById(R.id.page_first_bt).setOnClickListener(this);
        contentView.findViewById(R.id.time_first_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int[] location = new int[2];
        switch (v.getId()){
            case R.id.bt_two:
                pintusize = 2;
                popupWindow.dismiss();
                v.getLocationOnScreen(location);
                popupWindow.showAtLocation(v, Gravity.LEFT,0,location[1]-ScreenUtil.getScreenSize(this).heightPixels/2-MeasureUtil.dip2px(this,45));
                break;
            case R.id.bt_three:
                pintusize = 3;
                popupWindow.dismiss();
                v.getLocationOnScreen(location);
                popupWindow.showAtLocation(v,Gravity.CENTER,0,location[1]-ScreenUtil.getScreenSize(this).heightPixels/2-MeasureUtil.dip2px(this,45));
                break;
            case R.id.bt_four:
                pintusize = 4;
                popupWindow.dismiss();
                v.getLocationOnScreen(location);
                popupWindow.showAtLocation(v, Gravity.RIGHT,0,location[1]-ScreenUtil.getScreenSize(this).heightPixels/2-MeasureUtil.dip2px(this,45));
                break;
            case R.id.page_first_bt:
                rankInfos.clear();
                rankInfos.addAll(DataSupport.where("pintusize = ?",String.valueOf(pintusize)).order("pageCount asc").find(RankInfo.class));
                linearAdapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
            case R.id.time_first_bt:
                rankInfos.clear();
                rankInfos.addAll(DataSupport.where("pintusize = ?",String.valueOf(pintusize)).order("timeCount asc").find(RankInfo.class));
                linearAdapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
            default:
                break;
        }
    }
}
