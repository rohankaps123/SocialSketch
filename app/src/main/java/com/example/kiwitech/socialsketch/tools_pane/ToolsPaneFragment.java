package com.example.kiwitech.socialsketch.tools_pane;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kiwitech.socialsketch.R;

public class ToolsPaneFragment extends Fragment {
    OnButtonSelectedListener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnButtonSelectedListener {
        /** Called by ToolsPaneFrament when a button is selected */
        public void OnButtonSelected(int what_buton);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tools, container, false);
    }

}
