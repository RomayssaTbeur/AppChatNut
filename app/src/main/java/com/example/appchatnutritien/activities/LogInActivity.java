package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appchatnutritien.R;
import com.example.appchatnutritien.databinding.ActivityLogInBinding;
import com.example.appchatnutritien.repository.MainRepository;
import com.example.appchatnutritien.utlities.Constants;
import com.example.appchatnutritien.utlities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private MainRepository mainRepository;
    private PreferenceManager preferenceManager;
    private String currentUser;
    private TextView txtForgotPassword ;
    private FirebaseAuth firebaseAuth;
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
        binding= ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainRepository = MainRepository.getInstance();
        setListeners();

        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
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

        firebaseAuth = FirebaseAuth.getInstance();
        String email=binding.inputemail.getText().toString();
        String password=binding.inputpassword.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                        String userauth = firebaseAuth.getCurrentUser().getUid();
                        setCurrentUser(userauth);

                        showToast("Connexion reussie");
//                        Intent intent = new Intent(getApplicationContext(), Recupereinfo.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
                        mainRepository.login(
                                getCurrentUser(),getApplicationContext(),()-> {

                                    //if success move to call
                                    startActivity(new Intent(LogInActivity.this, Recupereinfo.class));
                                }
                        );
                    } else {
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

        if (binding.inputemail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (binding.inputpassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }
}