package com.example.joint;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

public class ItemRegisterActivity extends AppCompatActivity {

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        TextView mT;

        public void setTextView(TextView date) {
            mT = date;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpDialog =  new DatePickerDialog(getActivity(), this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
            dpDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            return dpDialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            deadlineDate = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth +"일";
            mT.setText(deadlineDate);
        }
    }

    static String deadlineDate;

    FirebaseDatabase database;
    DatabaseReference reference;
    ProgressDialog progressDialog;

    // realtime db - id, name, deadlineDate, content, targetNum, price, discountPrice
    private static int itemListCnt = 3; // id
    EditText editTextName; // 제목
    TextView textDeadlineDate; // 마감일자
    EditText editTextContent; // 내용
    EditText editTextTargetNum; // 목표 개수
    EditText editTextPrice; // 정가
    EditText editTextDiscountPrice; // 할인가


    // storage - icon
    private static final int GALLERY_CODE = 10;
    private ImageView photo;
    Uri file;
    FirebaseStorage storage;
    StorageReference storageRef;
    StorageReference riversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_register);

        progressDialog = new ProgressDialog(this);

        // realtime db
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("item_list");

        editTextName = (EditText) findViewById(R.id.editItemName);
        textDeadlineDate = findViewById(R.id.deadlineDateText);
        editTextContent = (EditText) findViewById(R.id.editItemContent);
        editTextTargetNum = (EditText) findViewById(R.id.editItemTargetNum);
        editTextPrice = (EditText) findViewById(R.id.editItemPrice);
        editTextDiscountPrice = (EditText) findViewById(R.id.editItemDiscountPrice);


        // storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        photo = (ImageView)findViewById(R.id.uploadImageView);
        storage = FirebaseStorage.getInstance();
    }

    public void oneDadlineDateClick(View v){
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.setTextView(textDeadlineDate);
        dpf.show(getSupportFragmentManager(), "datePicker");
    }

    public void registerItemPost(View v){
        // id, name, icon, deadlineDate,  content, targetNum, currNum, price, discountPrice, creationDate
        String id = "id" + String.valueOf(itemListCnt);
        String name = editTextName.getText().toString().trim();
        String icon = "";
        // deadlineDate
        String content = editTextContent.getText().toString().trim();
        String targetNum = editTextTargetNum.getText().toString().trim();
        String currNum = "0";
        String price = editTextPrice.getText().toString().trim();
        String discountPrice = editTextDiscountPrice.getText().toString().trim();
        String creationDate = LocalDate.now().getYear() + "년 " + LocalDate.now().getMonthValue() + "월 " +
                LocalDate.now().getDayOfMonth() + "일 "+ LocalTime.now().getHour() + "시 " + LocalTime.now().getMinute() + "분";


        if(!editTextCheck(name, content, targetNum, price, discountPrice)) return; // 입력 텍스트 체크

//      progressDialog.setMessage("등록중입니다. 기다려 주세요...");
//      progressDialog.show();

        Item item = new Item(id, name, icon, deadlineDate,  content, targetNum, currNum, price, discountPrice, creationDate);
        reference.child(id).setValue(item);

        riversRef = storageRef.child(id + ".png");
        UploadTask uploadTask = riversRef.putFile(file);

        Toast.makeText(ItemRegisterActivity.this, "등록 성공", Toast.LENGTH_SHORT).show();
        itemListCnt++;
        finish();
        startActivity(new Intent(getApplicationContext(), ItemListActivity.class));
    }

    private boolean editTextCheck(String name, String content, String targetNum, String price, String discountPrice){

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(content)){
            Toast.makeText(this, "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(targetNum)){
            Toast.makeText(this, "목표개수를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(price)){
            Toast.makeText(this, "정가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(discountPrice)){
            Toast.makeText(this, "할인가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void onUploadImage(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE) {
            file = data.getData();
//            riversRef = storageRef.child("1.png");
//            UploadTask uploadTask = riversRef.putFile(file);

            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();
                photo.setImageBitmap(img);
            } catch (Exception e){
                e.printStackTrace();
            }

//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(ItemRegisterActivtiy.this, "사진이 정상적으로 업로드되지 않았습니다.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(ItemRegisterActivtiy.this, "사진이 정상적으로 업로드 되었습니다.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }
}
