package com.example.kiwitech.socialsketch;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.canvas.CanvasFragment;
import com.example.kiwitech.socialsketch.canvas.CanvasView;
import com.example.kiwitech.socialsketch.tools_pane.ToolsPaneFragment;
import com.firebase.client.Firebase;
import com.google.android.gms.common.AccountPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import afzkl.development.colorpickerview.dialog.ColorPickerDialogFragment;


/**
 * Main Activity
 * Handles communications between fragments
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class MainActivity extends Activity implements ToolsPaneFragment.OnButtonSelectedListener,ColorPickerDialogFragment.ColorPickerDialogListener{
    private Handler h;

    // on create displays the main activity xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        h = new Handler(MainActivity.this.getMainLooper());
        Firebase.setAndroidContext(this);
        LoginFragment login = new LoginFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_window,login , "Login").commit();
    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Deletes the temporaary files on resume after sharing
     */
    @Override
    public void onResume(){
        super.onResume();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SocialSketch/temp");
        if(myDir.exists()){
            for (File child : myDir.listFiles()){
                child.delete();
            }
            myDir.delete();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * listener interface implementation to communicate from toolFragment to canvas fragment
     * @param what_button  Int referencing the selected button
     */
    public void OnButtonSelected(int what_button){
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        CanvasView cview = (CanvasView) canvasF.getView();
        switch (what_button) {
            case 1:
                //Get Current color and send it to the activity so that a new color pick dialog can start
                int cColor = cview.getCurrentPathColor();
                onColorChangeSelected(cColor);
                break;
            case 2:
                setupSliderDialog(cview.getBrush_size(),2);
                break;
            case 3:
                setupSliderDialog(cview.getBrush_size(), 3);
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
     * Takes a Bimap and compresses it into a PNG and stores it to internal Storage in a new thread
     * @param finalBitmap
     */
    private void SaveImage(final Bitmap finalBitmap) {

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
               sendBroadcast(mediaScanIntent);
               h.post(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_LONG).show();
                   }
               });
           }
       }).start();
    }

    /**
     * Takes a Bimap and compresses it into a PNG and shares it with other apps
     * @param bitmap
     */
    private void shareImage(final Bitmap bitmap){
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
    private void setupSliderDialog(int brush_size, final int case_option){
        final Dialog sizeSetter = new Dialog(this);
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
                    cview.changeBrushSize(seekBar.getProgress());
                }
                else if(case_option == 3){
                    cview.setEraser(seekBar.getProgress());
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
    private void onColorChangeSelected(int currentColor){
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
    @Override
    public void onColorSelected(int dialogId, int color) {
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        CanvasView cview = (CanvasView) canvasF.getView();
        cview.changeColor(color);
    }

    /**
     *  To run things when the dialog is dismissed
     * @param dialogId Id of the invoked dialog
     */
    @Override
    public void onDialogDismissed(int dialogId) {
    }

}
