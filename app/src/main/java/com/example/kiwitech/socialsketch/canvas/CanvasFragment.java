package com.example.kiwitech.socialsketch.canvas;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Canvas;

import com.example.kiwitech.socialsketch.R;

/**
 * FRAGMENT for Canvas
 *
 * Added Fragment to draw the canvas. It inflates the Canvas view when it is created.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */

public class CanvasFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.canvas, container, false);
    }
}
