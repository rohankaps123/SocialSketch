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
    private Path path;
    /**
     * Stores the coordinates of the point if the interaction is a point
     */
    private Point point;
    /**
     * Stores the paint property of the interaction.
     */
    private Paint paint;
    /**
     * Tells whether the interaction is a point or not
     */
    private boolean isPoint;


    public PathObject(Paint npaint){
        path = new Path();
        point = new Point();
        paint = npaint;
        isPoint = false;
    }
    public PathObject(Path npath, Paint npaint){
        path = npath;
        paint = npaint;
    }

    public PathObject(Point npoint, Paint npaint){
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

    public Point getPoint(){
        return point;
    }

    public Path getPath(){
        return path;
    }

    public Paint getPaint(){
        return paint;
    }

}