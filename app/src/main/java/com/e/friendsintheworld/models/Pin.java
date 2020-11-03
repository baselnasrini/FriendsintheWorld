package com.e.friendsintheworld.models;

import com.google.android.gms.maps.model.LatLng;

public class Pin {
    private String username;
    private LatLng position;

    public Pin(String username, LatLng position) {
        this.username = username;
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
