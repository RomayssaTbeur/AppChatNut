package com.example.appchatnutritien.activities;

public class Patient {
        private String name;
        private String email;
        private String phone;
        private String address;
        private int age;
        private String password;
        private String userId;

    // Constructeur par défaut requis pour Firestore
        public Patient() {
        }

        public Patient(String name, String email, String phone, String address, int age, String password) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.age = age;
            this.password = password;
        }

        // Getters et Setters

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
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

