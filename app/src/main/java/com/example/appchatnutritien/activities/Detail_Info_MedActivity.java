package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Detail_Info_MedActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userId;
    private String recivesId;
    private Button button;
    private Button btnPayerMessaging;
    private String priceMessaging;
    private String priceVideoCall;
    private String Service;
    private String priceVoiceCall;
    String clientId;
    private FirebaseFirestore db;
    int PAYPAL_REQUEST_CODE = 123;
    public static PayPalConfiguration configuration;
    private Button btnPayerVoiceCall;
    private Button btnPayerVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_detail_info_med);
        Intent chatIntent = new Intent(this, ChatHomeActivity.class);
        db = FirebaseFirestore.getInstance();
        String doctorName = getIntent().getStringExtra("doctorName");
        String doctorExperience = getIntent().getStringExtra("doctorExperience");
        String encodedImage = getIntent().getStringExtra("doctorImage");
        priceMessaging = getIntent().getStringExtra("priceMessaging");
        priceVideoCall = getIntent().getStringExtra("priceVideoCall");
        priceVoiceCall = getIntent().getStringExtra("priceVoiceCall");
        String doctorTele = getIntent().getStringExtra("doctorTele");
        String doctorEmail = getIntent().getStringExtra("doctorEmail");
        recivesId = getIntent().getStringExtra("userId");
        clientId = "AXN2ua6-GoKNa6wErt3KokEfK0GTXSG6cLimdK3xXJVE7q2DzkkW07wIPf-444PEDchGQ3SBZfZSK56j";
        configuration = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(clientId);
        super.onCreate(savedInstanceState);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        date = calendar.getTime();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            finish();

        }

        button = findViewById(R.id.messageButton);
        btnPayerMessaging = findViewById(R.id.btnPayerMessage);
        btnPayerVideoCall = findViewById(R.id.btnPayerVideoCall);
        btnPayerVoiceCall = findViewById(R.id.btnPayerVoiceCall);
        db.collection("Payments")
                .whereEqualTo("Doctor", recivesId)
                .whereEqualTo("Patient", userId) .whereEqualTo("Service","Messaging")

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count>0){
                        button.setVisibility(View.VISIBLE);
                        btnPayerMessaging.setVisibility(View.INVISIBLE);

                    }
                    else{
                        button.setVisibility(View.INVISIBLE);
                        btnPayerMessaging.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                });
        db.collection("Payments")
                .whereEqualTo("Doctor", recivesId)
                .whereEqualTo("Patient", userId) .whereEqualTo("Service","VideoCall")

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count>0){

                        btnPayerVideoCall.setVisibility(View.INVISIBLE);

                    }
                    else{

                        btnPayerVideoCall.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                });
        db.collection("Payments")
                .whereEqualTo("Doctor", recivesId)
                .whereEqualTo("Patient", userId) .whereEqualTo("Service","VoiceCall")

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count>0){

                        btnPayerVoiceCall.setVisibility(View.INVISIBLE);

                    }
                    else{

                        btnPayerVoiceCall.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                });



        btnPayerMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service = "Messaging";
                getPayment(priceMessaging);

            }
        });

        btnPayerVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service = "VoiceCall";
                getPayment(priceVoiceCall);

            }
        });

        btnPayerVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service = "VideoCall";
                getPayment(priceVideoCall);

            }
        });



        TextView nameTextView = findViewById(R.id.titleName);
        nameTextView.setText(doctorName);
        TextView teleTextView = findViewById(R.id.titletele);
        teleTextView.setText(doctorTele);
        TextView emailTextView = findViewById(R.id.titleemail);
        emailTextView.setText(doctorEmail);
        TextView experienceTextView = findViewById(R.id.experiences);
        experienceTextView.setText(doctorExperience);

        TextView priceMessagingTextView = findViewById(R.id.priceMessaging);
        priceMessagingTextView.setText(priceMessaging + " DOLAR");

        TextView priceVideoCallTextView = findViewById(R.id.priceVideoCall);
        priceVideoCallTextView.setText(priceVideoCall + " DOLAR");

        TextView priceVoiceCallTextView = findViewById(R.id.priceVoiceCall);
        priceVoiceCallTextView.setText(priceVoiceCall + " DOLAR");

        ImageView imageView = findViewById(R.id.profileImg);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.profillogo);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatIntent.putExtra("reciveId", recivesId);
                chatIntent.putExtra("name", "Dr."+doctorName);
                startActivity(chatIntent);
            }
        });
    }

    private void getPayment(String montant) {
                Map<String, Object> paymentCollection = new HashMap<>();
        paymentCollection.put("Patient", userId);
        paymentCollection.put("Doctor", recivesId);
        paymentCollection.put("Service", Service);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        date = calendar.getTime();
        paymentCollection.put("date",date);
        db.collection("Payments").document().set(paymentCollection)
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {

                });



        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(montant)), "USD","NutDoctors", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, configuration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation paymentConfirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (paymentConfirmation != null) {
                    String paymentDetails = paymentConfirmation.toJSONObject().toString();
                    try {
                        JSONObject object = new JSONObject(paymentDetails);
                    } catch (JSONException e) {
                        Toast.makeText(this, "ERREUR1 AU OPERATION DE PAYMENT", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "ERREUR2 AU OPERATION DE PAYMENT", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(this, "ERREUR3 AU OPERATION DE PAYMENT", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
