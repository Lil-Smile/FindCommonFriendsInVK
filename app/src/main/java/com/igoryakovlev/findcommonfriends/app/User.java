package com.igoryakovlev.findcommonfriends.app;

import android.graphics.Bitmap;


import java.io.Serializable;

/**
 * Created by Smile on 10.09.15.
 */
public class User implements Serializable {
    private String name;
    private String surname;
    private Bitmap photo;
    private String id;

    public User(String name, String surname, Bitmap photo, String id) {
        this.photo = photo;
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public User(String name, String surname, String id)
    {
        this.name = name;
        this.id = id;
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

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString()
    {
        return this.id+", "+this.name+", "+this.surname;
    }

}
