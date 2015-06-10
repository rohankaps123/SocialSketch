package com.example.kiwitech.socialsketch.canvas;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.DataTypes.PathObject;
import com.example.kiwitech.socialsketch.R;

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

    //Initializing different objects to for the view
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
    /**
     * Save the color state after setting brush from eraser
     */
    private int saved_color=0;
    /**
     * Keeps track whether the user is coming from erase mode or not
     */
    private boolean eraseMode =false;

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
        canvas = new Canvas();
        paint_canvas = new Paint();
        brush_size = 10;
        path_color = 0xFF660000;
        paint_canvas.setColor(path_color);
        paint_canvas.setAntiAlias(true);
        paint_canvas.setStrokeWidth(brush_size);
        paint_canvas.setStyle(Paint.Style.STROKE);
        paint_canvas.setStrokeJoin(Paint.Join.ROUND);
        paint_canvas.setStrokeCap(Paint.Cap.ROUND);
        path_canvas = new PathObject(paint_canvas);
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
    }

    /**
     * Recreates the Bitmap from the saved bitmap and draws the new path on the canvas.
     *
     * @param canvas The Canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = paint_canvas.getAlpha();
        paint_canvas.setAlpha(255);
        canvas.drawBitmap(canvas_bitmap, 0, 0, paint_canvas);
        paint_canvas.setAlpha(alpha);
        canvas.drawPath(path_canvas.getPath(), paint_canvas);
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
        redoStack.removeAllElements();
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Moves the finger to the coordinates of action down
                path_canvas.getPath().moveTo(touchX, touchY);
                PrevX = touchX;
                PrevY = touchY;
                //At this point the interaction can be either a path or a point
                itMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //If the finger moves the interaction is a path not a point. Add the the path using when_moving.
                when_moving(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (!itMoved) {
                    //Interaction is a point. Set its coordinates in path_canvas and add it to the paths stack.
                    path_canvas.getPoint().set((int) touchX, (int) touchY);
                    canvas.drawPoint(path_canvas.getPoint().x, path_canvas.getPoint().y, paint_canvas);
                    path_canvas.setIsPoint(true);
                    paths.add(path_canvas);
                } else {
                    //Interaction is a path. Set the path_canvas and add it to the paths stack.
                    path_canvas.getPath().lineTo(touchX, touchY);
                    canvas.drawPath(path_canvas.getPath(), paint_canvas);
                    path_canvas.setIsPoint(false);
                    paths.add(path_canvas);
                }
                // Renew path_canvas and paint_canvas for next interaction
                paint_canvas = new Paint(paint_canvas);
                path_canvas = new PathObject(paint_canvas);
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
        paint_canvas.setColor(path_color);
    }

    /**
     * Invokes The change Brush Size dialog
     */
    public void changeBrushSize() {
        //Load the saved color if it is not set to erase
        if(eraseMode) {
            path_color = saved_color;
        }
        paint_canvas.setColor(path_color);
        setupSliderDialog();
    }

    /**
     * This sets up a Slider Dialog to change the size of the brush
     */
    public void setupSliderDialog(){
        final Dialog sizeSetter = new Dialog(getContext());
        sizeSetter.setContentView(R.layout.slider_size_dialog);
        //set the title
        sizeSetter.setTitle("Set Size");
        TextView textView = (TextView) sizeSetter.findViewById(android.R.id.title);
        if(textView != null)
        {
            textView.setGravity(Gravity.CENTER);
        }
        sizeSetter.setCancelable(true);
        sizeSetter.show();
        //Get the slider reference
        SeekBar slider = (SeekBar)sizeSetter.findViewById(R.id.slider_dialog);
        slider.setFocusable(true);
        //Set Max Brush Size to 200 px
        slider.setMax(200);
        if(brush_size != 0){
            slider.setProgress(brush_size);
        }
        //Setup Textview to display the progress
        TextView progressText = (TextView)sizeSetter.findViewById(R.id.progressText_dialog);
        progressText.setText(String.valueOf(brush_size) + "px");
        SeekBar.OnSeekBarChangeListener onseekbarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //change the progress in the textview
                TextView progressText = (TextView)sizeSetter.findViewById(R.id.progressText_dialog);
                progressText.setText(String.valueOf(progress+"px"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //set the new size of the brush
                brush_size = seekBar.getProgress();
                paint_canvas.setStrokeWidth(brush_size);
            }
        };

        slider.setOnSeekBarChangeListener(onseekbarListener);
        Button okay = (Button) sizeSetter.findViewById(R.id.okay_button_size_dialog);
        View.OnClickListener ButtonHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss on clicking the OK button
                sizeSetter.dismiss();
            }
        };
        okay.setOnClickListener(ButtonHandler);
    }

    /**
     * Sets the brush type to erase and invoke a dialog to set the size
     */
    public void setEraser() {
        //save the current path_color and change it to erase
        eraseMode = true;
        saved_color = path_color;
        path_color = 0xFFFFFFFF;
        paint_canvas.setColor(path_color);
        setupSliderDialog();
    }

    /**
     * Clears the canvas and resets the stacks
     */
    public void clearCanvas() {
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        paths.removeAllElements();
        redoStack.removeAllElements();
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(canvas_bitmap);
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
     * Shares the jpeg to other apps and saving to the gallery
     */
    public void share() {
        Log.d("selected", "upload");
    }

    /**
     * Returns the current set color of the brush
     * @return Color of the current brush
     */
    public int getCurrentPathColor() {
        return paint_canvas.getColor();
    }
}