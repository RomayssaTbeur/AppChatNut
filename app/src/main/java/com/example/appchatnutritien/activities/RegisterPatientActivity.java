package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatnutritien.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterPatientActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, ageEditText, passwordEditText, addressEditText, phoneEditText;
    private Button registerButton, selectImageButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ImageView imageView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_patient);

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        ageEditText = findViewById(R.id.age);
        passwordEditText = findViewById(R.id.password);
        addressEditText = findViewById(R.id.adresse);
        phoneEditText = findViewById(R.id.phone);
        registerButton = findViewById(R.id.button);
        firebaseAuth = FirebaseAuth.getInstance();
        selectImageButton = findViewById(R.id.select_image_button);
        db = FirebaseFirestore.getInstance();
        imageView = findViewById(R.id.image_view);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPatient();
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("patientData") && intent.hasExtra("selectedImageUri")) {
            Map<String, Object> patient = (Map<String, Object>) intent.getSerializableExtra("patientData");
            selectedImageUri = intent.getParcelableExtra("selectedImageUri");

            nameEditText.setText((String) patient.get("name"));
            emailEditText.setText((String) patient.get("email"));
            ageEditText.setText(String.valueOf((int) patient.get("age")));
            addressEditText.setText((String) patient.get("address"));
            phoneEditText.setText((String) patient.get("phone"));

            imageView.setImageURI(selectedImageUri);
        }
    }

    private void registerPatient() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        int age = Integer.parseInt(ageEditText.getText().toString());

        String password = passwordEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            Map<String, Object> patient = new HashMap<>();
                            patient.put("userId", userId);
                            patient.put("name", name);
                            patient.put("email", email);
                            patient.put("age", age);
                            patient.put("address", address);
                            patient.put("phone", phone);
                            patient.put("password", password);
                            if (selectedImageUri != null) {
                                patient.put("photo", encodedImage);
                            }

                            db.collection("patients")
                                    .document(userId)
                                    .set(patient)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterPatientActivity.this, "Patient ajouté avec succès", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterPatientActivity.this, HomePatientActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterPatientActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterPatientActivity.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            encodeImage();
        }
    }

    private void encodeImage() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            encodedImage = encodedImage(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encodedImage;
    }
}