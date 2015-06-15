package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Paint;

import java.io.Serializable;

/**
 * Created by kiwitech on 15/6/15.
 */
public class SerializablePaint extends Paint implements Serializable {

    public SerializablePaint(){
        super();
    }

    public SerializablePaint(Paint paint){
        super(paint);
    }
    public SerializablePaint(SerializablePaint paint){
        super(paint);
    }
}
