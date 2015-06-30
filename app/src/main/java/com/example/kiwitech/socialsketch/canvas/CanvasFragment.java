package com.example.kiwitech.socialsketch.canvas;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.SegmentData;
import com.example.kiwitech.socialsketch.DataTypes.Serializer;
import com.example.kiwitech.socialsketch.MainActivity;
import com.example.kiwitech.socialsketch.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class CanvasFragment extends Fragment {


    private static Firebase mFirebaseRef;

    private ValueEventListener newSegment;


    /**
     * Saves data for each segment and can be used to send to other users.
     */
    private SegmentData segment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.canvas, container, false);
        return thisView;
    }


    public void addNewSegmentListener(){
        newSegment = new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null && !dataSnapshot.getKey().equals(MainActivity.getThisUserID())){
                CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                CanvasView cview = (CanvasView) canvasF.getView();
                    try {
                        cview.updateCanvas((String) dataSnapshot.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
        mFirebaseRef.child("canvas").child(MainActivity.getThisRoomID()).addValueEventListener(newSegment);
    }

    public void removeNewSegmentListener(){
       mFirebaseRef.child("canvas").child(MainActivity.getThisRoomID()).removeEventListener(newSegment);
    }


    /**
     * Takes a Bimap and compresses it into a PNG and stores it to internal Storage in a new thread
     * @param finalBitmap
     */
    public void SaveImage(final Bitmap finalBitmap, final Handler h) {

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Image Saved", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(MainActivity.getState().equals("canvas") && mFirebaseRef!=null){
        removeNewSegmentListener();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(MainActivity.getState().equals("canvas")&& mFirebaseRef!=null){
        removeNewSegmentListener();
        }
    }

    /**
     * Takes a Bimap and compresses it into a PNG and shares it with other apps
     * @param bitmap
     */
    public void shareImage(final Bitmap bitmap){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/SocialSketch/temp");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".PNG";
                File file = new File(myDir, fname);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                File f = new File(myDir + "/" + fname);
                Uri contentUri = Uri.fromFile(f);
                shareIntent.setData(contentUri);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share image using"));
            }
        }).start();
    }

    /**
     * This sets up a Slider Dialog to change the size of the brush
     * @param brush_size Takes in the Current Brush Size
     * @param case_option Reference to what option called the dialog to be setup
     */
    public void setupSliderDialog(final int brush_size, final int case_option){
        final Dialog sizeSetter = new Dialog(getActivity());
        sizeSetter.setContentView(R.layout.slider_size_dialog);
        //set the title
        sizeSetter.setTitle("Set Size");
        TextView textView = (TextView) sizeSetter.findViewById(android.R.id.title);
        if(textView != null)
        {
            textView.setGravity(Gravity.CENTER);
        }
        sizeSetter.setCancelable(false);
        sizeSetter.show();
        //Get the slider reference
        SeekBar slider = (SeekBar)sizeSetter.findViewById(R.id.slider_dialog);
        slider.setFocusable(true);
        //Set Max Brush Size to 200 px
        slider.setMax(200);
        if(brush_size != 0) {
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
                CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                CanvasView cview = (CanvasView) canvasF.getView();
                //set the new size of the brush
                if(case_option == 2){
                    cview.setBrush_size(seekBar.getProgress());
                }
                //set the new size of the eraser
                else if(case_option == 3){
                    cview.setEraser_size(seekBar.getProgress());
                }
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
     * listener interface implementation to communicate from canvas fragment to colorFragment and create a colorPicker dialog
     * @param currentColor Takes the current color of the brush
     */
    public void onColorChangeSelected(int currentColor){
        ColorPickerDialogFragment colorFragmentDialog = ColorPickerDialogFragment
                .newInstance(0, null, null, currentColor, true);
        colorFragmentDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme);
        colorFragmentDialog.show(getFragmentManager(), "d");
    }

    /**
     * Callback from the colorPicker Dialog to return the picked color
     * @param dialogId Id of the invoked dialog
     * @param color New selected color
     */


    public void onColorSelected(int dialogId, int color) {
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        CanvasView cview = (CanvasView) canvasF.getView();
        cview.changeColor(color);
    }

    /**
     *  To run things when the dialog is dismissed
     * @param dialogId Id of the invoked dialog
     */
    public void onDialogDismissed(int dialogId) {
    }
}

