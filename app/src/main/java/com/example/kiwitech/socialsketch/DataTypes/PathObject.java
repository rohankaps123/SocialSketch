package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.io.Serializable;

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

    /**
     * Constructs a new path object with a given paint object
     * @param npaint paint object
     */
    public PathObject(Paint npaint){
        path = new Path();
        point = new Point();
        paint = npaint;
        isPoint = false;
    }

    /**
     * Creates a new pathobject object using a paint object and a path object
     * @param npath path object
     * @param npaint paint object
     */
    public PathObject(Path npath, Paint npaint){
        path = npath;
        paint = npaint;
    }

    /**
     * Creates a new pathobject object using a paint object and a point object
     * @param npoint point object
     * @param npaint paint object
     */
    public PathObject(Point npoint, Paint npaint){
        point = npoint;
        paint = npaint;
        isPoint = true;
    }

    /**
     * Check if the Object is a path or a Point
     * @return true if its a point
     */
    public boolean CheckifPoint(){
        return isPoint;
    }

    /**
     * Set that the object is a point
     * @param ispoint true if the path is a point
     */
    public void setIsPoint(boolean ispoint){
        isPoint = ispoint;
    }

    /**
     * Get the  stored Point object
     * @return Point object
     */
    public Point getPoint(){
        return point;
    }

    /**\
     * Get the stored Path object
     * @return path object
     */

    public Path getPath(){
        return path;
    }

    /**
     * Get the stored paint object
     * @return paint object
     */
    public Paint getPaint(){
        return paint;
    }

}