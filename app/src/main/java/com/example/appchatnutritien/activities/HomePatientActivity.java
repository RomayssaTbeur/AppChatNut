package com.example.appchatnutritien.activities;

import static android.content.ContentValues.TAG;

import com.example.appchatnutritien.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.content.res.ColorStateList;
import android.widget.Toast;

public class HomePatientActivity extends AppCompatActivity {

    private LinearLayout doctorsLayout;
    private TextView emptyMessageTextView;
    private List<Map<String, Object>> doctorList;

    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private ImageView menuIcon;

    private String userId;
    private TextView averageRatingTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_patient);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                    displayPatientInfo();
                }
            }
        });

        // Initialisation de FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Référence aux vues
        doctorsLayout = findViewById(R.id.doctors_layout);
        emptyMessageTextView = findViewById(R.id.empty_message);

        // Initialisation de la liste de médecins
        doctorList = new ArrayList<>();

        // Chargement des médecins depuis Firebase
        loadDoctors();

        // Configurer le détecteur de changement Firestore
        setupFirestoreListener();
//search
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Appeler la méthode pour filtrer la liste des médecins avec le nouveau texte de recherche
                filterDoctors(newText);
                return true;
            }
        });

        // Récupérer l'ID de l'utilisateur actuel
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            // Gérer le cas où l'utilisateur n'est pas connecté
            // Par exemple, rediriger vers l'écran de connexion
            startActivity(new Intent(HomePatientActivity.this, LoginPatientActivity.class));
            finish(); // Fermer cette activité pour empêcher l'utilisateur de revenir ici en arrière
        }


    }



    private void filterDoctors(String query) {
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (Map<String, Object> doctor : doctorList) {
            String name = (String) doctor.get("name");
            if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(doctor);
            }
        }
        // Mettre à jour l'affichage avec la nouvelle liste filtrée
        displayDoctors(filteredList);
    }
    private void displayPatientInfo() {
        // Récupérer l'ID de l'utilisateur actuellement connecté
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Accéder au document du patient correspondant à l'ID de l'utilisateur
            db.collection("patients").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Le document existe, récupérer les données du patient
                                    Map<String, Object> patientData = document.getData();
                                    if (patientData != null) {
                                        String patientName = (String) patientData.get("name");
                                        String encodedImage = (String) patientData.get("photo");

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
                                    }
                                } else {
                                    // Le document n'existe pas
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                // Échec de la récupération du document
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }
    private void setupFirestoreListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("patients").document(userId);

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
                        String encodedImage = snapshot.getString("photo");

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
    private void setupFirestoreListenerForDoctor(String doctorUserId) {
        db.collection("doctors")
                .whereEqualTo("userId", doctorUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            Double averageRating = documentSnapshot.getDouble("averageRating");

                            // Trouvez le médecin correspondant dans doctorList
                            for (Map<String, Object> doctor : doctorList) {
                                if (doctorUserId.equals(doctor.get("userId"))) {
                                    // Mettre à jour l'interface utilisateur
                                    updateDoctorRatingInView(doctorUserId, averageRating);
                                    break;
                                }
                            }
                        } else {
                            Log.d(TAG, "No matching documents.");
                        }
                    }
                });
    }


    private void loadDoctors() {


        db.collection("doctors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            doctorList.clear();

                           for (DocumentSnapshot document : task.getResult()) {
                               Map<String, Object> doctorData = document.getData();

                               doctorList.add(doctorData);

                               // Configurer l'écouteur Firestore pour chaque médecin
                               setupFirestoreListenerForDoctor((String) doctorData.get("userId"));
                           }

                            displayDoctors(doctorList);
                        } else {

                            emptyMessageTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // Méthode pour afficher la moyenne des évaluations pour un médecin donné
    private void displayDoctorRating(LinearLayout doctorLayout, String userId) {
        TextView doctorRatingTextView = doctorLayout.findViewById(R.id.doctor_rating_text);

        // Récupérer la moyenne des évaluations pour le médecin correspondant à userId
        db.collection("doctors")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Double averageRating = documentSnapshot.getDouble("averageRating");
                        if (averageRating != null) {
                            doctorRatingTextView.setText(String.format("%.1f", averageRating));
                        } else {
                            doctorRatingTextView.setText("N/A");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePatientActivity.this, "Failed to load average rating", Toast.LENGTH_SHORT).show();
                });
    }
    private void updateDoctorRatingInView(String userId, Double averageRating) {
        for (int i = 0; i < doctorsLayout.getChildCount(); i++) {
            View doctorView = doctorsLayout.getChildAt(i);

            // Rechercher le TextView invisible contenant l'ID utilisateur
            TextView doctorUserIdTextView = (TextView) doctorView.findViewById(R.id.doctor_user_id);
            if (doctorUserIdTextView != null && userId.equals(doctorUserIdTextView.getText().toString())) {
                // Trouver et mettre à jour le TextView de l'évaluation du médecin
                TextView doctorRatingTextView = doctorView.findViewById(R.id.doctor_rating_text);
                if (averageRating != null) {
                    doctorRatingTextView.setText(String.format("%.1f", averageRating));
                } else {
                    doctorRatingTextView.setText("N/A");
                }
                break;
            }
        }
    }


    private void displayDoctors(List<Map<String, Object>> filteredDoctors) {
        // Effacer les vues précédemment ajoutées à doctorsLayout
        doctorsLayout.removeAllViews();

        if (filteredDoctors.isEmpty()) {
            emptyMessageTextView.setVisibility(View.VISIBLE);
        } else {
            emptyMessageTextView.setVisibility(View.GONE);

            LayoutInflater inflater = LayoutInflater.from(this);

            for (Map<String, Object> doctor : filteredDoctors) {
                // Afficher chaque médecin filtré
                LinearLayout doctorLayout = (LinearLayout) inflater.inflate(R.layout.doctor_item_layout, doctorsLayout, false);
                TextView nameTextView = doctorLayout.findViewById(R.id.doctor_name);
                nameTextView.setText((String) doctor.get("name"));

                TextView experienceTextView = doctorLayout.findViewById(R.id.doctor_experience);
                experienceTextView.setText("Expérience: " + (String) doctor.get("experiences")+ " years");

                TextView userIdTextView = doctorLayout.findViewById(R.id.doctor_user_id);
                String userId = (String) doctor.get("userId");
                userIdTextView.setText(userId);

                ImageView imageView = doctorLayout.findViewById(R.id.doctor_image);
                String encodedImage = (String) doctor.get("image");
                if (encodedImage != null && !encodedImage.isEmpty()) {
                    // Décoder la chaîne encodée en base64 en un tableau de bytes
                    byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
                    // Convertir les bytes en un objet Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    // Afficher le Bitmap dans l'ImageView
                    imageView.setImageBitmap(bitmap);
                } else {
                    // Afficher l'image par défaut si la chaîne encodée en base64 est vide ou null
                    imageView.setImageResource(R.drawable.profillogo); // Remplacez "default_doctor_image" par le nom de votre ressource d'image par défaut
                }
                // Ajouter un TextView invisible pour stocker l'ID utilisateur du médecin
                TextView doctorUserIdTextView = new TextView(this);
                doctorUserIdTextView.setText(userId);
                doctorUserIdTextView.setVisibility(View.GONE);
                doctorLayout.addView(doctorUserIdTextView);



                doctorsLayout.addView(doctorLayout);
                displayDoctorRating(doctorLayout, userId);




                Button button = doctorLayout.findViewById(R.id.book_appointment_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        // Récupérer les informations du médecin
                        String doctorName = (String) doctor.get("name");
                        String doctorExperience = (String) doctor.get("experiences");
                        String doctorImage = (String) doctor.get("image");
                        // Ajouter les informations supplémentaires

                        String priceMessaging = (String) doctor.get("priceMessaging");
                        String priceVideoCall = (String) doctor.get("priceVideoCall");
                        String priceVoiceCall = (String) doctor.get("priceVoiceCall");
                        String doctorEmail = (String) doctor.get("email");
                        String doctorTele = (String) doctor.get("phoneNumber");
                        String userId=(String) doctor.get("userId");

                        // Créer un intent pour démarrer Detail_Info_MedActivity
                        Intent intent = new Intent(HomePatientActivity.this, Detail_Info_MedActivity.class);

                        // Ajouter toutes les informations à l'intent
                        intent.putExtra("doctorName", doctorName);
                        intent.putExtra("doctorExperience", doctorExperience);
                        intent.putExtra("doctorImage", doctorImage);

                        intent.putExtra("doctorTele", doctorTele);
                        intent.putExtra("doctorEmail", doctorEmail);
                        intent.putExtra("priceMessaging", priceMessaging);
                        intent.putExtra("priceVideoCall", priceVideoCall);
                        intent.putExtra("priceVoiceCall", priceVoiceCall);
                        intent.putExtra("userId", userId);
                        // Démarrer l'activité Detail_Info_MedActivity
                        startActivity(intent);
                    }
                });


            }
        }
    }

    public void edit(View view) {
        // Créer un Intent pour démarrer votre nouvelle activité
        Intent intent = new Intent(this, EditProfilPatientActivity.class);
        // Démarrer l'activité
        startActivity(intent);
    }

    public void logout(View view) {
        logoutMenu(HomePatientActivity.this);
    }
private void logoutMenu(HomePatientActivity homePatientActivity){
    AlertDialog.Builder builder =new AlertDialog.Builder(homePatientActivity);
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
            FirebaseFirestore.getInstance().collection("patients").document(uid)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Supprimer le compte utilisateur dans Authentication
                            currentUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(HomePatientActivity.this, RegisterPatientActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Gérer les erreurs lors de la suppression du compte
                                                progressDialog.dismiss();
                                                Toast.makeText(HomePatientActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}