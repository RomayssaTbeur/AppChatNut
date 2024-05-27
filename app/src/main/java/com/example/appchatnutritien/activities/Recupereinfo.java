package com.example.appchatnutritien.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import static android.content.ContentValues.TAG;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.EventListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.appchatnutritien.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;
import java.util.Map;

public class Recupereinfo extends AppCompatActivity {
    private RelativeLayout layout;
    private String[] recivesId;
    private FirebaseFirestore mbase;
    private FirebaseFirestore db;
    private int[] profileIds = {R.id.profile, R.id.profile3, R.id.profile4, R.id.profile5, R.id.profile6};
    private int[] firstNameIds = {R.id.firstname, R.id.firstname3, R.id.firstname4, R.id.firstname5, R.id.firstname6};
    //    private int[] phoneIds = {R.id.phone, R.id.phone3, R.id.phone4, R.id.phone5, R.id.phone6};
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView menuIcon;
    private NavigationView navigationView;
    private String convertContentUriToImageUrl(Context context, Uri contentUri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            return imagePath;
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        recivesId = new String[5];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userrecuper);
        mbase = FirebaseFirestore.getInstance();

        CollectionReference doctorCollection = mbase.collection("patients");
        layout=findViewById(R.id.bar1);
        doctorCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> doctorList = queryDocumentSnapshots.getDocuments();
            int size = Math.min(doctorList.size(), Math.min(profileIds.length, firstNameIds.length));

            for (int i = 0; i < size; i++) {
                DocumentSnapshot documentSnapshot = doctorList.get(i);

                String name = documentSnapshot.getString("name");
                String encodedImage = documentSnapshot.getString("photo");
                String userId = documentSnapshot.getString("userId");

                recivesId[i]=userId;
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ImageView profileImageView = findViewById(profileIds[i]);

                int targetWidth = profileImageView.getWidth();
                int targetHeight = profileImageView.getHeight();
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
                    profileImageView.setImageBitmap(resizedBitmap);
                }

                TextView firstNameTextView = findViewById(firstNameIds[i]);
                firstNameTextView.setText(name);
            }
        });
        db = FirebaseFirestore.getInstance();
        // Initialisation de l'interface utilisateur
        setupUI();

        // Affichage des informations du médecin
        displayDoctorInfo();
        setupFirestoreListener();
    }

    private void setupUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setVisibility(View.VISIBLE); // Rendre la NavigationView visible

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().hide();

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
                displayDoctorInfo();
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }private void displayDoctorInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("doctors").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String doctorName = document.getString("name");
                        String encodedImage = document.getString("image");

                        // Affichage du nom du médecin dans le header
                        TextView titleName = navigationView.getHeaderView(0).findViewById(R.id.titleName);
                        titleName.setText(doctorName);

                        // Affichage de l'image du médecin dans le header
                        ImageView profileImg = navigationView.getHeaderView(0).findViewById(R.id.profileImg);
                        if (encodedImage != null && !encodedImage.isEmpty()) {
                            byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            profileImg.setImageBitmap(bitmap);
                        } else {
                            profileImg.setImageResource(R.drawable.profillogo); // Image par défaut si aucune image n'est disponible
                        }
                    }
                } else {
                    Log.e("Firestore", "Failed to retrieve doctor data", task.getException());
                }
            });
        }
    }
    private void setupFirestoreListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("doctors").document(userId);

            ((DocumentReference) docRef).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "Current data: " + snapshot.getData());
                        // Le document existe, récupérer les données du patient
                        String patientName = snapshot.getString("name");
                        String encodedImage = snapshot.getString("image");

                        // Afficher les informations du patient dans l'interface utilisateur
                        TextView titleName = findViewById(R.id.titleName);
                        titleName.setText(patientName);

                        ImageView profileImg = findViewById(R.id.profileImg);
                        if (encodedImage != null && !encodedImage.isEmpty()) {
                            byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            profileImg.setImageBitmap(bitmap);
                        } else {
                            profileImg.setImageResource(R.drawable.profillogo);
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }
    }
    /***  private void displayDoctorInfo() {
        Log.d("Firestore", "Starting to fetch doctor data");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("doctors").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String doctorName = document.getString("name");
                        String encodedImage = document.getString("image");

                        // Affichage du nom du médecin dans le header
                        TextView titleName = navigationView.getHeaderView(0).findViewById(R.id.titleName);
                        titleName.setText(doctorName);

                        // Affichage de l'image du médecin dans le header
                        ImageView profileImg = navigationView.getHeaderView(0).findViewById(R.id.profileImg);
                        if (encodedImage != null && !encodedImage.isEmpty()) {
                            byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            profileImg.setImageBitmap(bitmap);
                        } else {
                            profileImg.setImageResource(R.drawable.profillogo); // Image par défaut si aucune image n'est disponible
                        }
                    }Log.d("Firestore", "Doctor data retrieval successful");
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Log.e("Firestore", "Failed to retrieve doctor data", task.getException());

                }
            });
        }
    }****/
