package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by kiwitech on 15/6/15.
 */
public class SegmentData implements Serializable {

    String data;

    public SegmentData(){
    data = "";
    }
    public void addSinglePoint(int x, int y){
    data = "" + String.valueOf(x) +"," + String.valueOf(y);
    }

    public void addMoveTo(int x, int y){
        data = data + "||" + String.valueOf(x)+"," + String.valueOf(y);
    }
    public void addLineTo(int x, int y){
        data = data +"|" + String.valueOf(x)+"," + String.valueOf(y);
    }
    public void addQuadTo(int x, int y){
        data = data + "|||" + String.valueOf(x)+"," + String.valueOf(y);
    }
    public String getData(){
        return data;
    }

    public void reset(){
        data = "";
    }
}
