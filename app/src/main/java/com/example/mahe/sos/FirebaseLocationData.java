package com.example.mahe.sos;

import android.location.Location;

public class FirebaseLocationData {
    String email;
    Location location;

    String time;

    public FirebaseLocationData() {
    }

    public FirebaseLocationData(Location location, String email, String time) {
        this.location = location;
        this.email = email;
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

