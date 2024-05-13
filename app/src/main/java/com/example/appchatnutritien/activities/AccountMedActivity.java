package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;
import com.example.appchatnutritien.databinding.ActivityAccountMedBinding;
import com.example.appchatnutritien.databinding.ActivityLogInBinding;
import com.example.appchatnutritien.utlities.Constants;
import com.example.appchatnutritien.utlities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class AccountMedActivity extends AppCompatActivity {

    private ActivityAccountMedBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private EditText editTextEmail;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAccountMedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();

    }

    private void setListeners() {
        binding.lienLogin.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), LogInActivity.class)));
        binding.btnCreate.setOnClickListener(v->{
            if(isDataValide()){
                CreateAccount();
            }
        });
        binding.layoutImage.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void CreateAccount() {
        loading(true);
        editTextEmail =findViewById(R.id.email);
        String email= String.valueOf(editTextEmail.getText());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, String.valueOf(124554));
        FirebaseUser userauth = firebaseAuth.getCurrentUser();

        String userId = userauth.getUid();
        user.put(Constants.KEY_IMAGE,encodedImage);
        user.put(Constants.KEY_NAME,binding.inputname.getText().toString());
        user.put(Constants.KEY_PHONE,binding.inputTel.getText().toString());
        user.put(Constants.KEY_EXPERIENCE,binding.inputFormation.getText().toString());
        user.put(Constants.KEY_PRICE_CHAT,binding.inputPriceChat.getText().toString());
        user.put(Constants.KEY_PRICE_VOICE_CALL,binding.inputPriceVoice.getText().toString());
        user.put(Constants.KEY_PRICE_VIDEO_CALL,binding.inputPriceVideo.getText().toString());
        user.put(Constants.KEY_Email, email);
        user.put(Constants.KEY_USER_ID, userId);
        database.collection(Constants.KEY_COLLECTION_DOCTORS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());

                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputname.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE,binding.inputTel.getText().toString());
                    preferenceManager.putString(Constants.KEY_EXPERIENCE,binding.inputFormation.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_CHAT,binding.inputPriceChat.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_VOICE_CALL,binding.inputPriceVoice.getText().toString());
                    preferenceManager.putString(Constants.KEY_PRICE_VIDEO_CALL,binding.inputPriceVideo.getText().toString());

                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception->{
                    loading(false);
                    showToast(exception.getMessage());
                });



    }

    private boolean isDataValide() {
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
        } else if (binding.inputFormation.getText().toString().trim().isEmpty()) {
            showToast("Enter your experiences");
            return false;
        }else if (binding.inputPriceChat.getText().toString().trim().isEmpty()) {
            showToast("Enter price to messaging");
            return false;
        } else if (binding.inputPriceVoice.getText().toString().trim().isEmpty()) {
            showToast("Enter price to voice call");
            return false;
        } else if (binding.inputPriceVideo.getText().toString().trim().isEmpty()) {
            showToast("Enter price to video call");
            return false;
        } else {
            return true;
        }
    }

    private String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth /bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result->{
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.ImageProfil.setImageBitmap(bitmap);
                            binding.textImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }catch(FileNotFoundException e) {
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
        if(isLoading) {
            binding.btnCreate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.btnCreate.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }



}