package com.example.kiwitech.socialsketch.tools_pane;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.kiwitech.socialsketch.R;

public class ToolsPaneFragment extends Fragment {
    OnButtonSelectedListener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnButtonSelectedListener {
        /** Called by ToolsPaneFrament when a button is selected */
        void OnButtonSelected(int what_buton);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView =  inflater.inflate(R.layout.tools, container, false);

        Button color = (Button) thisView.findViewById(R.id.color_button);
        Button brush = (Button) thisView.findViewById(R.id.brush_button);
        Button erase = (Button) thisView.findViewById(R.id.erase_button);
        Button clear = (Button) thisView.findViewById(R.id.clear_button);
        Button undo = (Button) thisView.findViewById(R.id.undo_button);
        Button redo = (Button) thisView.findViewById(R.id.redo_button);
        Button upload = (Button) thisView.findViewById(R.id.share_button);

        color.setOnClickListener(ButtonHandler);
        brush.setOnClickListener(ButtonHandler);
        erase.setOnClickListener(ButtonHandler);
        clear.setOnClickListener(ButtonHandler);
        undo.setOnClickListener(ButtonHandler);
        redo.setOnClickListener(ButtonHandler);
        upload.setOnClickListener(ButtonHandler);

        return thisView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnButtonSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.color_button:
                    mCallback.OnButtonSelected(1);
                    break;
                case R.id.brush_button:
                    mCallback.OnButtonSelected(2);
                    break;
                case R.id.erase_button:
                    mCallback.OnButtonSelected(3);
                    break;
                case R.id.clear_button:
                    mCallback.OnButtonSelected(4);
                    break;
                case R.id.undo_button:
                    mCallback.OnButtonSelected(5);
                    break;
                case R.id.redo_button:
                    mCallback.OnButtonSelected(6);
                    break;
                case R.id.share_button:
                    mCallback.OnButtonSelected(7);
                    break;
                default:
                    break;
            }
        }
    };
}