/*private void displayDoctorInfo() {
       Log.d("Firestore", "Starting to fetch all doctors data");

       db.collection("doctors").get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               for (DocumentSnapshot document : task.getResult()) {
                   String doctorName = document.getString("name");
                   String encodedImage = document.getString("image");

                   // Affichage du nom du médecin dans le header
                   // Vous devrez modifier votre mise en page pour afficher plusieurs médecins
                   Log.d("Doctor Info", "Name: " + doctorName);

                   // Affichage de l'image du médecin dans le header
                   // Vous devrez modifier votre mise en page pour afficher plusieurs médecins
                   if (encodedImage != null && !encodedImage.isEmpty()) {
                       byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
                       Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                       // Affichez l'image comme vous le faites déjà
                   } else {
                       // Image par défaut si aucune image n'est disponible
                   }
               }
               Log.d("Firestore", "All doctors data retrieval successful");
           } else {
               Log.e("Firestore", "Failed to retrieve all doctors data", task.getException());
           }
       });
   }
*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRelativeLayoutClicked(View view) {
        int clickedLayoutId = view.getId();
        //relativelayout1
        if(clickedLayoutId == R.id.inter1){
            ImageView img1=findViewById(R.id.profile);
            TextView text1=findViewById(R.id.firstname);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);


            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[0]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout2
        if(clickedLayoutId == R.id.inter3){
            ImageView img1=findViewById(R.id.profile3);
            TextView text1=findViewById(R.id.firstname3);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[1]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout3
        if(clickedLayoutId == R.id.inter4){
            ImageView img1=findViewById(R.id.profile4);
            TextView text1=findViewById(R.id.firstname4);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[2]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout4
        if(clickedLayoutId == R.id.inter5){
            ImageView img1=findViewById(R.id.profile5);
            TextView text1=findViewById(R.id.firstname5);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("reciveId", recivesId[3]);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }
        //relativelayout5
        if(clickedLayoutId == R.id.inter6){
            ImageView img1=findViewById(R.id.profile6);
            TextView text1=findViewById(R.id.firstname6);
            Drawable imageDrawable = img1.getDrawable();
            Bitmap imageBitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            String name = text1.getText().toString();
            Intent chatIntent =new Intent(this,ChatHomeActivity.class);
            chatIntent.putExtra("reciveId", recivesId[4]);
            chatIntent.putExtra("image", imageBitmap);
            chatIntent.putExtra("name", name);
            startActivity(chatIntent);

        }




    }
    public void edit(View view) {
        // Créer un Intent pour démarrer votre nouvelle activité
        Intent intent = new Intent(this, EditProfilMedecinActivity.class);
        // Démarrer l'activité
        startActivity(intent);
    }
    public void logout(View view) {
        logoutMenu(Recupereinfo.this);
    }
    private void logoutMenu(Recupereinfo recupereinfo){
        AlertDialog.Builder builder =new AlertDialog.Builder(recupereinfo);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout ? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void delete(View view) {
        dodelete();
    }

    private void dodelete() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting account...");
        progressDialog.show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Supprimer le document correspondant dans Firestore
            FirebaseFirestore.getInstance().collection("doctors").document(uid)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Supprimer le compte utilisateur dans Authentication
                            currentUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(Recupereinfo.this, AccountMedActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Gérer les erreurs lors de la suppression du compte
                                                progressDialog.dismiss();
                                                Toast.makeText(Recupereinfo.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}