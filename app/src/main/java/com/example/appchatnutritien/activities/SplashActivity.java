package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnPatient = findViewById(R.id.btnpatient);
        Button btnDoctor = findViewById(R.id.btndoctor);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        btnPatient.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE); // Afficher la ProgressBar
            // Simuler un traitement en arrière-plan pendant 2 secondes
            new Handler().postDelayed(() -> {
                /////////////////////////////////////////////////////////////////////////////////////////
                startActivity(new Intent(SplashActivity.this, LoginPatientActivity.class)); // Redirection vers l'interface de Login
                progressBar.setVisibility(View.GONE); // Masquer la ProgressBar une fois le traitement terminé
            }, 2000);
        });

        btnDoctor.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE); // Afficher la ProgressBar
            // Simuler un traitement en arrière-plan pendant 2 secondes
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, LogInActivity.class)); // Redirection vers l'interface de Login
                progressBar.setVisibility(View.GONE); // Masquer la ProgressBar une fois le traitement terminé
            }, 2000);
        });


    }


}