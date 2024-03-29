package com.example.joint;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyprofileActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    // Front End (bottom_menu)
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;
    private Button buttonLogout;
    private TextView textviewDelete;

    private FirebaseUser userDB;

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        TextView textViewProfileName = findViewById(R.id.textViewProfileName);
        TextView textViewProfilePhoneNumber = findViewById(R.id.textViewProfilePhoneNumber);
        TextView textViewProfileEmail = findViewById(R.id.textViewProfileEmail);

        String studentId = PreferenceManager.getString(getApplicationContext(), "studentId");
        //관리자이면 주문내역 버튼과 이름만 보이도록(이메일, 핸드폰 번호 제외) 설정 변경
        if(studentId.equals("root")) {
            Button buttonOrder = findViewById(R.id.buttonOrder);
//            TextView textView7 = findViewById(R.id.textView7);
//            TextView textView8 = findViewById(R.id.textView8);

//            textView7.setVisibility(View.INVISIBLE);
//            textView8.setVisibility(View.INVISIBLE);
            textViewProfileEmail.setVisibility(View.INVISIBLE);
            textViewProfilePhoneNumber.setVisibility(View.INVISIBLE);

//            textView7.setEnabled(false);
//            textView8.setEnabled(false);
            textViewProfileEmail.setEnabled(false);
            textViewProfilePhoneNumber.setEnabled(false);

            textViewProfileName.setText("관리자");
            buttonOrder.setText("주문 내역");
        }
        else {
            FirebaseDatabase.getInstance().getReference().child("user").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (studentId.equals(snapshot.child("studentId").getValue().toString())) {
                            textViewProfilePhoneNumber.setText(snapshot.child("phoneNumber").getValue().toString());
                            textViewProfileEmail.setText(snapshot.child("email").getValue().toString());
                            textViewProfileName.setText(snapshot.child("name").getValue().toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        buttonLogout.setOnClickListener(this);

        // Front End
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch(item.getItemId()){
                    case R.id.action_home:
                        intent = new Intent(MyprofileActivity.this, ItemListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notice:
                        intent = new Intent(MyprofileActivity.this, NoticeListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_profile:
                        intent = new Intent(MyprofileActivity.this, MyprofileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_notification:
                        intent = new Intent(MyprofileActivity.this, NotificationActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    // 관리자 - 주문 내역 액티비티(RootOrderHistoryActivity), 사용자 - 구매 내역 액티비티(UserPurchaseHistoryActivity)
    public void onClickOrder(View v){
//         주문내역(관리자)
        String StudentId = PreferenceManager.getString(getApplicationContext(), "studentId");
        if(StudentId.equals("root")) {
            Intent intent = new Intent(MyprofileActivity.this, RootOrderHistoryActivity.class);
            startActivity(intent);
        // 구매내역(사용자)
        }else{
            Intent intent = new Intent(MyprofileActivity.this, UserPurchaseHistoryActivity.class);
            intent.putExtra("student_id", StudentId);
            startActivity(intent);
        }
    }

//    public void onClickHome(View v){
//        Intent intent = new Intent(MyprofileActivity.this, ItemListActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickNotice(View v){
//        Intent intent = new Intent(MyprofileActivity.this, NoticeListActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickMyProfile(View v){
//        Intent intent = new Intent(MyprofileActivity.this, MyprofileActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickNotification(View v){
//        Intent intent = new Intent(MyprofileActivity.this, NotificationActivity.class);
//        startActivity(intent);
//    }

    @Override
    public void onClick(View view) {
        if (view == buttonLogout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
//            PreferenceManager.clear(getApplicationContext()); // 로그인 정보 유지 데이터 삭제
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄움.
        if(view == textviewDelete) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MyprofileActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(MyprofileActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        }
                                    });
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MyprofileActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        }
    }

}
