package com.example.appchatnutritien.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatnutritien.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Recupereinfo extends AppCompatActivity {
    private RelativeLayout layout;
    private String[] recivesId;
    private FirebaseFirestore mbase;
    private int[] profileIds = {R.id.profile, R.id.profile3, R.id.profile4, R.id.profile5, R.id.profile6};
    private int[] firstNameIds = {R.id.firstname, R.id.firstname3, R.id.firstname4, R.id.firstname5, R.id.firstname6};
    //    private int[] phoneIds = {R.id.phone, R.id.phone3, R.id.phone4, R.id.phone5, R.id.phone6};
    private String convertContentUriToImageUrl(Context context, Uri contentUri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            return imagePath;
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        recivesId = new String[5];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userrecuper);
        mbase = FirebaseFirestore.getInstance();

        CollectionReference doctorCollection = mbase.collection("patients");
        layout=findViewById(R.id.bar1);
        doctorCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> doctorList = queryDocumentSnapshots.getDocuments();
            int size = Math.min(doctorList.size(), Math.min(profileIds.length, firstNameIds.length));

            for (int i = 0; i < size; i++) {
                DocumentSnapshot documentSnapshot = doctorList.get(i);

                String name = documentSnapshot.getString("name");
                String encodedImage = documentSnapshot.getString("photo");
                String userId = documentSnapshot.getString("userId");

                recivesId[i]=userId;
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ImageView profileImageView = findViewById(profileIds[i]);

                int targetWidth = profileImageView.getWidth();
                int targetHeight = profileImageView.getHeight();
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
                    profileImageView.setImageBitmap(resizedBitmap);
                }

                TextView firstNameTextView = findViewById(firstNameIds[i]);
                firstNameTextView.setText(name);
            }
        });
    }



    public void onRelativeLayoutClicked(View view) {
        int clickedLayoutId = view.getId();
        //relativelayout1
        if(clickedLayoutId == R.id.inter1){
            ImageView img1=findViewById(R.id.profile);
            TextView text1=findViewById(R.id.firstname);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);


            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[0]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout2
        if(clickedLayoutId == R.id.inter3){
            ImageView img1=findViewById(R.id.profile3);
            TextView text1=findViewById(R.id.firstname3);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[1]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout3
        if(clickedLayoutId == R.id.inter4){
            ImageView img1=findViewById(R.id.profile4);
            TextView text1=findViewById(R.id.firstname4);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[2]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout4
        if(clickedLayoutId == R.id.inter5){
            ImageView img1=findViewById(R.id.profile5);
            TextView text1=findViewById(R.id.firstname5);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[3]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout5
        if(clickedLayoutId == R.id.inter6){
            ImageView img1=findViewById(R.id.profile6);
            TextView text1=findViewById(R.id.firstname6);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("reciveId", recivesId[4]);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }




    }
}