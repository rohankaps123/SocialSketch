package com.example.kiwitech.socialsketch.canvas;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Canvas;
import android.widget.Button;

import com.example.kiwitech.socialsketch.R;

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
                    cview.share();
                    break;
            }
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

