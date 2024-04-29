package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;
import com.example.appchatnutritien.databinding.ActivityLogInBinding;
import com.example.appchatnutritien.utlities.Constants;
import com.example.appchatnutritien.utlities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        //Enter to home page directly
//        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        }
        binding=ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.lienCreate.setOnClickListener(v ->startActivity(new Intent(getApplicationContext(),AccountMedActivity.class)));

        binding.btnLogin.setOnClickListener(v->{
            if(isValideLoginDetails()){
                LogIn();
            }
        });
    }

    private void LogIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_DOCTORS)
                .whereEqualTo(Constants.KEY_NAME,binding.inputname.getText().toString())
                .whereEqualTo(Constants.KEY_PHONE,binding.inputphone.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                        && task.getResult().getDocuments().size() > 0) {

                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        loading(false);
                        showToast("Unable to log in");

                    }
                });
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.btnLogin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValideLoginDetails() {
        if (binding.inputname.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputphone.getText().toString().trim().isEmpty()) {
            showToast("Enter phone number");
            return false;
        } else {
           return true;
        }
    }

}