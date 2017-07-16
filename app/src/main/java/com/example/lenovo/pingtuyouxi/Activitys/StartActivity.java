package com.example.lenovo.pingtuyouxi.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.lenovo.pingtuyouxi.R;

/**
 * Created by Lenovo on 2017/7/15.
 */

public class StartActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_start);
    }

    public void start(View v){
        Intent intent = new Intent(StartActivity.this,PictureActivity.class);
        startActivity(intent);
        finish();
    }
    public void rank(View v){
        Intent intent = new Intent(StartActivity.this,RankActivity.class);
        startActivity(intent);
    }
}
