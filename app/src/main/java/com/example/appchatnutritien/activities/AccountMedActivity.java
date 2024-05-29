package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatnutritien.R;
import com.example.appchatnutritien.databinding.ActivityAccountMedBinding;
import com.example.appchatnutritien.utlities.Constants;
import com.example.appchatnutritien.utlities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AccountMedActivity extends AppCompatActivity {

    private ActivityAccountMedBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountMedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.lienLogin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LogInActivity.class)));
        binding.btnCreate.setOnClickListener(v -> {
            if (isDataValid()) {
                createAccount();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void createAccount() {
        loading(true);

        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email and password must not be empty");
            loading(false);
            return;
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            saveUserDataToFirestore(userId, email);

                        }
                    } else {
                        loading(false);
                        showToast(task.getException().getMessage());
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String email) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_IMAGE, encodedImage);
        user.put(Constants.KEY_NAME, binding.inputname.getText().toString());
        user.put(Constants.KEY_PHONE, binding.inputTel.getText().toString());
        user.put(Constants.KEY_EXPERIENCE, binding.inputFormation.getText().toString());
        user.put(Constants.KEY_PRICE_CHAT, binding.inputPriceChat.getText().toString());
        user.put(Constants.KEY_PRICE_VOICE_CALL, binding.inputPriceVoice.getText().toString());
        user.put(Constants.KEY_PRICE_VIDEO_CALL, binding.inputPriceVideo.getText().toString());
        user.put(Constants.KEY_USER_ID, userId);
        user.put(Constants.KEY_EMAIL, email); // Save email
        user.put(Constants.KEY_PASSWORD, binding.password.getText().toString()); // Save password
        // Ajouter les champs ratingsSum, ratingsCount et averageRating
        user.put("ratingsSum", 0.0);
        user.put("ratingsCount", 0L);
        user.put("averageRating", 0.0);

        database.collection(Constants.KEY_COLLECTION_DOCTORS)
                .document(userId)  // Use userId as the document ID
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, userId);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputname.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE, binding.inputTel.getText().toString());
                    preferenceManager.putString(Constants.KEY_EXPERIENCE, binding.inputFormation.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_CHAT, binding.inputPriceChat.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_VOICE_CALL, binding.inputPriceVoice.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_VIDEO_CALL, binding.inputPriceVideo.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, email); // Save email to preferences
                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.password.getText().toString()); // Save password to preferences

                    Intent intent = new Intent(getApplicationContext(), Recupereinfo.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }


    private boolean isDataValid() {
        if (encodedImage == null) {
            showToast("Select profile Image");
            return false;
        } else if (binding.inputname.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputTel.getText().toString().trim().isEmpty()) {
            showToast("Enter phone number");
            return false;
        } else if (binding.inputFormation.getText().toString().trim().isEmpty()) {
            showToast("Enter your experiences");
            return false;
        } else if (binding.inputPriceChat.getText().toString().trim().isEmpty()) {
            showToast("Enter price for messaging");
            return false;
        } else if (binding.inputPriceVoice.getText().toString().trim().isEmpty()) {
            showToast("Enter price for voice call");
            return false;
        } else if (binding.inputPriceVideo.getText().toString().trim().isEmpty()) {
            showToast("Enter price for video call");
            return false;
        } else {
            return true;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.ImageProfil.setImageBitmap(bitmap);
                            binding.textImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void showToast(String message) {
        Toast.makeText(AccountMedActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnCreate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnCreate.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}