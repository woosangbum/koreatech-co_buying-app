package com.example.joint;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NoticeListActivity extends AppCompatActivity {
    // 공시사항 리스트
    private ListView notice_view;
    NoticeListViewAdapter adapter;
    Button noticeRegisterButton;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    // Front End (bottom_menu)
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);

        String studentId = PreferenceManager.getString(getApplicationContext(), "studentId");

        if(!studentId.equals("root")) {
            noticeRegisterButton = findViewById(R.id.noticeRegisterButton);
            noticeRegisterButton.setVisibility(View.INVISIBLE);
            noticeRegisterButton.setEnabled(false);
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        notice_view = (ListView) findViewById(R.id.noticeListView);
        showNoticeList();

        notice_view.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent( getApplicationContext(), NoticeActivity.class);

            // intent 객체에 데이터를 실어서 보내기
            NoticeItem item = (NoticeItem) adapter.getItem(position);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("content", item.getContent());
            intent.putExtra("date", item.getDate());

            startActivity(intent);
        });


        // Front End
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch(item.getItemId()){
                    case R.id.action_home:
                        intent = new Intent(NoticeListActivity.this, ItemListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notice:
                        intent = new Intent(NoticeListActivity.this, NoticeListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_profile:
                        intent = new Intent(NoticeListActivity.this, MyprofileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notification:
                        intent = new Intent(NoticeListActivity.this, NotificationActivity.class);
                        startActivity(intent);
                        break;
                }

                return true;
            }
        });
    }



    private void showNoticeList() {
        adapter = new NoticeListViewAdapter();
        notice_view.setAdapter(adapter);

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("notice_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                String title = dataSnapshot.child("title").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                String content = dataSnapshot.child("content").getValue().toString();

                adapter.addItem(title, date, content);
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

    public void noticeRegisterButton(View v){
        Intent intent = new Intent(NoticeListActivity.this, NoticeRegisterActivity.class);
        startActivity(intent);
    }
}