package com.example.lenovo.pingtuyouxi.NetWork;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;


public interface RetrofitInterface {
    @GET("api/data/福利/9/{page}")
    Call<ResponseBody> GetPictures(@Path("page") int page);

    @GET
    Call<ResponseBody> GetPictureBitmaps(@Url String url);


}
