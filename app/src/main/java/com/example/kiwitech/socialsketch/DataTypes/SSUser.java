package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Canvas;

/**
 * A SocialSketch user class
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class SSUser {
    private String name;
    private String email;
    private String password;
    private String dob;
    private Boolean online;

    public SSUser(String name, String email, String password, String dob){
        this.name = name;
        this.email = email;
        this.password = password;
        this.dob = dob;
        online = false;
    }

    public String getEmail() {
        return email;
    }

    public String getDob(){
        return dob;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
