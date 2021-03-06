package com.example.joint;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationActivity extends AppCompatActivity {

    private ListView notification_view;
    NotificationListViewAdapter adapter;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    String checkStudentid;

    // Front End (bottom_menu)
    private BottomNavigationView bottomNavigationView;

    // 알림
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        checkStudentid = PreferenceManager.getString(getApplicationContext(), "studentId");

        notification_view = (ListView) findViewById(R.id.notificationListView);
        showNotificationList();

        // Front End
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch(item.getItemId()){
                    case R.id.action_home:
                        intent = new Intent(NotificationActivity.this, ItemListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notice:
                        intent = new Intent(NotificationActivity.this, NoticeListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_profile:
                        intent = new Intent(NotificationActivity.this, MyprofileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notification:
                        intent = new Intent(NotificationActivity.this, NotificationActivity.class);
                        startActivity(intent);
                        break;
                }

                return true;
            }
        });
    }

    private void showNotificationList() {
        adapter = new NotificationListViewAdapter();
        notification_view.setAdapter(adapter);

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("notification_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                String studentId = dataSnapshot.child("studentId").getValue().toString();

                if(!studentId.equals(checkStudentid)) return;

                String date = dataSnapshot.child("date").getValue().toString();
                String content = dataSnapshot.child("content").getValue().toString();

                adapter.addItem(date, content, studentId);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    public void onClickHome(View v){
//        Intent intent = new Intent(NotificationActivity.this, ItemListActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickNotice(View v){
//        Intent intent = new Intent(NotificationActivity.this, NoticeListActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickMyProfile(View v){
//        Intent intent = new Intent(NotificationActivity.this, MyprofileActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickNotification(View v){
//        Intent intent = new Intent(NotificationActivity.this, NotificationActivity.class);
//        startActivity(intent);
//    }
}