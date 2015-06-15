package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Point;

import java.io.Serializable;

/**
 * Created by kiwitech on 15/6/15.
 */
public class SerializablePoint extends Point implements Serializable {

    public SerializablePoint(){
        super();
    }

    public SerializablePoint(Point point){
        super(point);
    }
    public SerializablePoint(SerializablePoint point){
        super(point);
    }
}