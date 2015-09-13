package com.igoryakovlev.findcommonfriends.app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Smile on 13.09.15.
 */
public class ContainerArrayList implements Serializable {
    private ArrayList<User> data;

    ContainerArrayList(ArrayList<User> data)
    {
        this.data = data;
    }

    public ArrayList<User> getData() {
        return data;
    }

    public void setData(ArrayList<User> data) {
        this.data = data;
    }
}
