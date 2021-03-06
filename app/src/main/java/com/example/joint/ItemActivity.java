package com.example.joint;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ItemActivity extends AppCompatActivity {

    TextView tvName;
    ImageView img;
    TextView tvDeadlineDate;
    TextView tvContent;
    TextView tvTargetNum;
    TextView tvCurrNum;
    TextView tvPrice;
    TextView tvDiscountPrice;
    TextView tvCreationDate;
    TextView textViewComputePrice;
    TextView itemCount;

    FirebaseStorage storage;

    private String itemId;
    private String productPrice;

    private int userPurchaseNum = 1;
    private int updateCurrNum;

    FirebaseDatabase database;
    DatabaseReference refPur;
    DatabaseReference refItem;
    DatabaseReference refCnt;
    DatabaseReference refNot;

    private String studentId;

    Button buyButton;
    ImageView imageViewMinus;
    ImageView imageViewPlus;

    // 물품 게시글 상세글
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        buyButton = (Button) findViewById(R.id.buyButton);
        imageViewMinus = findViewById(R.id.imageViewMinus);
        imageViewPlus = findViewById(R.id.imageViewPlus);

        database = FirebaseDatabase.getInstance();
        refPur = database.getReference("user_purchase");
        refItem = database.getReference("item_list");
        refCnt = database.getReference("id_cnt_list");
        refNot = database.getReference("notification_list");

        studentId = PreferenceManager.getString(getApplicationContext(), "studentId");
        storage = FirebaseStorage.getInstance();
        tvName = (TextView) findViewById(R.id.item_name);
        img = (ImageView) findViewById(R.id.item_image);
        tvDeadlineDate = (TextView) findViewById(R.id.item_deadlineDate);
        tvContent = (TextView) findViewById(R.id.item_content);
        tvTargetNum = (TextView) findViewById(R.id.item_targetNum);
        tvCurrNum = (TextView) findViewById(R.id.item_currNum);
        tvPrice = (TextView) findViewById(R.id.item_price);
        tvDiscountPrice = (TextView) findViewById(R.id.item_discountPrice);
        tvCreationDate = (TextView) findViewById(R.id.item_creationDate);
        textViewComputePrice = (TextView) findViewById(R.id.textViewComputePrice);
        itemCount = (TextView) findViewById(R.id.item_count);
        userPurchaseNum = Integer.parseInt(itemCount.getText().toString());


        Intent intent = getIntent(); // 보내온 Intent를 얻는다
        tvName.setText(intent.getStringExtra("name"));
        tvDeadlineDate.setText(intent.getStringExtra("deadlineDate"));
        tvContent.setText(intent.getStringExtra("content"));
        tvTargetNum.setText(intent.getStringExtra("targetNum"));
        tvCurrNum.setText(intent.getStringExtra("currNum"));
        tvPrice.setText(intent.getStringExtra("price"));
        tvDiscountPrice.setText(intent.getStringExtra("discountPrice"));
        tvCreationDate.setText(intent.getStringExtra("creationDate"));
        itemId = intent.getStringExtra("itemId");

        productPrice = String.valueOf(Integer.parseInt(tvDiscountPrice.getText().toString()) / Integer.parseInt(tvTargetNum.getText().toString()));
        textViewComputePrice.setText(productPrice);

        int prevNum = Integer.valueOf(tvCurrNum.getText().toString());
        updateCurrNum = prevNum + userPurchaseNum;

        Log.d("product", tvDiscountPrice.getText().toString());
        Log.d("product", tvTargetNum.getText().toString());
        Log.d("product", productPrice);
        Log.d("product", itemId);

        if (studentId.equals("root") || tvCurrNum.getText().toString().equals(tvTargetNum.getText().toString())) {
            itemCount.setVisibility(View.INVISIBLE);
            itemCount.setEnabled(false);
            imageViewPlus.setVisibility(View.INVISIBLE);
            imageViewPlus.setEnabled(false);
            imageViewMinus.setVisibility(View.INVISIBLE);
            imageViewMinus.setEnabled(false);
            buyButton.setVisibility(View.INVISIBLE);
            buyButton.setEnabled(false);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef.child(intent.getStringExtra("icon")).getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(getApplicationContext())
                        .load(uri)
                        .into(img)).addOnFailureListener(exception -> Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show());


    }

    public void onClickPurchasePost(View v) {
        int noCnt = Integer.valueOf(PreferenceManager.getString(getApplicationContext(), "notificationCnt"));
        int purCnt = Integer.valueOf(PreferenceManager.getString(getApplicationContext(), "purchaseCnt"));

        String id = "purchase" + purCnt;
        String studentId = PreferenceManager.getString(getApplicationContext(), "studentId");
        String productCount = ((TextView) findViewById(R.id.item_count)).getText().toString();
        String isReceipt = "false";
        String purchaseDate = LocalDate.now().getYear() + "년 " + LocalDate.now().getMonthValue() + "월 " +
                LocalDate.now().getDayOfMonth() + "일";

        //id(o) , studentId, productId(o), productCount, productPrice, isReceipt, purchaseDate;
        UserPurchase userPurchase = new UserPurchase(id, studentId, itemId, productCount, productPrice, isReceipt, purchaseDate);
        refPur.child(id).setValue(userPurchase);

        // 구매 id 업데이트
        purCnt++;
        PreferenceManager.setString(getApplicationContext(), "purchaseCnt", String.valueOf(purCnt));
        Map<String, Object> hopperUpdatePur = new HashMap<>();
        hopperUpdatePur.put("purchaseCnt", String.valueOf(purCnt));
        refCnt.updateChildren(hopperUpdatePur);

        // 아이템 현재 개수 업데이트
        DatabaseReference hopperRef = refItem.child(itemId);
        Map<String, Object> hopperUpdateItem = new HashMap<>();
        hopperUpdateItem.put("currNum", String.valueOf(updateCurrNum));
        hopperRef.updateChildren(hopperUpdateItem);


        if (updateCurrNum == Integer.valueOf(tvTargetNum.getText().toString())) { // 목표 개수 == 현재 개수 -> 관리자 알림
            DatabaseReference hopperRefNot = refNot.child("notification" + noCnt);
            Map<String, Object> hopperUpdateNot = new HashMap<>();
            hopperUpdateNot.put("content", tvName.getText().toString() + "의 목표 개수를 달성하였습니다.");
            hopperUpdateNot.put("date", purchaseDate);
            hopperUpdateNot.put("studentId", "root");
            hopperRefNot.updateChildren(hopperUpdateNot);
            noCnt++;

            PreferenceManager.setString(getApplicationContext(), "notificationCnt", String.valueOf(noCnt));
            Map<String, Object> hopperUpdateCnt = new HashMap<>();
            hopperUpdateCnt.put("notificationCnt", String.valueOf(noCnt));
            refCnt.updateChildren(hopperUpdateCnt);
        }

        DialogInterface.OnClickListener buy = (dialog, which) -> {
            Toast.makeText(getApplicationContext(), "구매되었습니다.", Toast.LENGTH_SHORT).show();
            Toast.makeText(ItemActivity.this, "구매 성공", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(getApplicationContext(), ItemListActivity.class));
        };
        DialogInterface.OnClickListener cancel = (dialog, which) -> Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();

        new AlertDialog.Builder(this)
                .setTitle("구매하시겠습니까?")
                .setPositiveButton("구매", buy)
                .setNegativeButton("취소", cancel)
                .show();

//        Toast.makeText(ItemActivity.this, "구매 성공", Toast.LENGTH_SHORT).show();
//        finish();
//        startActivity(new Intent(getApplicationContext(), ItemListActivity.class));
    }

    public void onClickPlusMinus(@NonNull View v) {
        if (updateCurrNum == Integer.parseInt(tvTargetNum.getText().toString())) {
            Toast.makeText(getApplicationContext(), "개수를 더 담을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (R.id.imageViewMinus == v.getId()) {
            if (userPurchaseNum > 1) {
                userPurchaseNum -= 1;
                updateCurrNum -= 1;
            } else
                Toast.makeText(getApplicationContext(), "더 이상 인원 수를 줄일 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        if (R.id.imageViewPlus == v.getId()) {
            userPurchaseNum += 1;
            updateCurrNum += 1;
        }
        itemCount.setText(Integer.toString(userPurchaseNum));

    }
}