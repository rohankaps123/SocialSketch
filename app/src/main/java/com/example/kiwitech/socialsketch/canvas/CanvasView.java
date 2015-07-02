package com.example.kiwitech.socialsketch.canvas;
import com.example.kiwitech.socialsketch.DataTypes.*;
import android.content.Context;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;

import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.kiwitech.socialsketch.DataTypes.PathObject;
import com.example.kiwitech.socialsketch.MainActivity;
import com.example.kiwitech.socialsketch.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;


/**
 * View for Canvas
 *
 * This view is designed to display the canvas. It uses a canvas initialized on a Bitmap to draw.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class CanvasView extends View{
    private static final String TAG = CanvasView.class.getSimpleName();

    /**
     * Saves data for each segment and can be used to send to other users.
     */
    private SegmentData segment;
    /**
     * Represents the Bitmap that the canvas draws to.
     */
    private Bitmap canvas_bitmap;
    /**
     * Represents the canvas that the user draws t
     */
    private Canvas canvas;
    /**
     * Represents the Brush Size;
     */
    private int brush_size;
    /**
     * PathCanvas is an object that stores the data for instantaneous interaction between the finger
     * and the canvas
     */
    private PathObject path_canvas;
    /**
     * A Stack to store all interactions.
     */
    private Stack<PathObject> paths = new Stack<>();
    /**
     * A Stack to store all interactions that have been undo for redo.
     */
    private Stack<PathObject> redoStack = new Stack<>();

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
    /**
     * Save the color state after setting brush from eraser
     */
    private int saved_color=0;
    /**
     * Save the alpha state after setting brush from eraser
     */
    private int saved_alpha = 255;
    /**
     * Keeps track whether the user is coming from erase mode or not
     */
    private boolean eraseMode =false;
    /**
     * Eraser size
     */
    private int eraser_size;

    private Boolean newCanvas = true;
    /**
     * Constructor to setup the Canvas.
     *
     * @param context Context of the view
     * @param attrs  Attributes passed to the View
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupCanvas();
    }

    /**
     * Sets up the canvas for drawing
     */
    protected void setupCanvas() {
        segment = new SegmentData();
        path_canvas = new PathObject(new Paint());
        canvas = new Canvas();
        brush_size = 10;
        eraser_size = 10;
        path_color = 0xFF660000;
        path_canvas.getPaint().setColor(path_color);
        path_canvas.getPaint().setAntiAlias(true);
        path_canvas.getPaint().setStrokeWidth(brush_size);
        path_canvas.getPaint().setStyle(Paint.Style.STROKE);
        path_canvas.getPaint().setStrokeJoin(Paint.Join.ROUND);
        path_canvas.getPaint().setStrokeCap(Paint.Cap.ROUND);
        path_canvas.getPaint().setAlpha(255);

    }



    /**
     * Sets up the size of the Bitmap used in the view when the size is changed or set and initializes a new Bitmap based on that.
     *
     * @param w    new width
     * @param h    new height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(canvas_bitmap);
        canvas.drawColor(0xFFFFFFFF);
    }

    /**
     * Recreates the Bitmap from the saved bitmap and draws the new path on the canvas.
     *
     * @param canvas The Canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = path_canvas.getPaint().getAlpha();
        path_canvas.getPaint().setAlpha(255);
        canvas.drawBitmap(canvas_bitmap, 0, 0, path_canvas.getPaint());
        path_canvas.getPaint().setAlpha(alpha);
        canvas.drawPath(path_canvas.getPath(), path_canvas.getPaint());
    }


    /**
     * Calculates the path between two coordinates using interpolation and adds the quadratic to the Path.
     *
     * @param touchX X Coordinate for Touch
     * @param touchY Y coordinate for Touch
     */
    protected void when_moving(float touchX, float touchY) {
        float Xdiff = Math.abs(touchX - PrevX);
        float Ydiff = Math.abs(touchY - PrevY);
        if (Xdiff >= TOUCH_TOLERANCE || Ydiff >= TOUCH_TOLERANCE) {
            path_canvas.getPath().quadTo(PrevX, PrevY, (touchX + PrevX) / 2, (touchY + PrevY) / 2);
            PrevX = touchX;
            PrevY = touchY;
            //Sets that the interaction is not a point
            itMoved = true;
        }
    }

    /**
     * On a touch event it adds a line from previous coordinates to new coordinates.
     * @param event MotionEvent that has happened
     * @return True if the event was a touch event and false if it was not a touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        newCanvas = false;
        redoStack.removeAllElements();
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Moves the finger to the coordinates of action down
                path_canvas.getPath().moveTo(touchX, touchY);
                PrevX = touchX;
                PrevY = touchY;
                //add point to the current segment
                segment.addPoint(touchX, touchY);
                //At this point the interaction can be either a path or a point
                itMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //If the finger moves the interaction is a path not a point. Add the the path using when_moving.
                when_moving(touchX, touchY);
                //add point to the current segment
                segment.addPoint(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (!itMoved) {
                    //Interaction is a point. Set its coordinates in path_canvas and add it to the paths stack.
                    path_canvas.getPoint().set((int) touchX, (int) touchY);
                    canvas.drawPoint(path_canvas.getPoint().x, path_canvas.getPoint().y, path_canvas.getPaint());
                    path_canvas.setIsPoint(true);
                    paths.add(path_canvas);
                    // Make a single point segment
                    segment.addSinglePoint(path_canvas.getPoint().x, path_canvas.getPoint().y);

                } else {
                    //Interaction is a path. Set the path_canvas and add it to the paths stack.
                    path_canvas.getPath().lineTo(touchX, touchY);
                    canvas.drawPath(path_canvas.getPath(), path_canvas.getPaint());
                    path_canvas.setIsPoint(false);
                    paths.add(path_canvas);
                    //add point to the current segment
                    segment.addPoint(touchX, touchY);
                }
                if(!MainActivity.getIsLocal()) {
                    Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
                    Serializer s = new Serializer();
                    String str;
                    segment.setBrush_size((int) path_canvas.getPaint().getStrokeWidth());
                    segment.setColor(path_canvas.getPaint().getColor());
                    segment.setSizeOrigin(new Pair((float)getHeight(),(float)getWidth()));
                    segment.setIsErase(eraseMode);
                    try {
                        byte[] by_new = s.serialize(segment);
                        str = Base64.encodeToString(by_new, 0);
                        String keyPush = mFirebaseRef.child("canvas").child(MainActivity.getThisRoomID())
                                .push().getKey();
                        mFirebaseRef.child("canvas").child(MainActivity.getThisRoomID())
                                .child(keyPush).child(MainActivity.getThisUserID()).setValue(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Renew path_canvas and segment for next interaction
                Paint paint = new Paint(path_canvas.getPaint());
                path_canvas = new PathObject(paint);
                segment.reset();
                break;
            default:
                return false;
        }
        //destroy the canvas to be made again
        invalidate();
        return true;
    }



    /**
     * Changes the color on after receiving a message from the dialogue
     */
    public void changeColor(int color) {
        path_color = color;
        saved_color = color;
        path_canvas.getPaint().setColor(path_color);
    }

    /**
     * Changes the BrushSize
     */
    public void setBrush() {
        //Load the saved color if it was not set to erase
        if(eraseMode) {
            path_color = saved_color;
            path_canvas.getPaint().setAlpha(saved_alpha);
            eraseMode = false;
        }
        path_canvas.getPaint().setColor(path_color);
        path_canvas.getPaint().setStrokeWidth(brush_size);
    }


    /**
     * Sets the brush type to erase and invoke a dialog to set the size
     */
    public void setEraser() {
        //save the current path_color and change it to erase
        eraseMode = true;
        saved_alpha = path_canvas.getPaint().getAlpha();
        saved_color = path_color;
        path_color = 0xFFFFFFFF;
        path_canvas.getPaint().setColor(path_color);
        path_canvas.getPaint().setAlpha(255);
        path_canvas.getPaint().setStrokeWidth(eraser_size);
    }

    /**
     * Clears the canvas and resets the stacks
     */
    public void clearCanvas() {
        newCanvas = true;
        int w = getWidth();
        int h = getHeight();
        paths.removeAllElements();
        redoStack.removeAllElements();
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(canvas_bitmap);
        canvas.drawColor(0xFFFFFFFF);
        invalidate();
    }

    /**
     * Undoes the last drawn item
     */
    public void pathUndo() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(canvas_bitmap);
        canvas.drawColor(0xFFFFFFFF);
        //Remove the last path from the stack if there is one and add it to the redo stack
        if (!paths.isEmpty()) {
            redoStack.add(paths.pop());
        } else {
            return;
        }
        //redraw all the paths drawn earlier
        for (PathObject a : paths) {
            if (a.CheckifPoint()) {
                canvas.drawPoint(a.getPoint().x, a.getPoint().y, a.getPaint());
            } else {
                canvas.drawPath(a.getPath(), a.getPaint());
            }
        }
        invalidate();
    }

    /**
     * redraws the items undoed
     */
    public void pathRedo() {
            if (!redoStack.isEmpty()) {
            PathObject redoPath = redoStack.pop();
            //Redraws the path in the redoStack and adds it to the paths stack as it is back on the canvas
            if (redoPath.CheckifPoint()) {
                canvas.drawPoint(redoPath.getPoint().x, redoPath.getPoint().y, redoPath.getPaint());
                paths.add(redoPath);
            } else {
                canvas.drawPath(redoPath.getPath(), redoPath.getPaint());
                paths.add(redoPath);
            }
            invalidate();
            }
    }


    /**
     * Returns the Bitmap of the canvas
     * @return Bitmap of the canvas
     */
    public Bitmap getCanvas_bitmap() {
        return canvas_bitmap;
    }

    /**
     * Returns the current set color of the brush
     * @return Color of the current brush
     */
    public int getCurrentPathColor() {
        if(eraseMode) {
            path_color = saved_color;
            path_canvas.getPaint().setColor(path_color);
            eraseMode = false;
        }
        return path_canvas.getPaint().getColor();
    }

    /**
     * returns the eraser size
     * @return int eraser size
     */
    public int getEraser_size() {
        return eraser_size;
    }

    /**
     * Returns the brush size of the paint_Object
     * @return Brush Size
     */
    public int getBrush_size(){
        return brush_size;
    }

    /**
     * Sets the eraser size
     * @param size size for eraser
     */
    public void setEraser_size(int size){
        eraser_size = size;
        path_canvas.getPaint().setStrokeWidth(eraser_size);
    }

    /**
     * Sets Brush Size
     * @param size brush size
     */
    public void setBrush_size(int size){
        brush_size = size;
        path_canvas.getPaint().setStrokeWidth(brush_size);
    }

    public void updateCanvas(String segment) throws IOException, ClassNotFoundException {

        Serializer s = new Serializer();
        byte[] by_new =  Base64.decode(segment, 0);
        SegmentData nsegment = (SegmentData) s.deserialize(by_new);
        Paint paint = new Paint(path_canvas.getPaint());
        PathObject npath = new PathObject(paint);
        npath.getPaint().setColor(nsegment.getColor());
        ArrayList<Pair<Float,Float>> pointlist = nsegment.getArrayList();


        float preHeight = nsegment.getSizeOrigin().getX();
        float preWidth = nsegment.getSizeOrigin().getY();
        float w = getWidth();
        float h = getHeight();
        float scaleHeight = h/preHeight;
        float scaleWidth = w/preWidth;
        for(Pair<Float,Float> point : pointlist){
            point.setX(point.getX()*scaleWidth);
            point.setY(point.getY()*scaleHeight);
        }

        npath.getPaint().setStrokeWidth(nsegment.getBrush_size());
        if(pointlist.size() == 1){
            npath.setIsPoint(true);
            npath.getPoint().set(Math.round(pointlist.get(0).getX()), Math.round(pointlist.get(0).getY()));
            canvas.drawPoint(npath.getPoint().x, npath.getPoint().y, npath.getPaint());
            paths.add(npath);
        }
        else{
            npath.setIsPoint(false);
            npath.getPath().moveTo(pointlist.get(0).getX(),pointlist.get(0).getY());
            float previousX = pointlist.get(0).getX();
            float previousY = pointlist.get(0).getY();
            pointlist.remove(0);
            Pair<Float,Float> lastPoint = pointlist.get(pointlist.size() - 1);
            pointlist.remove(pointlist.size() - 1);
            for(Pair<Float,Float> point : pointlist){
                float Xdiff = Math.abs(point.getX() - previousX);
                float Ydiff = Math.abs(point.getY() - previousY);
                if (Xdiff >= TOUCH_TOLERANCE || Ydiff >= TOUCH_TOLERANCE) {
                    npath.getPath().quadTo(previousX, previousY, (point.getX() + previousX) / 2, (point.getY() + previousY) / 2);
                    previousX = point.getX();
                    previousY = point.getY();
                }
            }
            npath.getPath().lineTo(lastPoint.getX(), lastPoint.getY());
            canvas.drawPath(npath.getPath(), npath.getPaint());
            paths.add(npath);
        }
        invalidate();
    }

    public Boolean isNewCanvas() {
        return newCanvas;
    }
}