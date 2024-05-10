package com.example.appchatnutritien.activities;


import com.example.appchatnutritien.R;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginPatientActivity extends AppCompatActivity {

    private EditText nameEditText, passwordEditText;
    private TextView textView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_patient);

        nameEditText = findViewById(R.id.name);
        passwordEditText = findViewById(R.id.password);
        textView = findViewById(R.id.textView);
        db = FirebaseFirestore.getInstance();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this,HomePatientActivity.class);
        startActivity(intent);
    }
    private void goToRegister() {
        Intent intent = new Intent(this,RegisterPatientActivity.class);
        startActivity(intent);
    }
    public void login(View view) {
        String email = nameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginPatientActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Toast.makeText(LoginPatientActivity.this, "Adresse e-mail ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}