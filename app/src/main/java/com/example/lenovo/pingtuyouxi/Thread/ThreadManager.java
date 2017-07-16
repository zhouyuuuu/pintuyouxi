package com.example.lenovo.pingtuyouxi.Thread;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import com.example.lenovo.pingtuyouxi.NetWork.RetrofitInterface;
import com.example.lenovo.pingtuyouxi.Utils.PicSaveUtil;
import com.example.lenovo.pingtuyouxi.Utils.ResizeUtil;
import com.example.lenovo.pingtuyouxi.mode.UrlAndPic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ThreadManager {
    private List<Integer> NullBitmapIndexs;//应删除的图片Url Id
    private boolean thread1cd;//线程是否冷却
    private boolean thread2cd;
    private boolean thread3cd;
    private boolean thread4cd;
    private boolean thread5cd;
    private boolean thread6cd;
    private boolean thread7cd;
    private boolean thread8cd;
//    private boolean thread9cd;
    private Handler handler;
    private List<String> PictureUrls;//Urls表
//    private List<Bitmap> bitmaps;//Bitmaps表
    private List<UrlAndPic> urlAndPics;
    private int ScreenWidth;//屏幕宽度
    private int range_start;//从哪里开始
    private int range_page;//每个线程分配几个图
    private String cacheDir;
    private int pagesize;//请求中每次获得多少个Url


    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://gank.io/")
            .build();
    private RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);



    public ThreadManager(Context context, int ScreenWidth, List<String> pictureUrls, List<UrlAndPic> urlAndPics, Handler handler, ArrayList<Integer> nullBitmapIndexs, int pagesize) {
        this.PictureUrls = pictureUrls;
        this.ScreenWidth = ScreenWidth;
        this.urlAndPics = urlAndPics;
        this.handler = handler;
        this.NullBitmapIndexs = nullBitmapIndexs;
        this.pagesize = pagesize;
        thread1cd = false;
        thread2cd = false;
        thread3cd = false;
        thread4cd = false;
        thread5cd = false;
        thread6cd = false;
        thread7cd = false;
        thread8cd = false;
//        thread9cd = false;

        range_start = PictureUrls.size()- this.pagesize;//最新的10个URL
        range_page = pagesize /9;
        cacheDir = context.getCacheDir().getAbsolutePath();//图片缓存路径
    }

    private ArrayList<UrlAndPic> loadPics(int range_start,int range_end){
        ArrayList<UrlAndPic> urlAndPicsTemp = new ArrayList<>();
        for (int i=range_start;i<range_end;i++){
            String[] urlTemps = PictureUrls.get(i).split("/");
            String urlTemp = urlTemps[urlTemps.length-1];
            UrlAndPic urlAndPic = new UrlAndPic();
            urlAndPic.bitmap = BitmapFactory.decodeFile(cacheDir+"/"+urlTemp+"_"+"small");//从SD卡中拿图片
            if (urlAndPic.bitmap == null){//如果没有就网络下载
                try {
                    Call<ResponseBody> call = retrofitInterface.GetPictureBitmaps(PictureUrls.get(i));
                    Response<ResponseBody> response = call.execute();
                    urlAndPic.bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    if (urlAndPic.bitmap != null){
                        PicSaveUtil.savePicture(cacheDir,urlTemp,urlAndPic.bitmap,"large");//原图保存
                        urlAndPic.bitmap = ResizeUtil.resizeBitmap(ScreenWidth/3-4,urlAndPic.bitmap);//修改图片大小
                        PicSaveUtil.savePicture(cacheDir,urlTemp,urlAndPic.bitmap,"small");//缩略图保存
                        urlAndPic.url = cacheDir+"/"+urlTemp+"_"+"small";
                        urlAndPicsTemp.add(urlAndPic);
                    }else {
                        NullBitmapIndexs.add(i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                urlAndPic.url = cacheDir+"/"+urlTemp+"_"+"small";
                urlAndPicsTemp.add(urlAndPic);
            }

        }
        return urlAndPicsTemp;
    }

    private void sendRefreshMsg(List<UrlAndPic> urlAndPicsTemp){
        urlAndPics.addAll(urlAndPicsTemp);//加载完所有图片后全都添加进bitmaps
        Message message = new Message();
        message.arg1 = 2;
        handler.sendMessage(message);//发送刷新消息
    }

    private Thread thread1 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread1cd = false;//未冷却
            List<UrlAndPic> tempBitmaps = loadPics(range_start,range_start+range_page);
            sendRefreshMsg(tempBitmaps);
            thread1cd = true;//已冷却
        }
    });
    private Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread2cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+range_page,range_start+2*range_page);
            while (!thread1cd);//等待线程1冷却
            sendRefreshMsg(tempBitmaps);
            thread2cd = true;
        }
    });
    private Thread thread3 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread3cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+2*range_page,range_start+3*range_page);
            while (!thread2cd);
            sendRefreshMsg(tempBitmaps);
            thread3cd = true;
        }
    });
    private Thread thread4 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread4cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+3*range_page,range_start+4*range_page);
            while (!thread3cd);
            sendRefreshMsg(tempBitmaps);
            thread4cd = true;
        }
    });
    private Thread thread5 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread5cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+4*range_page,range_start+5*range_page);
            while (!thread4cd);
            sendRefreshMsg(tempBitmaps);
            thread5cd = true;
        }
    });
    private Thread thread6 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread6cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+5*range_page,range_start+6*range_page);
            while (!thread5cd);
            sendRefreshMsg(tempBitmaps);
            thread6cd = true;
        }
    });
    private Thread thread7 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread7cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+6*range_page,range_start+7*range_page);
            while (!thread6cd);
            sendRefreshMsg(tempBitmaps);
            thread7cd = true;
        }
    });
    private Thread thread8 = new Thread(new Runnable() {
        @Override
        public void run() {
            thread8cd = false;
            List<UrlAndPic> tempBitmaps = loadPics(range_start+7*range_page,range_start+8*range_page);
            while (!thread7cd);
            sendRefreshMsg(tempBitmaps);
            thread8cd = true;
        }
    });
//    private Thread thread9 = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            thread9cd = false;
//            List<UrlAndPic> tempBitmaps = loadPics(range_start+8*range_page,range_start+9*range_page);
//            while (!thread8cd);
//            sendRefreshMsg(tempBitmaps);
//            thread9cd = true;
//        }
//    });
    private Thread thread9 = new Thread(new Runnable() {
        @Override
        public void run() {
            List<UrlAndPic> tempBitmaps = loadPics(range_start+8*range_page,range_start+9*range_page);
            while (!thread8cd);
            urlAndPics.addAll(tempBitmaps);
            Message message = new Message();
            message.arg1 = 2;
            message.arg2 = 1;
            handler.sendMessage(message);
        }
    });

    public void startLoadPics(){
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();
        thread9.start();
//        thread10.start();
    }


}
