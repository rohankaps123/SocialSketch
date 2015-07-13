package com.example.kiwitech.socialsketch.DataTypes;

import android.graphics.Bitmap;
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
     * Size of the display from where the segment origins
     */
    private Pair<Float,Float> SizeOrigin;

    /**
     * Arraylist of all Points
     */
    ArrayList<Pair<Float,Float>> points;
    /**
     * Stores the color of the Paint
     */

    private String background;

    private Boolean isBitmap = false;

    private String mode;
    int color;
    /**
     * Stores the size of the Brush
     */
    int brush_size;
    /**
     * Whether the segment is an eraser segment
     */
    private boolean isErase;

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

    /**
     * get the size of the brush
     * @return brush size
     */
    public int getBrush_size(){
        return brush_size;
    }

    public SegmentData(){
        points = new ArrayList<Pair<Float, Float>>();
        isErase = false;
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
        setIsErase(false);
        setIsBitmap(false);
        setBackground(null);
    }

    /**
     * Gets the ArrayList of Points
     * @return
     */
    public ArrayList getArrayList(){
       return points;
    }

    /**
     * Gets whether the segment is an erase segment
     * @return
     */
    public boolean isErase() {
        return isErase;
    }

    /**
     * Sets whether the segment is an erase segment
     * @param isErase
     */
    public void setIsErase(boolean isErase) {
        this.isErase = isErase;
    }

    /**
     * Get the size of the Originating screen
     * @return
     */
    public Pair<Float, Float> getSizeOrigin() {
        return SizeOrigin;
    }

    /**
     * Set the size of the originating screen
     * @param sizeOrigin
     */
    public void setSizeOrigin(Pair<Float, Float> sizeOrigin) {
        SizeOrigin = sizeOrigin;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Boolean getIsBitmap() {
        return isBitmap;
    }

    public void setIsBitmap(Boolean isBitmap) {
        this.isBitmap = isBitmap;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
