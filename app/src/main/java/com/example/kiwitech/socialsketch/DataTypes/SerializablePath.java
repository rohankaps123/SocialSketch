package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Path;

import java.io.Serializable;

/**
 * Created by kiwitech on 15/6/15.
 */
public class SerializablePath extends Path implements Serializable {

    public SerializablePath(){
        super();
    }

    public SerializablePath(Path path){
        super(path);
    }
    public SerializablePath(SerializablePath path){
        super(path);
    }
}