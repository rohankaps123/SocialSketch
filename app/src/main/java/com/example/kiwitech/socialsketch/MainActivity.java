package com.example.kiwitech.socialsketch;


import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kiwitech.socialsketch.canvas.CanvasFragment;
import com.example.kiwitech.socialsketch.canvas.CanvasView;
import com.example.kiwitech.socialsketch.tools_pane.ToolsPaneFragment;

import java.io.File;

import afzkl.development.colorpickerview.dialog.ColorPickerDialogFragment;


/**
 * Main Activity
 * Handles communications between fragments
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class MainActivity extends Activity implements ToolsPaneFragment.OnButtonSelectedListener,ColorPickerDialogFragment.ColorPickerDialogListener,CanvasFragment.OnMessageListener {
    // on create displays the main activity xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        canvasF.buttonSelected(what_button);
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
    @Override
    public void onColorSelected(int dialogId, int color) {
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        canvasF.colorChange(color);
    }

    /**
     *  To run things when the dialog is dismissed
     * @param dialogId Id of the invoked dialog
     */
    @Override
    public void onDialogDismissed(int dialogId) {
    }

    @Override
    public void onResume(){
        super.onResume();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SocialSketch/temp");
        if(myDir.exists())
        myDir.delete();
    }
}
