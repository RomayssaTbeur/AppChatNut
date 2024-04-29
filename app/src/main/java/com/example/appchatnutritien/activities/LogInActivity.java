package com.example.appchatnutritien.activities;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatnutritien.R;
import com.example.appchatnutritien.databinding.ActivityLogInBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);

        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
           // Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            //return insets;
        //});
        binding=ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.lienCreate.setOnClickListener(v ->onBackPressed());
                //startActivity(new Intent(getApplicationContext(),AccountMedActivity.class)));
       // binding.btnLogin.setOnClickListener(v-> addDataToFireStore());
    }

   /* private void addDataToFireStore(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> data = new HashMap<>();
        data.put("first name", "Romaysa");
        data.put("last name", "Tbeur");

        database.collection("users").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(LogInActivity.this, "Data inserted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception ->{
                    Toast.makeText(LogInActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }*/
}