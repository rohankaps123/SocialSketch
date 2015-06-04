package com.example.kiwitech.socialsketch;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

// creates a canvas View on which you can draw.

public class CanvasView extends View {

//Initializing different objects to for the view
    private Bitmap canvas_bitmap;
    private Canvas canvas ;
    private int brush_size;
    /* Path Canvas is a temporary path and stores the data between finger down and finger up.
    Can be later used to send data to different users so that they can replicate the canvas */
    private Path path_canvas;
    /* Stores the total Path so that it can be redrawn later or on a canvas of different size */
    private Path path_total;
    private Paint paint_canvas;
    private int path_color;


    public CanvasView(Context context,AttributeSet attrs){
        super(context,attrs);
        setupCanvas();
    }

    protected void setupCanvas() {
        //setting up basic attributes for drawing
        path_canvas = new Path();
        path_total = new Path();
        paint_canvas = new Paint();
        brush_size = 5;
        path_color = 0xFF660000;
        paint_canvas.setColor(path_color);
        paint_canvas.setAntiAlias(true);
        paint_canvas.setStrokeWidth(brush_size);
        paint_canvas.setStyle(Paint.Style.STROKE);
        paint_canvas.setStrokeJoin(Paint.Join.ROUND);
        paint_canvas.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(canvas_bitmap);
    }

    @Override

    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(canvas_bitmap,0,0,paint_canvas);
        canvas.drawPath(path_canvas,paint_canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path_canvas.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                path_canvas.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                path_canvas.lineTo(touchX, touchY);
                canvas.drawPath(path_canvas, paint_canvas);
                path_total.addPath(path_canvas);
                path_canvas.reset();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }
}
