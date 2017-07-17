package com.example.lenovo.pingtuyouxi.Activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.lenovo.pingtuyouxi.Adapter.MyBaseAdapter;
import com.example.lenovo.pingtuyouxi.Adapter.gridAdapterMy;
import com.example.lenovo.pingtuyouxi.NetWork.RetrofitInterface;
import com.example.lenovo.pingtuyouxi.R;
import com.example.lenovo.pingtuyouxi.Thread.ThreadManager;
import com.example.lenovo.pingtuyouxi.Utils.PicSaveUtil;
import com.example.lenovo.pingtuyouxi.Utils.ResizeUtil;
import com.example.lenovo.pingtuyouxi.Utils.ScreenUtil;
import com.example.lenovo.pingtuyouxi.mode.GridSpacingItemDecoration;
import com.example.lenovo.pingtuyouxi.mode.UrlAndPic;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PictureActivity extends AppCompatActivity {

    private View contentView;
//    private boolean isRefreshing = false;
    private boolean HaveDatas = true;
    private int pagesize;//一次请求多少个图片Url
    private String cacheDir;//SD卡缓存路径
    private String pintuCacheDir;//拼图缓存路径
    private int fresh_count;//请求刷新的次数
    private int pic_page;//请求图片Url列表时图片页码
    private boolean isLoading;//是否正在加载图片
    private int lastVisibleItem;//RecyclerView中可见的最后一项的position
    private int oldlastVisibleItem = -1;
    private int firstVisibleItem;
    private int oldfirstVisibleItem = -1;
    private int ScreenWidth;//屏幕宽度
    private List<String> PictureUrls;//网络图片Url列表，存放图片Url
    private ArrayList<Integer> nullBitmapIndex;//应删除的PictureUrls中的对象的position
    private Handler handler;//hanlder
    private List<Integer> pictures = new ArrayList<>();//本地图片ID列表，存放图片的ResourceId;
    private int pingtusize;//拼图规模
//    private List<Bitmap> bitmaps;//图片列表，用于显示自带图片+网络图片
    private List<UrlAndPic> urlAndPics;
    private gridAdapterMy mGridAdapter;//RecyclerView适配器
    private AlertDialog alertDialog;//生成拼图对话框
    private GridLayoutManager mGridLayoutManager;//RecyclerView网格管理器
    private ImageView ivNum2;
    private ImageView ivNum3;
    private ImageView ivNum4;

    private static class MyHandler extends Handler {

        WeakReference<PictureActivity> mActivity;//对此Activity的弱引用,防止内存泄露

        MyHandler(PictureActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            PictureActivity theActivity = mActivity.get();//静态内部类要拿到Activity中的东西需要拿到Activity实例
            if (theActivity == null){
                return;//如果拿到为空则不处理消息
            }
            if (msg.arg1 == 1){//1是请求图片的Url完成时发送的消息
                if (theActivity.HaveDatas){
                    theActivity.decodePicturesUrlPlus();
                }
            }else if (msg.arg1 == 2){//2是下载每张图片完成时发送的消息

                theActivity.mGridAdapter.notifyItemChanged(theActivity.PictureUrls.size()-9+theActivity.fresh_count);
                if (theActivity.PictureUrls.size()-9+theActivity.fresh_count>=90){
                    theActivity.urlAndPics.get(theActivity.PictureUrls.size()-9+theActivity.fresh_count-90).bitmap=null;
                    theActivity.mGridAdapter.notifyItemChanged(theActivity.PictureUrls.size()-9+theActivity.fresh_count-90);
                    Log.e("deleteA",""+(theActivity.PictureUrls.size()-9+theActivity.fresh_count-90));
                    Runtime.getRuntime().gc();
                }
                theActivity.fresh_count++;
                if (theActivity.fresh_count >= 9){
                    theActivity.fresh_count = 0;
                }



//                if (++theActivity.fresh_count >= 2){//判断是否是第二次请求刷新
//                    theActivity.fresh_count = 0;//请求数重置
//                    theActivity.mGridAdapter.notifyItemChanged(theActivity.PictureUrls.size());
//                    theActivity.mGridAdapter.notifyDataSetChanged();//RecyclerView刷新
//                }
                if (msg.arg2 == 1){//arg2 = 1 是下载完每次下载任务的最后一张图时发送的消息
                    Collections.sort(theActivity.nullBitmapIndex);//将nullBitmapIndex按顺序排列
                    Collections.reverse(theActivity.nullBitmapIndex);//将nullBitmapIndex反转，因为删除ArrayList中的元素从后面开始删
                    for (int index: theActivity.nullBitmapIndex){//遍历nullBitmapIndex，将Urls表中应该删除的item删掉
                        theActivity.PictureUrls.remove(index);
                    }
                    theActivity.nullBitmapIndex.clear();//清空nullBitmapIndex
                    theActivity.isLoading = false;//加载状态为否
                }
            }else if (msg.arg1 == 3){//加载完网络拼图即将进入游戏界面时发送的消息
                theActivity.alertDialog.dismiss();//对话框隐藏
                Intent intent = new Intent();
                intent.putExtra("picPath",theActivity.pintuCacheDir);//携带图片地址
                intent.putExtra("pingtusize",theActivity.pingtusize);//携带拼图规模
                intent.setClass(theActivity,GameActivity.class);
                theActivity.startActivity(intent);//启动游戏界面
            }else if (msg.arg1 == 4){
                theActivity.alertDialog.dismiss();//对话框隐藏
                String url = (String) msg.obj;
                Intent intent = new Intent();
                intent.putExtra("picPath",url);//携带图片地址
                intent.putExtra("pingtusize",theActivity.pingtusize);//携带拼图规模
                intent.setClass(theActivity,GameActivity.class);
                theActivity.startActivity(intent);//启动游戏界面
            }else if (msg.arg1 == 5){
                theActivity.mGridAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        initPictures();//初始化图片ID列表
        InitMembers();//初始化成员变量
        InitView();//初始化布局配置


    }

    private void InitMembers() {
        contentView = findViewById(R.id.pic_ll);
        ScreenWidth = ScreenUtil.getScreenSize(this).widthPixels;
//        bitmaps = decodePictures(pictures);//解析图片ID列表后初始化图片列表
        cacheDir = getCacheDir().getAbsolutePath();//取到APP缓存文件路径
        urlAndPics = decodePicturesIdToUrlAndPic(pictures);
        nullBitmapIndex  = new ArrayList<>();//初始化nullBitmapIndex
        PictureUrls = new ArrayList<>();//初始化PictureUrls
        handler = new MyHandler(this);
        pingtusize = 3;//默认拼图规模为3
        pagesize = 9;
        pic_page = 0;
        fresh_count = 0;
        isLoading = false;
        pintuCacheDir = Environment.getExternalStorageDirectory().getPath()+"/temp.png";//初始化为SD卡根目录下的temp.png
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);//销毁Activity之前清除hanlder消息
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//调用相册或照相机选择完成后执行
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){//返回结果为成功
            if (requestCode == 100){//请求码为100，是相册返回
                String imagePath;

                Cursor cursor = this.getContentResolver().query(data.getData(),null,null,null,null);
                if (cursor == null){//有些手机可能是从Bundle中返回Bitmap，cursor会返回空
                    Bundle bundle = data.getExtras();
                    if (bundle != null){//将取得的Bitmap存到指定路径下
                        Bitmap photo = (Bitmap) bundle.get("data");
                        saveImage(photo,pintuCacheDir);
                        imagePath = pintuCacheDir;
                    }else {
                        Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    cursor.moveToFirst();
                    imagePath = cursor.getString(cursor.getColumnIndex("_data"));//获得cursor中的路径
                    cursor.close();
                }

                Intent intent = new Intent(PictureActivity.this,GameActivity.class);
                intent.putExtra("picPath",imagePath);//携带路径传到GameActivity
                intent.putExtra("pingtusize",pingtusize);
                startActivity(intent);
            }else if (requestCode == 200){//请求码为200是照相机返回
                Intent intent = new Intent(PictureActivity.this,GameActivity.class);
                intent.putExtra("picPath",pintuCacheDir);
                intent.putExtra("pingtusize",pingtusize);
                startActivity(intent);
            }
        }else {
            Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
    }

    /*
        初始化图片ID数组
         */
    private void initPictures(){
        pictures.add(R.drawable.pic0);
        pictures.add(R.drawable.pic1);
        pictures.add(R.drawable.pic2);
        pictures.add(R.drawable.pic3);
        pictures.add(R.drawable.pic4);
        pictures.add(R.drawable.pic5);
        pictures.add(R.drawable.pic6);
        pictures.add(R.drawable.pic7);
        pictures.add(R.drawable.pic8);
        pictures.add(R.drawable.pic9);
        pictures.add(R.drawable.pic10);
        pictures.add(R.drawable.pic11);
        pictures.add(R.drawable.pic12);
        pictures.add(R.drawable.pic13);
        pictures.add(R.drawable.pic14);
        pictures.add(R.drawable.pic15);
        pictures.add(R.drawable.pic16);
        pictures.add(R.drawable.pic17);
        pictures.add(R.drawable.pic18);
        pictures.add(R.drawable.pic19);
        pictures.add(R.drawable.pic20);
    }

//    //解析本地图片id转为Bitmap存于Bitmap列表中返回
//    private List<Bitmap> decodePictures(List<Integer> pictures){
//        List<Bitmap> bitmaps = new ArrayList<>();
//        Bitmap bitmap;
//        for (int id:pictures){
//            bitmap = BitmapFactory.decodeResource(getResources(),id);//通过id拿到图片
//            bitmap = ResizeUtil.resizeBitmap(ScreenUtil.getScreenSize(this).widthPixels/3-10,bitmap);//修改图片的大小
//            bitmaps.add(bitmap);//添加到bitmaps中
//        }
//        return bitmaps;
//    }


    private List<UrlAndPic> decodePicturesIdToUrlAndPic(List<Integer> pictures){
        List<UrlAndPic> urlAndPics =new ArrayList<>();

        for (int id:pictures){
            UrlAndPic urlAndPic = new UrlAndPic();
            urlAndPic.url = cacheDir+"/"+String.valueOf(id)+"_small";
            urlAndPic.bitmap = BitmapFactory.decodeFile(urlAndPic.url);
            if (urlAndPic.bitmap == null){
                urlAndPic.bitmap = BitmapFactory.decodeResource(getResources(),id);
                urlAndPic.bitmap = ResizeUtil.resizeBitmap(ScreenUtil.getScreenSize(this).widthPixels/3-10,urlAndPic.bitmap);
                PicSaveUtil.savePicture(cacheDir,String.valueOf(id),urlAndPic.bitmap,"small");
            }
            urlAndPics.add(urlAndPic);
        }
        return urlAndPics;
    }


    //将图片保存到指定路径
    public boolean saveImage(Bitmap photo, String spath) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(spath, false));
            photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //初始化布局
    private void InitView(){


        //初始化排行按钮
        ImageView rankButton = (ImageView) findViewById(R.id.image_rank);
        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PictureActivity.this, RankActivity.class);
                startActivity(intent);
            }
        });

