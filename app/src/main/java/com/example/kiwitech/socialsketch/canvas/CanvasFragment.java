package com.example.kiwitech.socialsketch.canvas;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Canvas;
import android.widget.Button;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import afzkl.development.colorpickerview.dialog.ColorPickerDialogFragment;

/**
 * FRAGMENT for Canvas
 *
 * Added Fragment to draw the canvas. It inflates the Canvas view when it is created.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */

public class CanvasFragment extends Fragment{
    /**
     * Callback object to communicate with the main activity and give it the current color.
     */
    OnMessageListener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnMessageListener {
        /** Called by CanvasFragment when a a color Picker dialogue needs to be called */
        void onColorChangeSelected(int currentColor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.canvas, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMessageListener");
        }
    }

    /**
     * Receives the message that a button has been selected in the toolbar and invokes the right function
     *
     *
     * @param what_option Int referencing the selected button
     */
    public void buttonSelected(int what_option){
        CanvasView cview = (CanvasView) getView();
            switch (what_option) {
                case 1:
                    //Get Current color and send it to the activity so that a new color pick dialog can start
                    int cColor = cview.getCurrentPathColor();
                    mCallback.onColorChangeSelected(cColor);
                    break;
                case 2:
                    cview.changeBrushSize();
                    break;
                case 3:
                    cview.setEraser();
                    break;
                case 4:
                    cview.clearCanvas();
                    break;
                case 5:
                    cview.pathUndo();
                    break;
                case 6:
                    cview.pathRedo();
                    break;
                case 7:
                    SaveImage(cview.getCanvas_bitmap());
                    break;
                case 8:
                    shareImage(cview.getCanvas_bitmap());
            }
        }
    /**
     * Takes a Bimap and compresses it into a PNG and stores it to internal Storage
     * @param finalBitmap
     */
    private void SaveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SocialSketch/Saved");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".PNG" ;
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(myDir+"/"+ fname);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);

        final Dialog fileSaved = new Dialog(getActivity());
        fileSaved.setTitle("File Saved");
        TextView textView = (TextView) fileSaved.findViewById(android.R.id.title);
        if(textView != null)
        {
            textView.setGravity(Gravity.CENTER);
        }
        fileSaved.setContentView(R.layout.file_saved_layout);
        Button okay = (Button) fileSaved.findViewById(R.id.okay_button_filesaved);
        View.OnClickListener ButtonHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss on clicking the OK button
                fileSaved.dismiss();
            }
        };
        okay.setOnClickListener(ButtonHandler);
        fileSaved.setCancelable(true);
        fileSaved.show();
    }

    private void shareImage(Bitmap bitmap){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SocialSketch/temp");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".PNG" ;
        File file = new File (myDir, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File f = new File(myDir+"/"+ fname);
        Uri contentUri = Uri.fromFile(f);
        shareIntent.setData(contentUri);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }


    /**
     * Calls changeColor to set the Color to the new selected color.
     * @param newColor new selected color
     */
    public void colorChange(int newColor) {
        CanvasView cview = (CanvasView) getView();
        cview.changeColor(newColor);
    }

}

