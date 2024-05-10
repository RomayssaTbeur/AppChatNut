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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.CircularArray;

import com.example.appchatnutritien.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfilPatientActivity extends AppCompatActivity {
    private ImageView imageProfil;
    private Uri selectedImageUri;
    private Bitmap existingImageBitmap;
    EditText editName, editEmail, editPhone, editAddress, editAge, editPassword;
    Button editButton;
    ProgressBar progressBar;
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil_patient);

        db = FirebaseFirestore.getInstance();

        imageProfil = findViewById(R.id.ImageProfil);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editAge = findViewById(R.id.editAge);
        editPassword = findViewById(R.id.editPassword);
        editButton = findViewById(R.id.btnEdit);
        progressBar = findViewById(R.id.progressBar);

        // Ajouter un OnClickListener à l'image de profil pour sélectionner une nouvelle image dans la galerie
        imageProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
        // Récupérer les données du patient à partir de Firestore
        retrievePatientData();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mettre à jour les données du patient dans Firestore
                updatePatientData();
            }
        });
    }
    // Méthode pour sélectionner une image dans la galerie
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Méthode pour gérer le résultat de la sélection de l'image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                // Convertir l'URI de l'image en Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                // Afficher l'image sélectionnée dans l'ImageView
                imageProfil.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void retrievePatientData() {
        progressBar.setVisibility(View.VISIBLE);

        // Authentifier le patient
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Récupérer l'UID du patient authentifié
        String uid = currentUser.getUid();

        // Référence au document du patient dans Firestore
        DocumentReference docRef = db.collection("patients").document(uid);

        // Récupérer les données du document
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Récupérer les données du patient et les afficher dans les champs EditText
                        editName.setText(document.getString("name"));
                        editEmail.setText(document.getString("email"));
                        editPhone.setText(document.getString("phone"));
                        editAddress.setText(document.getString("address"));
                        editAge.setText(String.valueOf(document.getLong("age")));
                        editPassword.setText(document.getString("password"));

                        // Vérifier si une image est associée au patient
                        if (document.contains("photo")) {
                            // Récupérer l'image encodée en base64 depuis Firestore
                            String base64Image = document.getString("photo");

                            // Convertir la chaîne base64 en une image et l'afficher
                            if (base64Image != null && !base64Image.isEmpty()) {
                                existingImageBitmap = decodeBase64(base64Image);
                                if (existingImageBitmap != null) {
                                    // Afficher l'image dans votre interface utilisateur
                                    imageProfil.setImageBitmap(existingImageBitmap);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(EditProfilPatientActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfilPatientActivity.this, "Failed to retrieve document", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Méthode pour décoder une chaîne base64 en Bitmap
    public Bitmap decodeBase64(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

  private void updatePatientData() {
      progressBar.setVisibility(View.VISIBLE);

      // Récupérer les données saisies par l'utilisateur
      String newName = editName.getText().toString();
      String newEmail = editEmail.getText().toString();
      String newPhone = editPhone.getText().toString();
      String newAddress = editAddress.getText().toString();
      int newAge = Integer.parseInt(editAge.getText().toString());
      String newPassword = editPassword.getText().toString();

      // Authentifier le patient
      FirebaseAuth mAuth = FirebaseAuth.getInstance();
      FirebaseUser currentUser = mAuth.getCurrentUser();
      String uid = currentUser.getUid();
      DocumentReference docRef = db.collection("patients").document(uid);

      // Vérifier si aucune nouvelle image n'a été sélectionnée
      if (selectedImageUri == null && existingImageBitmap != null) {
          // Aucune nouvelle image sélectionnée, utiliser l'image existante

          String base64ExistingImage = encodeImage(existingImageBitmap);

              // Mettre à jour l'image du patient dans Firestore

              updateProfileImage(docRef, base64ExistingImage);


      } else {
          // Nouvelle image sélectionnée ou aucune image existante à utiliser
          if (selectedImageUri != null) {
              try { // Nouvelle image sélectionnée

                  Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri); // Convertir l'URI de l'image en Bitmap
                  String base64Image = encodeImage(bitmap); // Convertir le Bitmap en une chaîne Base64

                      // Mettre à jour l'image du patient dans Firestore
                      updateProfileImage(docRef, base64Image);


              } catch (IOException e) {
                  e.printStackTrace();
              }
          } else {

              Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
          }
      }


      docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if (task.isSuccessful()) {
                  DocumentSnapshot document = task.getResult();
                  if (document.exists()) {
                      // Récupérer les données existantes du patient
                      String existingName = document.getString("name");
                      String existingEmail = document.getString("email");
                      String existingPhone = document.getString("phone");
                      String existingAddress = document.getString("address");
                      int existingAge = document.getLong("age").intValue();
                      String existingPassword = document.getString("password");

                      // Vérifier si les données saisies par l'utilisateur sont différentes des données existantes
                      if (!newName.equals(existingName) ||
                              !newEmail.equals(existingEmail) ||
                              !newPhone.equals(existingPhone) ||
                              !newAddress.equals(existingAddress) ||
                              newAge != existingAge ||
                              !newPassword.equals(existingPassword)) {
                          // Créer un objet Patient avec les données mises à jour
                          Patient patient = new Patient(newName, newEmail, newPhone, newAddress, newAge, newPassword);
                          // Définir l'ID de l'utilisateur
                          String existingUserId = document.getString("userId");
                          patient.setUserId(existingUserId);

                          // Mettre à jour le document du patient dans Firestore

                          docRef.set(patient)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if (task.isSuccessful()) {
                                              Toast.makeText(EditProfilPatientActivity.this, "Patient data updated successfully", Toast.LENGTH_SHORT).show();
                                          } else {
                                              Toast.makeText(EditProfilPatientActivity.this, "Failed to update patient data", Toast.LENGTH_SHORT).show();
                                          }

                                          progressBar.setVisibility(View.GONE);
                                      }
                                  });

                      } else {
                          // Les données saisies par l'utilisateur sont identiques aux données existantes
                          Toast.makeText(EditProfilPatientActivity.this, "No changes detected", Toast.LENGTH_SHORT).show();
                          progressBar.setVisibility(View.GONE);
                      }
                  } else {
                      Toast.makeText(EditProfilPatientActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                      progressBar.setVisibility(View.GONE);
                  }
              } else {
                  Toast.makeText(EditProfilPatientActivity.this, "Failed to retrieve document", Toast.LENGTH_SHORT).show();
                  progressBar.setVisibility(View.GONE);
              }
          }
      });
  }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private void updateProfileImage(DocumentReference docRef, String base64Image) {
        // Mettre à jour l'image encodée en base64 dans le document du patient
        docRef.update("photo", base64Image)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfilPatientActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfilPatientActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}
