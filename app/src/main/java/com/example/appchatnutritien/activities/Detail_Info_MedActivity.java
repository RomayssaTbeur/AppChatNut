package com.example.appchatnutritien.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;
import com.squareup.picasso.Picasso;

public class Detail_Info_MedActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info_med);

        // Récupérer les données du médecin depuis l'intent
        String doctorName = getIntent().getStringExtra("doctorName");
        String doctorExperience = getIntent().getStringExtra("doctorExperience");
        String encodedImage = getIntent().getStringExtra("doctorImage");
        String priceMessaging = getIntent().getStringExtra("priceMessaging");
        String priceVideoCall = getIntent().getStringExtra("priceVideoCall");
        String priceVoiceCall = getIntent().getStringExtra("priceVoiceCall");
        String doctorTele = getIntent().getStringExtra("doctorTele");
        String doctorEmail = getIntent().getStringExtra("doctorEmail");

        // Afficher les données du médecin dans vos vues
        TextView nameTextView = findViewById(R.id.titleName);
        nameTextView.setText(doctorName);
        TextView teleTextView = findViewById(R.id.titletele);
        teleTextView.setText(doctorTele); // Afficher le numéro de téléphone
        TextView emailTextView = findViewById(R.id.titleemail);
        emailTextView.setText(doctorEmail);
        TextView experienceTextView = findViewById(R.id.experiences);
        experienceTextView.setText(doctorExperience);

        TextView priceMessagingTextView = findViewById(R.id.priceMessaging);
        priceMessagingTextView.setText(priceMessaging+" DH");// Afficher le prix du messaging

        TextView priceVideoCallTextView = findViewById(R.id.priceVideoCall);
        priceVideoCallTextView.setText(priceVideoCall+" DH" );
        TextView priceVoiceCallTextView = findViewById(R.id.priceVoiceCall);
        priceVoiceCallTextView.setText(priceVoiceCall+" DH");

        // Afficher l'image du médecin
        ImageView imageView = findViewById(R.id.profileImg);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.profillogo);
        }
    }


}