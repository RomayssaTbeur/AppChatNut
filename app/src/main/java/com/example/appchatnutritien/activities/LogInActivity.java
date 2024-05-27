package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatnutritien.databinding.ActivityLogInBinding;
import com.example.appchatnutritien.utlities.Constants;
import com.example.appchatnutritien.utlities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private PreferenceManager preferenceManager;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        progressBar = binding.progressBar;

        setListeners();
    }

    private void setListeners() {
        binding.lienCreate.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AccountMedActivity.class)));

        binding.btnLogin.setOnClickListener(this::logIn);
    }

    private void logIn(View view) {
        String email = binding.email.getText().toString().trim();
        String password = binding.inputpassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        loading(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            checkUserInFirestore(firebaseUser.getUid());
                        }
                    } else {
                        loading(false);
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(String userId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_DOCTORS)
                .document(userId)  // Use userId as the document ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, userId);
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), Recupereinfo.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        Toast.makeText(this, "Utilisateur introuvable dans Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.btnLogin.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isValideLoginDetails() {
        if (binding.email.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.inputpassword.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}