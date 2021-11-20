package com.example.joint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ItemActivity extends AppCompatActivity {
    // 물품 게시글 상세글 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
    }
    public void onClickNotice(View v){
        Intent intent = new Intent(ItemActivity.this, NoticeActivity.class);
        startActivity(intent);
    }

    public void onClickMyProfile(View v){
        Intent intent = new Intent(ItemActivity.this, MyprofileActivity.class);
        startActivity(intent);
    }

    public void onClickNotification(View v){
        Intent intent = new Intent(ItemActivity.this, NotificationActivity.class);
        startActivity(intent);
    }
}