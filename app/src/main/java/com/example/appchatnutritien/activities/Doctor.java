package com.example.appchatnutritien.activities;

public class Doctor {
    private String name;
    private String image;
    private String experiences;
    private String email;
    private String phoneNumber;
    private String priceMessaging;
    private String priceVoiceCall;
    private String priceVideoCall;
    private String password;
    private String userId;

    public Doctor(String name, String email, String phoneNumber, String experiences, String priceMessaging, String priceVideoCall, String priceVoiceCall, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.experiences = experiences;
        this.priceMessaging = priceMessaging;
        this.priceVideoCall = priceVideoCall;
        this.priceVoiceCall = priceVoiceCall;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getExperiences() {
        return experiences;
    }

    public void setExperiences(String experiences) {
        this.experiences = experiences;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPriceMessaging() {
        return priceMessaging;
    }

    public void setPriceMessaging(String priceMessaging) {
        this.priceMessaging = priceMessaging;
    }

    public String getPriceVoiceCall() {
        return priceVoiceCall;
    }

    public void setPriceVoiceCall(String priceVoiceCall) {
        this.priceVoiceCall = priceVoiceCall;
    }

    public String getPriceVideoCall() {
        return priceVideoCall;
    }

    public void setPriceVideoCall(String priceVideoCall) {
        this.priceVideoCall = priceVideoCall;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
