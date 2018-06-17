package com.example.mahe.sos;

import android.location.Location;

public class FirebaseLocationData {
    String email;
    double latitude;
    double longitude;
    String time;
    public FirebaseLocationData() {
    }

    public FirebaseLocationData(String email, double latitude, double longitude, String time) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}

