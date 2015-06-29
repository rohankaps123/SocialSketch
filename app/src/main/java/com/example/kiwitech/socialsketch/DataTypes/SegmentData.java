package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Point;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Contains data for each segment. Will be sent over the internet as Json object.It is serializable.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class SegmentData implements Serializable {
    /**
     * Arraylist of all Points
     */
    ArrayList<Pair<Float,Float>> points;
    /**
     * Stores the color of the Paint
     */
    int color;
    /**
     * Stores the size of the Brush
     */
    int brush_size;

    /**
     * Set the brush size used to draw the segment
     * @param brush_size brush size
     */
    public void setBrush_size(int brush_size) {
        this.brush_size = brush_size;
    }

    /**
     * Set the color used to draw the segment
     * @param color brush color
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * get the color used to draw the segment
     * @return color
     */
    public int getColor(){
        return color;
    }
    public int getBrush_size(){
        return brush_size;
    }

    public SegmentData(){
        points = new ArrayList<Pair<Float, Float>>();
    }

    /**
     * Add a point to the list of points
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(float x, float y){
        points.add(new Pair(x,y));
    }

    /**
     * Add a single point if the segment is just a point
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addSinglePoint(float x, float y){
        points.clear();
        points.add(new Pair(x,y));
    }

    /**
     * get back the Point
     * @return
     */
    public ArrayList<Pair<Float,Float>> getData(){
        return points;
    }

    /**
     * Reset the list for a new segment
     */
    public void reset(){
        points.clear();
    }

    public ArrayList getArrayListAsString(){
       return points;
    }
}
