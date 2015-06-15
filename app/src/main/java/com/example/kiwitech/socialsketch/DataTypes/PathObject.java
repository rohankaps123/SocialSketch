package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * PathObject is a class that stores the data for each interaction between the finger
 * and the canvas
 * @author Rohan Kapoor
 * @since 1.0
 */
public class PathObject implements Serializable{
    /**
     * Stores Path
     */
    private SerializablePath path;
    /**
     * Stores the coordinates of the point if the interaction is a point
     */
    private SerializablePoint point;
    /**
     * Stores the paint property of the interaction.
     */
    private SerializablePaint paint;
    /**
     * Tells whether the interaction is a point or not
     */
    private boolean isPoint;


    public PathObject(SerializablePaint npaint){
        path = new SerializablePath();
        point = new SerializablePoint();
        paint = npaint;
        isPoint = false;
    }
    public PathObject(SerializablePath npath, SerializablePaint npaint){
        path = npath;
        paint = npaint;
    }

    public PathObject(SerializablePoint npoint, SerializablePaint npaint){
        point = npoint;
        paint = npaint;
        isPoint = true;
    }

    public boolean CheckifPoint(){
        return isPoint;
    }

    public void setIsPoint(boolean ispoint){
        isPoint = ispoint;
    }

    public SerializablePoint getPoint(){
        return point;
    }

    public SerializablePath getPath(){
        return path;
    }

    public SerializablePaint getPaint(){
        return paint;
    }

}