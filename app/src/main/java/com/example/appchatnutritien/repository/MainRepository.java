package com.example.appchatnutritien.repository;

import android.content.Context;
import android.util.Log;

import com.example.appchatnutritien.WebRTC.MyPeerConnectionObserver;
import com.example.appchatnutritien.WebRTC.WebRTCClient;
import com.example.appchatnutritien.remote.FireBaseClient;
import com.example.appchatnutritien.utlities.DataModel;
import com.example.appchatnutritien.utlities.DataModelType;
import com.example.appchatnutritien.utlities.ErrorCallBack;
import com.example.appchatnutritien.utlities.NewEventCallBack;
import com.example.appchatnutritien.utlities.SuccessCallBack;
import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class MainRepository implements WebRTCClient.Listener {
    public Listener listener;
    private final Gson gson = new Gson();
    private final FireBaseClient fireBaseClient;
    private WebRTCClient webRTCClient;
    private String currentUserName;
    private SurfaceViewRenderer remoteView;//utilisée pour afficher le flux vidéo distant
    private String target;//destination de la connexion WebRTC
    private static MainRepository instance;

    public MainRepository(){
        this.fireBaseClient = new FireBaseClient();

    }
    private void UpdateCurrentUserName(String username){
        this.currentUserName=username;
    }

    public static MainRepository getInstance(){
        if(instance == null){
            instance = new MainRepository();
        }
        return instance;
    }



    public void login(String username, Context context, SuccessCallBack callBack){
        fireBaseClient.login(username ,()->{
            UpdateCurrentUserName(username);
            this.webRTCClient = new WebRTCClient(context,new MyPeerConnectionObserver(){
                @Override
                public void onAddStream(MediaStream mediaStream) {
                    super.onAddStream(mediaStream);
                    try {
                        mediaStream.videoTracks.get(0).addSink(remoteView);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    super.onConnectionChange(newState);
                    Log.d("TAG", "onConnectionChange: "+newState);
                    super.onConnectionChange(newState);
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                        listener.webrtcConnected();
                    }

                    if (newState == PeerConnection.PeerConnectionState.CLOSED ||
                            newState == PeerConnection.PeerConnectionState.DISCONNECTED ){
                        if (listener!=null){
                            listener.webrtcClosed();
                        }
                    }
                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    super.onIceCandidate(iceCandidate);
                    webRTCClient.sendIceCandidate(iceCandidate, target);
                }
            },username);
            webRTCClient.listener = this;
            callBack.onSuccess();
        });
    }

    public void initLocalViews(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    public void startCall(String target){
        webRTCClient.call(target);
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }
    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }

    public void endCall(){
        webRTCClient.closeConnection();
    }

    public void sendCallRequest(String target, ErrorCallBack errorCallBack){
        fireBaseClient.sendMessageToOtherUser(
                new DataModel(target,currentUserName,null, DataModelType.StartCall),errorCallBack
        );
    }
    public void subscribeForLatestEvent(NewEventCallBack callBack){
        fireBaseClient.observeIncomingLatestEvent(model-> {
            switch(model.getType()){
                case Offer:
                    this.target = model.getSender();
                    webRTCClient.OnRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER, model.getData()
                    ));
                    webRTCClient.answer(model.getSender());
                    break;

                case Answer:
                    webRTCClient.OnRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER, model.getData()
                    ));
                    break;

                case IceCandidate:
                    try{
                        IceCandidate candidate = gson.fromJson(model.getData(),IceCandidate.class);
                        webRTCClient.addIceCandidate(candidate);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;

                case StartCall:
                    this.target = model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
            }
        });
    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        fireBaseClient.sendMessageToOtherUser(model,()->{});
    }

    public interface Listener{
        void webrtcConnected();
        void webrtcClosed();
    }
}
