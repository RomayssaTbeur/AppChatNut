package com.example.appchatnutritien.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appchatnutritien.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfilMedecinActivity extends AppCompatActivity {
    private ImageView imageProfil;
    private Uri selectedImageUri;
    private Bitmap existingImageBitmap;
    private EditText editName, editEmail, editPhone, editMessagingPrice, editVoicePrice, editVideoPrice, editExperience, editPassword;
    private Button editButton;
    private ProgressBar progressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil_medecin);

        db = FirebaseFirestore.getInstance();

        imageProfil = findViewById(R.id.ImageProfil);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editMessagingPrice = findViewById(R.id.editMessagingPrice);
        editVoicePrice = findViewById(R.id.editVoicePrice);
        editVideoPrice = findViewById(R.id.editVideoPrice);
        editExperience = findViewById(R.id.editExperience);
        editPassword = findViewById(R.id.editPassword);
        editButton = findViewById(R.id.btnEdit);
        progressBar = findViewById(R.id.progressBar);

        imageProfil.setOnClickListener(v -> selectImageFromGallery());

        retrieveDoctorData();

        editButton.setOnClickListener(view -> updateDoctorData());
    }

    private void selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageProfil.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveDoctorData() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            DocumentReference docRef = db.collection("doctors").document(uid);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    editName.setText(document.getString("name"));
                    editEmail.setText(document.getString("email"));
                    editPhone.setText(document.getString("phoneNumber"));
                    editExperience.setText(document.getString("experiences"));
                    editMessagingPrice.setText(document.getString("priceMessaging"));
                    editVideoPrice.setText(document.getString("priceVideoCall"));
                    editVoicePrice.setText(document.getString("priceVoiceCall"));
                    editPassword.setText(document.getString("password"));

                    if (document.contains("image")) {
                        String base64Image = document.getString("image");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            existingImageBitmap = decodeBase64(base64Image);
                            if (existingImageBitmap != null) {
                                imageProfil.setImageBitmap(existingImageBitmap);
                            }
                        }
                    }
                } else {
                    Toast.makeText(EditProfilMedecinActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap decodeBase64(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void updateDoctorData() {
        progressBar.setVisibility(View.VISIBLE);

        String newName = editName.getText().toString();
        String newEmail = editEmail.getText().toString();
        String newPhone = editPhone.getText().toString();
        String newExperience = editExperience.getText().toString();
        String newMessagingPrice = editMessagingPrice.getText().toString();
        String newVideoPrice = editVideoPrice.getText().toString();
        String newVoicePrice = editVoicePrice.getText().toString();
        String newPassword = editPassword.getText().toString();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference docRef = db.collection("doctors").document(uid);

            if (selectedImageUri == null && existingImageBitmap != null) {
                String base64ExistingImage = encodeImage(existingImageBitmap);
                updateProfileImage(docRef, base64ExistingImage);
            } else if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    String base64Image = encodeImage(bitmap);
                    updateProfileImage(docRef, base64Image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    String existingName = document.getString("name");
                    String existingEmail = document.getString("email");
                    String existingPhone = document.getString("phoneNumber");
                    String existingExperience = document.getString("experiences");
                    String existingMessaging = document.getString("priceMessaging");
                    String existingVideoPrice = document.getString("priceVideoCall");
                    String existingVoicePrice = document.getString("priceVoiceCall");
                    String existingPassword = document.getString("password");

                    if (!newName.equals(existingName) ||
                            !newEmail.equals(existingEmail) ||
                            !newPhone.equals(existingPhone) ||
                            !newExperience.equals(existingExperience) ||
                            !newMessagingPrice.equals(existingMessaging) ||
                            !newVideoPrice.equals(existingVideoPrice) ||
                            !newVoicePrice.equals(existingVoicePrice) ||
                            !newPassword.equals(existingPassword)) {

                        Doctor doctor = new Doctor(newName, newEmail, newPhone, newExperience, newMessagingPrice, newVideoPrice, newVoicePrice, newPassword);
                        doctor.setUserId(uid);

                        docRef.set(doctor)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(EditProfilMedecinActivity.this, "Doctor data updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EditProfilMedecinActivity.this, "Failed to update doctor data", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(EditProfilMedecinActivity.this, "No changes detected", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(EditProfilMedecinActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void updateProfileImage(DocumentReference docRef, String base64Image) {
        docRef.update("image", base64Image)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfilMedecinActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfilMedecinActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}