//        //初始化图片规模控件
//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
//                //每次点击RadioButton改变图片规模
//                switch (checkedId){
//                    case R.id.rb1:
//                        pingtusize = 2;
//                        break;
//                    case R.id.rb2:
//                        pingtusize = 3;
//                        break;
//                    case R.id.rb3:
//                        pingtusize = 4;
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });

        //初始化图片规模控件
        ivNum2 = (ImageView) findViewById(R.id.iv_num2);
        ivNum3 = (ImageView) findViewById(R.id.iv_num3);
        ivNum4 = (ImageView) findViewById(R.id.iv_num4);
        ivNum2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingtusize = 2;
                ivNum2.setImageResource(R.drawable.num2);
                ivNum3.setImageResource(R.drawable.numb3);
                ivNum4.setImageResource(R.drawable.numb4);
            }
        });
        ivNum3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingtusize = 3;
                ivNum2.setImageResource(R.drawable.numb2);
                ivNum3.setImageResource(R.drawable.num3);
                ivNum4.setImageResource(R.drawable.numb4);
            }
        });
        ivNum4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingtusize = 4;
                ivNum2.setImageResource(R.drawable.numb2);
                ivNum3.setImageResource(R.drawable.numb3);
                ivNum4.setImageResource(R.drawable.num4);
            }
        });


        //初始化ImageView选择图片
        ImageView iv_more = (ImageView) findViewById(R.id.more_iv);
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PictureActivity.this);
                builder.setTitle("选择图片来源");
                builder.setItems(new String[]{"已有相册","马上去拍"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){//启动相册选取图片，请求码100
                                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent,100);
                                }else if (i==1){//启动相机拍照，请求码200
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    Uri photoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/temp.png"));
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                                    startActivityForResult(intent,200);
                                }
                            }
                        });
                builder.create().show();//显示对话框
            }
        });

        //初始化RecyclerView配置
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        //初始化网格布局
        mGridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);//设置为网格布局
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3,5,false));//设置布局间隔为5px
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置动画效果，目前未体现
        //初始化适配器
        mGridAdapter = new gridAdapterMy(this, urlAndPics);
        mGridAdapter.setOnItemClickListener(new MyBaseAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(View view, final int position) {
                if (position<21){//前21张图为本地图片
                    Intent intent = new Intent();
                    intent.putExtra("picture",pictures.get(position));//携带图片Id
                    intent.putExtra("pingtusize",pingtusize);//携带拼图规模
                    intent.setClass(PictureActivity.this,GameActivity.class);
                    startActivity(intent);//启动游戏界面
                }else {//21张图以后是网络图片
                    AlertDialog.Builder builder = new AlertDialog.Builder(PictureActivity.this);
                    builder.setView(R.layout.dialog_item);
                    alertDialog = builder.create();
                    alertDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            Bitmap bitmap;
                            try {
                                StringBuilder stringBuffer = new StringBuilder(urlAndPics.get(position).url);
                                stringBuffer.delete(stringBuffer.length()-6,stringBuffer.length());
                                stringBuffer.append("_large");
                                String urlTemp = stringBuffer.toString();
                                bitmap = BitmapFactory.decodeFile(urlTemp);
//                                String[] urlTemps = PictureUrls.get(tempPosition-pictures.size()).split("/");
//                                String urlTemp = urlTemps[urlTemps.length-1];//取图片的Url
//                                bitmap = BitmapFactory.decodeFile(cacheDir+"/"+urlTemp+"_"+"large");//从SD卡中拿到图片
                                if (bitmap == null){//SD卡中没有再网络下载
                                    bitmap = Picasso.with(PictureActivity.this).load(PictureUrls.get(position -21)).get();
                                    saveImage(bitmap,pintuCacheDir);
                                    message.arg1 = 3;//网络下载成功
                                }else {
                                    message.arg1 = 4;//SD卡加载成功
                                    message.obj = urlTemp;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            handler.sendMessage(message);//加载拼图完成发送消息
                        }
                    }).start();
                }
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余少于7个item时，自动加载下一页
                if (//newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastVisibleItem +3>=mGridLayoutManager.getItemCount()) {
                    if (HaveDatas){
                        if (!isLoading){//如果没有在加载图片
                            RequestPictures(++pic_page);//请求网络图片
                        }
                    }else {
                        Snackbar.make(contentView,"再怎么刷也没有啦~",Snackbar.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();//获取每次滚动RecyclerView时最后一个可见Item的position
                firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
                if (dy<0&&oldfirstVisibleItem!=firstVisibleItem){
                    oldfirstVisibleItem=firstVisibleItem;
                    if (firstVisibleItem +89<mGridLayoutManager.getItemCount()&&firstVisibleItem - 3>=0&&urlAndPics.get(firstVisibleItem -3).bitmap== null){
                        final int firstVisibleItemTemp = firstVisibleItem;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                urlAndPics.get(firstVisibleItemTemp +89).bitmap = null;
                                urlAndPics.get(firstVisibleItemTemp +88).bitmap = null;
                                urlAndPics.get(firstVisibleItemTemp +87).bitmap = null;
                                Log.e("delete",""+(firstVisibleItemTemp +89)+","+(firstVisibleItemTemp +88)+","+(firstVisibleItemTemp +87));Log.e("ddddddd",urlAndPics.get(firstVisibleItem -3).url);
                                urlAndPics.get(firstVisibleItemTemp -3).bitmap = BitmapFactory.decodeFile(urlAndPics.get(firstVisibleItemTemp -3).url);
                                urlAndPics.get(firstVisibleItemTemp -2).bitmap = BitmapFactory.decodeFile(urlAndPics.get(firstVisibleItemTemp -2).url);
                                urlAndPics.get(firstVisibleItemTemp -1).bitmap = BitmapFactory.decodeFile(urlAndPics.get(firstVisibleItemTemp -1).url);
                                Runtime.getRuntime().gc();
                                Message message = new Message();
                                message.arg1 = 5;
                                handler.sendMessage(message);
                            }
                        }).start();
                    }
                }
                if (dy>0&&oldlastVisibleItem!=lastVisibleItem) {
                    oldlastVisibleItem=lastVisibleItem;
                    if (lastVisibleItem + 3 < mGridLayoutManager.getItemCount()&&urlAndPics.get(lastVisibleItem + 3).bitmap == null&&lastVisibleItem - 92>=0) {
                        final int lastVisibleItemTemp = lastVisibleItem;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                urlAndPics.get(lastVisibleItemTemp - 90).bitmap = null;
                                urlAndPics.get(lastVisibleItemTemp - 91).bitmap = null;
                                urlAndPics.get(lastVisibleItemTemp - 92).bitmap = null;
                                Log.e("delete",""+(lastVisibleItemTemp - 90)+","+(lastVisibleItemTemp - 91)+","+(lastVisibleItemTemp - 92));
                                urlAndPics.get(lastVisibleItemTemp + 1).bitmap = BitmapFactory.decodeFile(urlAndPics.get(lastVisibleItemTemp + 1).url);
                                urlAndPics.get(lastVisibleItemTemp + 2).bitmap = BitmapFactory.decodeFile(urlAndPics.get(lastVisibleItemTemp + 2).url);
                                urlAndPics.get(lastVisibleItemTemp + 3).bitmap = BitmapFactory.decodeFile(urlAndPics.get(lastVisibleItemTemp + 3).url);
                                Runtime.getRuntime().gc();
                                Message message = new Message();
                                message.arg1 = 5;
                                handler.sendMessage(message);
                            }
                        }).start();

                    }
                }
            }
        });
        mRecyclerView.setAdapter(mGridAdapter);
    }



    //请求网络上的图片
    private void RequestPictures(int picturesPage){
        isLoading = true;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://gank.io/")
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<ResponseBody> call = retrofitInterface.GetPictures(picturesPage);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {//解析Json将Urls保存起来
                    String jsonString = new String(response.body().bytes(), "utf-8");
                    JSONObject jsonObeject = new JSONObject(jsonString);
                    JSONArray ja1 = jsonObeject.getJSONArray("results");
                    if (ja1.length()<9){
                        HaveDatas = false;
                        Snackbar.make(contentView,"没有数据了哦~",Snackbar.LENGTH_SHORT).show();
                        isLoading = false;
                    }else {
                        for (int i=0;i<ja1.length();i++){
                            JSONObject object = ja1.getJSONObject(i);
                            String pictureUrl = object.getString("url");
                            PictureUrls.add(pictureUrl);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.arg1 = 1;
                handler.sendMessage(message);//请求图片URL成功发送消息
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading = false;//失败时加载结束
            }
        });
    }


    //开启10条线程将Url列表解析成Bitmap导入到bitmaps列表中
    private void decodePicturesUrlPlus(){
        //传context，屏幕宽度（便于下载后修改图片大小），Url列表，bitmaps列表，hanlder，nullBitmapIndex列表
        ThreadManager threadManager = new ThreadManager(this,ScreenWidth,PictureUrls, urlAndPics,handler,nullBitmapIndex,pagesize);
        threadManager.startLoadPics();
    }

}
