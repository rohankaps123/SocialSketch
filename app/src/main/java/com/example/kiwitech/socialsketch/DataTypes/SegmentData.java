package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Point;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Contains data for each segment. Will be sent over the internet as Json object.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class SegmentData implements Serializable {

    ArrayList<Pair<Float,Float>> points;
    int color;
    int brush_size;

    public void setBrush_size(int brush_size) {
        this.brush_size = brush_size;
    }

    public void setColor(int color) {
        this.color = color;
    }
    public int getColor(){
        return color;
    }
    public int getBrush_size(){
        return brush_size;
    }

    public SegmentData(){
        points = new ArrayList<Pair<Float, Float>>();
    }

    public void addPoint(float x, float y){
        points.add(new Pair(x,y));
    }

    public void addSinglePoint(float x, float y){
        points.clear();
        points.add(new Pair(x,y));
    }

    public ArrayList<Pair<Float,Float>> getData(){
        return points;
    }

    public void reset(){
        points.clear();
    }
}
