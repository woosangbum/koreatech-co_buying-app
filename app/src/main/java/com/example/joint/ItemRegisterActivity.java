package com.example.joint;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.HashMap;
import java.util.Map;

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
            deadlineDate = year + "??? " + (monthOfYear + 1) + "??? " + dayOfMonth +"???";
            mT.setText(deadlineDate);
        }
    }

    static String deadlineDate;

    FirebaseDatabase database;
    DatabaseReference reference;
    ProgressDialog progressDialog;

    // realtime db - id, name, deadlineDate, content, targetNum, price, discountPrice
    EditText editTextName; // ??????
    TextView textDeadlineDate; // ????????????
    EditText editTextContent; // ??????
    EditText editTextTargetNum; // ?????? ??????
    EditText editTextPrice; // ??????
    EditText editTextDiscountPrice; // ?????????


    // storage - icon
    private static final int GALLERY_CODE = 10;
    private ImageView photo;
    Uri file;
    FirebaseStorage storage;
    StorageReference storageRef;
    StorageReference riversRef;
    Bitmap img;

    DatabaseReference refCnt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_register);

        progressDialog = new ProgressDialog(this);

        // realtime db
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("item_list");
        refCnt = database.getReference("id_cnt_list");

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

    public void onDeadlineDateClick(View v){
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.setTextView(textDeadlineDate);
        dpf.show(getSupportFragmentManager(), "datePicker");
    }

    public void registerItemPost(View v){
        int itemCnt = Integer.valueOf(PreferenceManager.getString(getApplicationContext(), "itemCnt"));

        // id, name, icon, deadlineDate,  content, targetNum, currNum, price, discountPrice, creationDate
        String id = "item" + String.valueOf(itemCnt);
        String name = editTextName.getText().toString().trim();
        String icon = "item" + String.valueOf(itemCnt) + ".png";
        // deadlineDate
        String content = editTextContent.getText().toString().trim();
        String targetNum = editTextTargetNum.getText().toString().trim();
        String currNum = "0";
        String price = editTextPrice.getText().toString().trim();
        String discountPrice = editTextDiscountPrice.getText().toString().trim();
        String creationDate = LocalDate.now().getYear() + "??? " + LocalDate.now().getMonthValue() + "??? " +
                LocalDate.now().getDayOfMonth() + "??? "+ LocalTime.now().getHour() + "??? " + LocalTime.now().getMinute() + "???";

        if(!editTextCheck(name, content, targetNum, price, discountPrice)){
            Log.d("ddddd", "??????");
            return;
        }

        Item item = new Item(id, name, icon, deadlineDate, content, targetNum, currNum, price, discountPrice, creationDate);
        reference.child(id).setValue(item);

        riversRef = storageRef.child(id + ".png");
        Log.d("uri", file.toString());
        UploadTask uploadTask = riversRef.putFile(file);

        itemCnt++;
        PreferenceManager.setString(getApplicationContext(), "itemCnt", String.valueOf(itemCnt));
        Map<String, Object> hopperUpdateItem = new HashMap<>();
        hopperUpdateItem.put("itemCnt", String.valueOf(itemCnt));
        refCnt.updateChildren(hopperUpdateItem);

        ((ItemListActivity) ItemListActivity.context).showItemList();

        Toast.makeText(ItemRegisterActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private boolean editTextCheck(String name, String content, String targetNum, String price, String discountPrice){
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(content)){
            Toast.makeText(this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(targetNum)){
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(price)){
            Toast.makeText(this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(discountPrice)){
            Toast.makeText(this, "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(textDeadlineDate.getText().toString().equals("")) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(img == null){
            Log.d("ddddd", "?????? ??????");
            Toast.makeText(this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
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
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                photo.setImageBitmap(img);
            } catch (Exception e){
                e.printStackTrace();
            }

//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(ItemRegisterActivtiy.this, "????????? ??????????????? ??????????????? ???????????????.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(ItemRegisterActivtiy.this, "????????? ??????????????? ????????? ???????????????.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }
}

