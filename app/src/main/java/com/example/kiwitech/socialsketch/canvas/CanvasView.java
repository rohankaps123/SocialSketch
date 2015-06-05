package com.example.kiwitech.socialsketch.canvas;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * View for Canvas
 *
 * This view is designed to display the canvas. It uses a canvas initialized on a Bitmap to draw.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class CanvasView extends View {

    //Initializing different objects to for the view

    private Bitmap canvas_bitmap;
    private Canvas canvas;
    /**
     * Represents the Brush Size;
     */
    private int brush_size;
    /**
     * Path Canvas is a temporary path and stores the data between finger down and finger up.
     * Can be later used to send data to different users so that they can replicate the canvas.
     */
    private Path path_canvas;
    /**
     * Stores the total Path so that it can be redrawn later or on a canvas of different size.
     */
    private Path path_total;
    /**
     * Stores the points made by the user so that it can be redrawn later or on a canvas.
     */
    private ArrayList<Point> points = new ArrayList<Point>();

    /**
     * Stores the Paint attributes for the drawing
     */
    private Paint paint_canvas;
    /**
     * Stores the color of the drawings
     */
    private int path_color;
    /**
     * Stores the y coordinate of the starting point on touch
     */
    private float PrevY;
    /**
     * Stores the X coordinate of the starting point on touch
     */
    private float PrevX;
    /**
     * Stores the Touch Tolerance for interpolation
     */
    private static final float TOUCH_TOLERANCE = 4;
    /**
     * To keep track of whether the finger moved or not
     */
    private boolean itMoved = false;

    public CanvasView(Context context,AttributeSet attrs){
        super(context,attrs);
        setupCanvas();
    }

    /**
     * sets up the canvas for drawing
     * @param NONE
     */
    protected void setupCanvas() {
        path_canvas = new Path();
        path_total = new Path();
        paint_canvas = new Paint();
        brush_size = 10;
        path_color = 0xFF660000;
        paint_canvas.setColor(path_color);
        paint_canvas.setAntiAlias(true);
        paint_canvas.setStrokeWidth(brush_size);
        paint_canvas.setStyle(Paint.Style.STROKE);
        paint_canvas.setStrokeJoin(Paint.Join.ROUND);
        paint_canvas.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Takes in the Arraylist of points and converts it into an array for creating points using canvas.drawPoints().
     * @param points
     * @return float array
     */
    protected float[] getArrayOfPoints(ArrayList<Point> points){
        int length = points.size()*2;
        float[] pointsarray = new float[length];
        for(int i=0; i<points.size()*2; i=i+2) {
            pointsarray[i]= points.get(i/2).x;
            pointsarray[i+1]= points.get(i/2).y;
        }
        return pointsarray;
    }
    /**
     * Sets up the size of the Bitmap used in the view when the size is changed or set and initializes a new Bitmap based on that.
     * @param w new width
     * @param h new height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(canvas_bitmap);
    }

    /**
     * Recreates the Bitmap from the saved bitmap and draws the new path on the canvas.
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(canvas_bitmap, 0, 0, paint_canvas);
        canvas.drawPath(path_canvas, paint_canvas);
        canvas.drawPoints(getArrayOfPoints(points), paint_canvas);
    }

    /**
     * Calculates the path between two coordinates using interpolation
     * @param touchX
     * @param touchY
     */
    protected void when_moving(float touchX, float touchY) {
        float Xdiff = Math.abs(touchX - PrevX);
        float Ydiff = Math.abs(touchY - PrevY);
        if (Xdiff >= TOUCH_TOLERANCE || Ydiff >= TOUCH_TOLERANCE) {
            path_canvas.quadTo(PrevX, PrevY, (touchX + PrevX)/2, (touchY + PrevY)/2);
            PrevX = touchX;
            PrevY = touchY;
        }
    }

    /**
     * Checks if the finger has moved or not
     * @param touchX
     * @param touchY
     * @return true if it moved else false
     */
    protected boolean checkIfMoved(float touchX, float touchY) {
        float Xdiff = Math.abs(touchX - PrevX);
        float Ydiff = Math.abs(touchY - PrevY);
        if (Xdiff >= TOUCH_TOLERANCE || Ydiff >= TOUCH_TOLERANCE) {
            return true;
        }
        return false;
    }

    /**
     * On a touch event it adds a line from previous coordinates to new coordinates.
     * @param event
     * @return True if the event was a touch event and false if it was not a touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path_canvas.moveTo(touchX, touchY);
                PrevX = touchX;
                PrevY = touchY;
                itMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                when_moving(touchX, touchY);
                itMoved = true;
                break;
            case MotionEvent.ACTION_UP:
                path_canvas.lineTo(touchX, touchY);
                path_total.addPath(path_canvas);
                if(!itMoved) {
                    points.add(new Point((int) touchX, (int) touchY));
                }
                canvas.drawPath(path_canvas, paint_canvas);
                path_canvas.reset();
                break;
            default:
                return false;
        }
        //destroy the canvas to be made again
        invalidate();
        return true;
    }
}
