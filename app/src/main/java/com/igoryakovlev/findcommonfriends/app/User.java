package com.igoryakovlev.findcommonfriends.app;

import android.media.Image;

/**
 * Created by Smile on 10.09.15.
 */
public class User {
    private String name;
    private String surname;
    private Image photo;
    private String id;

    public User(String name, String surname, Image photo, String id) {
        this.photo = photo;
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Image getPhoto() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
