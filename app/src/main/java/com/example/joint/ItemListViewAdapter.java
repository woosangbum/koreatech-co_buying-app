package com.example.joint;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ItemListViewAdapter extends BaseAdapter {
    private ArrayList<Item> listViewItemList = new ArrayList<Item>() ;
    Context context;

    public void ListViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }
    public String getCountString() {
        return String.valueOf(listViewItemList.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImageView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.name) ;
        TextView deadlineDateTextView = (TextView) convertView.findViewById(R.id.deadlinedate) ;


        Item listViewItem = listViewItemList.get(position);
        nameTextView.setText(listViewItem.getName());
        deadlineDateTextView.setText(listViewItem.getDeadlineDate());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef.child(listViewItem.getIcon()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context.getApplicationContext())
                                .load(uri)
                                .into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context.getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                    }
                });

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(String id, String name, String icon, String deadlineDate,
                        String content, String targetNum, String currNum, String price, String discountPrice, String creationDate) {
        Item item = new Item(id, name, icon, deadlineDate, content, targetNum, currNum, price, discountPrice, creationDate);

        listViewItemList.add(item);
    }
}