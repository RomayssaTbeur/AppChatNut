package com.example.appchatnutritien.remote;

import androidx.annotation.NonNull;

import com.example.appchatnutritien.utlities.DataModel;
import com.example.appchatnutritien.utlities.ErrorCallBack;
import com.example.appchatnutritien.utlities.NewEventCallBack;
import com.example.appchatnutritien.utlities.SuccessCallBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;

public class FireBaseClient {

    private final Gson gson = new Gson();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private String currentUserName;
    private static final String LATEST_EVENT_FIELD_NAME="latest_event";

    public void login(String username, SuccessCallBack callBack){
       dbRef.child(username).setValue("").addOnCompleteListener(task->{
           currentUserName = username;
           callBack.onSuccess();
       });
    }

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){
       dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.child(dataModel.getTarget()).exists()){
                   //send the signal to other user
                   dbRef.child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                           .setValue(gson.toJson(dataModel));
               }else{
                   errorCallBack.onError();
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {
               errorCallBack.onError();
           }
       });
    }

    public void observeIncomingLatestEvent(NewEventCallBack callBack){
      dbRef.child(currentUserName).child(LATEST_EVENT_FIELD_NAME)
              .addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                      try {
                          String data = Objects.requireNonNull(snapshot.getValue()).toString();
                          DataModel dataModel =gson.fromJson(data, DataModel.class);
                          callBack.onNewEventReceived(dataModel);
                      }catch (Exception e){
                         e.printStackTrace();
                      }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
              });
    }
}
