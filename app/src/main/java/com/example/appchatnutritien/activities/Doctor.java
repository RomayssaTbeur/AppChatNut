package com.example.appchatnutritien.activities;

public class Doctor {
    private String name;
    private String imageUrl;
    private String experience;

    public Doctor(String name, String imageUrl, String experience) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.experience = experience;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getExperience() {
        return experience;
    }
}
