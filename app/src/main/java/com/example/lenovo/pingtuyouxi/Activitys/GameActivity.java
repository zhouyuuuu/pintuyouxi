package com.example.lenovo.pingtuyouxi.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.lenovo.pingtuyouxi.Adapter.PiecesAdapterMy;
import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.Utils.BitmapSplitter;
import com.example.lenovo.pingtuyouxi.Utils.ResizeUtil;
import com.example.lenovo.pingtuyouxi.Utils.ScreenUtil;
import com.example.lenovo.pingtuyouxi.mode.GridSpacingItemDecoration;
import com.example.lenovo.pingtuyouxi.mode.ImagePiece;
import com.example.lenovo.pingtuyouxi.mode.RankInfo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int LEFT = 101;
    private static final int RIGHT= 102;
    private static final int UP = 103;
    private static final int DOWN = 104;

    private TextView pagecount_tv;//步数控件
    private TextView timecount_tv;//时间控件
    private ImageView imageView;//原图控件

    private RecyclerView mRecyclerView;//用于拼图显示
    private PiecesAdapterMy mPiecesAdapter;//碎片适配器

    private List<ImagePiece> pieces = new ArrayList<>();//图片碎片列表
    private Bitmap mBitmap;//原图Bitmap
    private int pingtusize;//拼图规模

    private int pageCount = 0;//步数，初始化0
    private float timeCount = 0;//时间，初始化0.000
    private boolean isCompleted = false;//完成标记
    private int markid;//记录空白图所在列表中的位置

    private Timer timer;//定时器
    private TimerTask timerTask;//定时器任务

    private static Handler handler = new Handler();


    @Override
    public void onBackPressed() {
        if (imageView.getVisibility() == View.VISIBLE){
            TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,-imageView.getHeight());
            translateAnimation.setDuration(500);
            imageView.startAnimation(translateAnimation);
            imageView.setVisibility(View.INVISIBLE);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        InitIntentData();
        InitView();
        InitTimer();
        InitPieces();
        InterminglePieces();
    }
    //初始化拼图
    private void InitPieces(){
        ImagePiece piece = pieces.get(pingtusize*pingtusize-1);
        piece.bitmap = null;
        markid = piece.index;
    }
    //打乱拼图
    private void InterminglePieces() {

        for (int i=0;i<100;i++){
            int random1 = (int) (Math.random()*4);
            if (random1 == 4){
                random1 = 3;
            }
            switch (random1){
                case 0:
                    if (markid %pingtusize == 0){
                        break;
                    }else {
                        Collections.swap(pieces, markid, markid -1);
                        markid = markid -1;
                    }
                    break;
                case 1:
                    if (markid /pingtusize == 0){
                        break;
                    }else {
                        Collections.swap(pieces, markid, markid -pingtusize);
                        markid = markid -pingtusize;
                    }
                    break;
                case 2:
                    if ((markid +1)%pingtusize == 0){
                        break;
                    }else {
                        Collections.swap(pieces, markid, markid +1);
                        markid = markid +1;
                    }
                    break;
                case 3:
                    if (markid +pingtusize > pingtusize*pingtusize-1){
                        break;
                    }else {
                        Collections.swap(pieces, markid, markid +pingtusize);
                        markid = markid +pingtusize;
                    }
                    break;
            }
        }
        mPiecesAdapter.notifyDataSetChanged();
    }

    //初始化定时器，100毫秒更新一次UI
    private void InitTimer() {
        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeCount = (float)Math.round((timeCount+0.1)*10)/10;
                        timecount_tv.setText(String.valueOf(timeCount));
                    }
                });
            }
        };
        timer.schedule(timerTask,0,100);
    }

    //初始化Intent传过来的数据(图片地址/图片ResourceId，拼图规模)，初始化拼图碎片列表
    private void InitIntentData() {
        pingtusize = getIntent().getExtras().getInt("pingtusize");
        String imagePath = getIntent().getExtras().getString("picPath");
        int resourceId = getIntent().getExtras().getInt("picture");
        if (imagePath != null){
            Bitmap mBitmapTemp = BitmapFactory.decodeFile(imagePath);
            int screenWidth = ScreenUtil.getScreenSize(this).widthPixels;
            mBitmap = ResizeUtil.resizeBitmap(screenWidth,mBitmapTemp);
        }else{
            mBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        }
        pieces = BitmapSplitter.split(mBitmap,pingtusize,pingtusize);
    }

    //判断点击的Item是否在空白格四周,传入该点position
    private int isCanSwap(int position, int pingtusize){
        if (markid / pingtusize == position / pingtusize) {
            if (markid + 1 == position) {
                return LEFT;
            }else if (markid - 1 == position){
                return RIGHT;
            }
        } else if(markid % pingtusize == position % pingtusize){
            if (markid + pingtusize == position){
                return UP;
            }else if (markid - pingtusize == position){
                return DOWN;
            }
        }
        return 0;
    }

    //判断拼图是否已完成
    private boolean isCompleted(){
        int correctCount = 0;
        for (int i = 0;i<pingtusize;i++){
            for (int j = 0;j<pingtusize;j++){
                int position1 = j+i*pingtusize;
                if (position1 ==pieces.get(position1).index){
                    correctCount++;
                }
            }
        }
        if (correctCount == pingtusize * pingtusize){
            isCompleted = true;
            return true;
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_yuantu:
                showyuantu();
                break;
            case R.id.bt_reset:
                reset();
                break;
            case R.id.bt_return:
                GameActivity.this.finish();
                break;
            default:
                break;
        }

    }

    public void showyuantu(){
        if (!isCompleted){
            if (imageView.getVisibility() == View.INVISIBLE){
                TranslateAnimation translateAnimation = new TranslateAnimation(0,0,-imageView.getHeight(),0);
                translateAnimation.setDuration(500);
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(translateAnimation);
            }
        }
    }

    @Override
    protected void onDestroy() {
        ClearTimer();
        super.onDestroy();
    }


    private void ClearTimer(){
        if (timer != null&&timerTask!=null){
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }
    }

    public void reset(){

        ClearTimer();
        InterminglePieces();
        InitTimer();

        imageView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        isCompleted =false;
        timeCount = 0;
        pageCount = 0;
        pagecount_tv.setText("0");
        timecount_tv.setText("0");
    }

    private void InitView(){


        //初始化原图、重置、返回按钮
        Button button_yuantu = (Button) findViewById(R.id.bt_yuantu);
        Button button_reset = (Button) findViewById(R.id.bt_reset);
        Button button_return = (Button) findViewById(R.id.bt_return);
        button_yuantu.setOnClickListener(this);
        button_reset.setOnClickListener(this);
        button_return.setOnClickListener(this);


        //初始化ImageView原图控件
        imageView = (ImageView) findViewById(R.id.game_iv);
        imageView.setImageBitmap(mBitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getVisibility() == View.VISIBLE){
                    TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,-imageView.getHeight());
                    translateAnimation.setDuration(500);
                    imageView.startAnimation(translateAnimation);
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });


        //初始化步数、时间控件
        pagecount_tv = (TextView) findViewById(R.id.pageCount_tv);
        timecount_tv = (TextView) findViewById(R.id.timecount_tv);


        //初始化RecyclerView配置
        mRecyclerView = (RecyclerView) findViewById(R.id.game_rv);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(pingtusize,2,false));
        final GridLayoutManager mGridLayoutManager = new GridLayoutManager(GameActivity.this, pingtusize, GridLayoutManager.VERTICAL, false);
        mPiecesAdapter = new PiecesAdapterMy(this,pieces);
        mPiecesAdapter.setOnItemClickListener(new PiecesAdapterMy.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {//
//                if(isAnimating){
//                    return;
//                }
//                isAnimating = true;
                final int positionTemp = position;
                final int markIdTemp = markid;
                int direction = isCanSwap(position,pingtusize);
                TranslateAnimation translateAnimation = null;
                if(direction != 0){
                    switch (direction){
                        case UP:
                            translateAnimation = new TranslateAnimation(0,0,0,-view.getHeight()-2);
                            break;
                        case DOWN:
                            translateAnimation = new TranslateAnimation(0,0,0,+view.getHeight()+2);
                            break;
                        case LEFT:
                            translateAnimation = new TranslateAnimation(0,-view.getWidth()-2,0,0);
                            break;
                        case RIGHT:
                            translateAnimation = new TranslateAnimation(0,+view.getWidth()+2,0,0);
                            break;
                        default:break;
                    }

                    pageCount++;
                    pagecount_tv.setText(String.valueOf(pageCount));
                    if (translateAnimation!=null){
                        translateAnimation.setDuration(120);
                        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                Collections.swap(pieces,positionTemp,markIdTemp);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                mPiecesAdapter.notifyItemChanged(positionTemp);
                                mPiecesAdapter.notifyItemChanged(markIdTemp);
//                                isAnimating =false;
//                                mRecyclerView.setEnabled(true);
                                if (isCompleted()){
                                    mRecyclerView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                    ClearTimer();
                                    RankInfo rankInfo = new RankInfo();
                                    rankInfo.setTimeCount(timeCount);
                                    rankInfo.setPintusize(pingtusize);
                                    rankInfo.setPageCount(pageCount);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("MM-dd  HH:mm:ss");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String str = formatter.format(curDate);
                                    rankInfo.setTime(str);
                                    rankInfo.save();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                                    View dialogView = LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_game,null);
                                    builder.setView(dialogView);
                                    final AlertDialog alertDialog = builder.create();
                                    dialogView.findViewById(R.id.go_rank).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(GameActivity.this,RankActivity.class);
                                            intent.putExtra("pintusize",pingtusize);
                                            startActivity(intent);
                                            alertDialog.dismiss();
                                        }
                                    });
                                    dialogView.findViewById(R.id.go_return).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismiss();
                                        }
                                    });
                                    alertDialog.show();
                                }

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        view.startAnimation(translateAnimation);
                    }
                    markid = position;

                }
            }
        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setAdapter(mPiecesAdapter);


    }
}
