package com.example.appchatnutritien.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.appchatnutritien.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatHomeActivity extends AppCompatActivity {
    private CustomWaveformView customWaveformView;
    private Long temps;
    private String audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audios/recording.3gp";

    private FirebaseFirestore db;
    private FirebaseFirestore db2;
    private EditText messageEditText;
    private ImageView sendButton;
    private ImageView chooseImageButton;
    private LinearLayout layout;
    private LinearLayout layout2;
    private TextView textView;
    private ImageView imageView;
    private LinearLayout layout1;
    private List<Message> messages;
    private String userId;
    private String reciveID;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private String previousDate;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int REQUEST_RECORD_AUDIO = 1002;

    private ImageView audioImageView;
    private  ImageView deleteMessage;
    private MediaRecorder mediaRecorder;

    private String audioFileName ;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {





        db2 = FirebaseFirestore.getInstance();
        db2.collection("messages")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        layout.removeAllViews();
                        messages.clear();
                        loadMessages();
                        try {
                            displayMessages();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                });









        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);
        deleteMessage=findViewById(R.id.deleteMessage);
        deleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("messages")
                        .whereEqualTo("timestamp", temps)
                        .whereEqualTo("senderId",  userId ).whereEqualTo("receiverId",reciveID)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {


                                            Toast.makeText(ChatHomeActivity.this, "Message supprimé", Toast.LENGTH_SHORT).show();
                                            layout1.setVisibility(View.INVISIBLE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DeleteMessageFailed", "Failed to delete message", e);
                                            Toast.makeText(ChatHomeActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DeleteMessageFailed", "Failed to find message to delete", e);
                            Toast.makeText(ChatHomeActivity.this, "Failed to find message to delete", Toast.LENGTH_SHORT).show();
                        });

            }
        });
        Intent intent = getIntent();
        layout2 = findViewById(R.id.c);
        layout1=findViewById(R.id.layout1);
        if (intent != null && intent.hasExtra("reciveId")) {
//            Bitmap imageBitmap = (Bitmap) intent.getParcelableExtra("image");
            String name = intent.getStringExtra("name");
            textView = findViewById(R.id.name);
            imageView = findViewById(R.id.ImageProfil);
//            Bitmap circularBitmap = getCircularBitmap(imageBitmap);
//            imageView.setImageBitmap(circularBitmap);
            textView.setText(name);
            reciveID = intent.getStringExtra("reciveId");
        }
        audioImageView = findViewById(R.id.audio);
        audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/recording.3gp";

        storageReference = FirebaseStorage.getInstance().getReference();

        audioImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    checkPermissionsAndRecord();
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        layout = findViewById(R.id.contentLayout);

        sendButton = findViewById(R.id.send);
        chooseImageButton = findViewById(R.id.choseImage);
        messageEditText = findViewById(R.id.messageText);
        messages = new ArrayList<>();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                userId = user.getUid();
                messages.clear();
                loadMessages();
            } else {
                userId = null;
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

        sendButton.setOnClickListener(v -> sendMessage());
        chooseImageButton.setOnClickListener(v -> chooseImage());
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);

        Bitmap circularBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float radius = diameter / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return circularBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        releaseMediaRecorder();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void loadMessages() {
        if (userId == null) return;

        db.collection("messages").whereEqualTo("senderId", userId).whereEqualTo("receiverId", reciveID)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String text = documentSnapshot.getString("text");
                        long timestamp = documentSnapshot.getLong("timestamp");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String audioUrl = documentSnapshot.getString("audioUrl");
                        messages.add(new Message(text, timestamp, imageUrl, audioUrl, true));
                    }

                }).addOnFailureListener(e -> {
                    Log.e("LoadMessagesFailed", "Failed to load messages", e);
                    Toast.makeText(ChatHomeActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                });

        db.collection("messages").whereEqualTo("receiverId", userId).whereEqualTo("senderId", reciveID)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String text = documentSnapshot.getString("text");
                        long timestamp = documentSnapshot.getLong("timestamp");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String audioUrl = documentSnapshot.getString("audioUrl");
                        messages.add(new Message(text, timestamp, imageUrl, audioUrl, false));
                    }

                    try {
                        displayMessages();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("LoadMessagesFailed", "Failed to load messages", e);
                    Toast.makeText(ChatHomeActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString();

        if (message.isEmpty() && selectedImageUri == null) {
            Toast.makeText(ChatHomeActivity.this, "Le message est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("receiverId", reciveID);
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("senderId", currentUser.getUid());

            if (!message.isEmpty()) {
                messageData.put("text", message);
            }

            addMessageToFirestore(messageData);
//            Message messageMessage=new Message(message,System.currentTimeMillis(),null,null,true);
//            long notificationTime = System.currentTimeMillis() + 10000;
//            sendNotification(messageMessage,notificationTime);

        } else {
            Intent intent = new Intent(ChatHomeActivity.this, LoginPatientActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, Map<String, Object> messageData) {
        if (imageUri != null) {
            StorageReference storageRef = storageReference.child("images/" + UUID.randomUUID().toString());

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            messageData.put("imageUrl", imageUrl);
                            addMessageToFirestore(messageData);
                            Log.d("UploadImageSuccess", "Image téléchargée avec succès, URL: " + imageUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("GetImageUrlFailed", "Échec de récupération de l'URL de l'image", e);
                            Toast.makeText(ChatHomeActivity.this, "Échec de récupération de l'URL de l'image", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UploadImageFailed", "Échec du téléchargement de l'image", e);
                        Toast.makeText(ChatHomeActivity.this, "Échec du téléchargement de l'image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addMessageToFirestore(Map<String, Object> messageData) {
        db.collection("messages").add(messageData)
                .addOnSuccessListener(documentReference -> {

                    messages.add(new Message(
                            (String) messageData.get("text"),
                            (Long) messageData.get("timestamp"),
                            (String) messageData.get("imageUrl"),
                            (String) messageData.get("audioUrl"),
                            true));



                    messageEditText.setText("");

                    selectedImageUri = null;
                })
                .addOnFailureListener(e -> {

                    Log.e("AddMessageFailed", "Failed to send message", e);
                    Toast.makeText(ChatHomeActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            Map<String, Object> messageData = new HashMap<>();
            messageData.put("text", null);
            messageData.put("imageUrl", selectedImageUri.toString());
            messageData.put("audioUrl", null);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            messageData.put("receiverId", reciveID);
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("senderId", currentUser.getUid());
            uploadImageToFirebaseStorage(selectedImageUri, messageData);
        } else {
            Toast.makeText(ChatHomeActivity.this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ResourceType")
    private void displayMessages() throws IOException {
        Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

        layout.removeAllViews();

        if (!messages.isEmpty()) {
            previousDate = messages.get(0).getFormattedTimesdate();
        }

        TextView dateTextView = new TextView(ChatHomeActivity.this);
        dateTextView.setText(previousDate);
        dateTextView.setTextSize(13);
        dateTextView.setTextColor(Color.BLACK);
        dateTextView.setGravity(Gravity.CENTER);
        layout.addView(dateTextView);

        for (Message message : messages) {
            if (!message.getFormattedTimesdate().equals(previousDate)) {
                TextView newDateTextView = new TextView(ChatHomeActivity.this);
                newDateTextView.setText(message.getFormattedTimesdate());
                newDateTextView.setTextSize(13);
                newDateTextView.setTextColor(Color.BLACK);
                newDateTextView.setGravity(Gravity.CENTER);
                layout.addView(newDateTextView);
                previousDate = message.getFormattedTimesdate();
            }

            if (message.getImageUrl() != null) {
                ImageView imageView = new ImageView(ChatHomeActivity.this);
                Glide.with(ChatHomeActivity.this)
                        .load(message.getImageUrl())
                        .into(imageView);



                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200,200);


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(  300,300);
                if (message.isSent()) {



                    params.setMargins(1, 10, 400, 10);
                    ;
                } else {



                    params.setMargins(740, 10, 1, 10);

                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout1.setVisibility(View.VISIBLE);
                        temps=message.timestamp;
                    }
                });


                CardView card=new CardView(ChatHomeActivity.this);
                card.setRadius(50);
                card.setLayoutParams(params);


                card.addView(imageView);
                TextView timeTextView = new TextView(ChatHomeActivity.this);
                timeTextView.setText("\n"+message.getFormattedTimestamp());
                card.addView(timeTextView);
                layout.addView(card);





            } else if (message.getAudioUrl() != null) {

                ImageView audioImageView = new ImageView(ChatHomeActivity.this);
                audioImageView.setImageResource(R.drawable.micro2);


                int iconSize = 90;
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
                audioImageView.setLayoutParams(iconParams);


                int startMargin, endMargin;
                int backgroundResource;
                if (message.isSent()) {
                    startMargin = 30;
                    endMargin = 480;
                    backgroundResource = R.drawable.audioback;
                } else {
                    startMargin = 480;
                    endMargin = 30;
                    backgroundResource = R.drawable.rectangle;
                }





                CustomWaveformView customWaveformView = new CustomWaveformView(ChatHomeActivity.this);
                LinearLayout.LayoutParams waveformParams = new LinearLayout.LayoutParams(
                        300,
                        50);

                waveformParams.setMargins(10, 20, 10, 40);

                customWaveformView.setLayoutParams(waveformParams);
                customWaveformView.setWaveformLineColor(Color.BLACK);

                customWaveformView.setAudioFile(message.getAudioUrl().toString());

                LinearLayout l=new LinearLayout(ChatHomeActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        530,
                        120
                );
                l.setLayoutParams(layoutParams);
                layoutParams.setMargins(startMargin, 40, endMargin, 40);
                audioImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (customWaveformView.isPlaying) {
                            customWaveformView.stopPlayback();
                            audioImageView.setImageResource(R.drawable.micro2);

                        } else {
                            customWaveformView.startPlayback(message.getAudioUrl());
                            audioImageView.setImageResource(R.drawable.baseline_contactless_24);
                        }
                        customWaveformView.isPlaying = !customWaveformView.isPlaying; // Toggle playback state
                    }
                });

                l.setBackgroundResource(backgroundResource);

                l.addView(audioImageView);
                l.addView(customWaveformView);
                TextView timeTextView = new TextView(ChatHomeActivity.this);
                timeTextView.setText("\n"+message.getFormattedTimestamp());
                l.addView(timeTextView);
                layout.addView(l);
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout1.setVisibility(View.VISIBLE);
                        temps=message.timestamp;
                    }
                });





            } else {
                TextView textView = new TextView(ChatHomeActivity.this);
                String messageText = message.getText() + "\n" + message.getFormattedTimestamp();
                textView.setText(messageText);
                int paddingStart, paddingEnd;
                if (message.isSent()) {
                    textView.setBackgroundResource(R.drawable.audioback);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(510, 150);
                    params.setMargins(1, 10, 500, 10);

                    textView.setLayoutParams(params);
                } else {
                    textView.setBackgroundResource(R.drawable.rectangle);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(510, 150);
                    params.setMargins(500, 10, 1, 10);
                    textView.setLayoutParams(params);
                }
                textView.setPadding(50, 20, 10, 20);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout1.setVisibility(View.VISIBLE);
                        temps=message.timestamp;
                    }
                });
                layout.addView(textView);
            }
        }
    }

    private static class Message {
        private String text;
        private long timestamp;
        private boolean sent;
        private String imageUrl;
        private String audioUrl;

        Message(String text, long timestamp, String imageUrl, String audioUrl, boolean sent) {
            this.text = text;
            this.timestamp = timestamp;
            this.imageUrl = imageUrl;
            this.audioUrl = audioUrl;
            this.sent = sent;
        }

        String getText() {
            return text;
        }

        long getTimestamp() {
            return timestamp;
        }

        boolean isSent() {
            return sent;
        }

        String getImageUrl() {
            return imageUrl;
        }

        String getAudioUrl() {
            return audioUrl;
        }

        String getFormattedTimesdate() {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        }

        String getFormattedTimestamp() {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(date);
        }
    }

    private boolean isRecording = false;

    private void checkPermissionsAndRecord() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("MediaRecorderError", "prepare() failed: " + e.getMessage());
        }

        mediaRecorder.start();
        isRecording = true;
        audioImageView.setImageResource(R.drawable.micro2);
        Toast.makeText(ChatHomeActivity.this, "Enregistrement audio en cours...", Toast.LENGTH_SHORT).show();
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            audioImageView.setImageResource(R.drawable.micro2);
            Toast.makeText(ChatHomeActivity.this, "Enregistrement audio terminé", Toast.LENGTH_SHORT).show();

            uploadAudioToFirebaseStorage();
        }
    }

    private void uploadAudioToFirebaseStorage() {
        if (audioFilePath != null) {
            audioFileName= UUID.randomUUID().toString();
            StorageReference audioRef = storageReference.child("audios").child(audioFileName);
            Uri audioUri = Uri.fromFile(new File(audioFilePath));

            audioRef.putFile(audioUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String audioUrl = uri.toString();
                            Map<String, Object> messageData = new HashMap<>();
                            messageData.put("text", null);
                            messageData.put("imageUrl", null);
                            messageData.put("audioUrl", audioUrl);
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            messageData.put("receiverId", reciveID);
                            messageData.put("timestamp", System.currentTimeMillis());
                            messageData.put("senderId", currentUser.getUid());
                            addMessageToFirestore(messageData);

                            Log.d("UploadAudioSuccess", "Audio téléchargé avec succès, URL: " + audioUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("GetAudioUrlFailed", "Échec de récupération de l'URL de l'audio", e);
                            Toast.makeText(ChatHomeActivity.this, "Échec de récupération de l'URL de l'audio", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UploadAudioFailed", "Échec du téléchargement de l'audio", e);
                        Toast.makeText(ChatHomeActivity.this, "Échec du téléchargement de l'audio", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(ChatHomeActivity.this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private static class CustomWaveformView extends View {
        private Paint waveformPaint;
        String  amplitudes;
        private boolean isPlaying = false;

        public CustomWaveformView(Context context) {
            super(context);
            waveformPaint = new Paint();
        }

        public void setWaveformLineColor(int color) {
            waveformPaint.setColor(color);
        }

        public void setAudioFile(String audioAmplitudes) {
            amplitudes = audioAmplitudes;
            invalidate();
        }
        public void startPlayback(String audioUrl) {
            isPlaying = true;
            invalidate();
            if (audioUrl != null) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(mp -> {

                        mediaPlayer.release();
                        isPlaying = false;
                        invalidate();
                    });
                } catch (IOException e) {
                    Log.e("MediaPlayerError", "Erreur lors de la préparation du lecteur audio", e);
                    isPlaying = false;
                    invalidate();
                }
            }
        }

        public void stopPlayback() {

        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (amplitudes != null) {
                float width = getWidth();
                float height = getHeight();
                float centerY = height / 2;

                int numAmplitudes = amplitudes.length();
                float deltaX = width / numAmplitudes;
                float curX = 0;
                float halfHeight = height / 2;

                for (int i = 0; i < numAmplitudes; i++) {
                    char amplitudeChar = amplitudes.charAt(i);
                    float amplitude = Character.getNumericValue(amplitudeChar);
                    float scaledAmplitude = (amplitude / 9) * halfHeight;
                    float startY = centerY - scaledAmplitude;
                    float endY = centerY + scaledAmplitude;

                    // Draw the upper part of the waveform
                    waveformPaint.setColor(ContextCompat.getColor(getContext(), R.color.blancCasse));
                    canvas.drawLine(curX, startY, curX, centerY, waveformPaint);

                    // Draw the lower part of the waveform
                    waveformPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
                    canvas.drawLine(curX, centerY, curX, endY, waveformPaint);

                    curX += deltaX;
                }
            }
        }

    }
//    public void getFCMTokenFromReceiverId(String receiverId, FCMTokenCallback callback) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference patientsCollection = db.collection("patients");
//        Query query = patientsCollection.whereEqualTo("userId", receiverId);
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (!queryDocumentSnapshots.isEmpty()) {
//                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
//                String fcmToken = documentSnapshot.getString("fcmToken");
//
//                callback.onTokenReceived(fcmToken);
//            } else {
//                Log.e(TAG, "No patient document found with userid: " + receiverId);
//                callback.onTokenReceived(null);
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "Error getting patient document", e);
//            callback.onTokenReceived(null);
//        });
//    }
//    public void sendNotification(Message message, long notificationTime) {
//        String receiverId = reciveID;
//        getFCMTokenFromReceiverId(receiverId, new FCMTokenCallback() {
//            @Override
//            public void onTokenReceived(String fcmToken) {
//                if (fcmToken != null) {
//                    JSONObject json = new JSONObject();
//                    try {
//                        json.put("to", fcmToken);
//                        JSONObject notificationObj = new JSONObject();
//                        notificationObj.put("title", "New Message");
//                        notificationObj.put("body", "You have a new message from " + userId);
//                        json.put("notification", notificationObj);
//                        notificationObj.put("icon", "notif");
//                        OkHttpClient client = new OkHttpClient();
//                        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                        RequestBody requestBody = RequestBody.create(mediaType, json.toString());
//                        Request request = new Request.Builder()
//                                .header("Authorization", "Bearer AAAAGrLOvOA:APA91bFWSG5jUud1hYLXQSGAuUbAtwDfGAlCO0guY_od9XmHc9NqbogJE1hQEtVQln0_lgV478swgJ4qUP1cnMnS-BvQ-p4Qk-B3taIuVQREFom_8S1bUWYgmAOV8du2iLqmTxeKvVMg ")
//                                .url("https://fcm.googleapis.com/fcm/send")
//                                .post(requestBody)
//                                .build();
//
//                        client.newCall(request).enqueue(new Callback() {
//                            @Override
//                            public void onFailure(Call call, IOException e) {
//                                Log.e(TAG, "Error sending FCM message", e);
//                            }
//
//                            @Override
//                            public void onResponse(Call call, Response response) throws IOException {
//                                try {
//                                    Log.d(TAG, "FCM message sent successfully");
//                                    // Utilisez la réponse ici
//                                } finally {
//                                    if (response != null && response.body() != null) {
//                                        response.body().close(); // Fermez le corps de la réponse
//                                    }
//                                }
//                            }
//                        });
//                    } catch (JSONException e) {
//                        Log.e(TAG, "Error creating JSON object for FCM message", e);
//                    }
//                }
//            }
//        });
//    }

}