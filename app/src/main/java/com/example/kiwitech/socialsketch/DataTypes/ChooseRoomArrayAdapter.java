package com.example.kiwitech.socialsketch.DataTypes;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.ChooseRoomFragment;
import com.example.kiwitech.socialsketch.MainActivity;
import com.example.kiwitech.socialsketch.R;
import com.firebase.client.Firebase;

import java.util.List;

/**
 * A Class representing an Array Adpater to display a list of friends to be selected and bound to the room.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class ChooseRoomArrayAdapter extends ArrayAdapter<String> {

    //Context for the list view
    private final Context context;
    //List of objects to show in the List View
    private final List<String> objects;
    private final List<String> ids;
    private final ChooseRoomFragment fragment;


    public ChooseRoomArrayAdapter(Context context, int resource, List<String> email,List<String> ids, ChooseRoomFragment fragment) {
        super(context, resource, email);
        this.context = context;
        this.objects = email;
        this.ids = ids;
        this.fragment = fragment;
    }

    // Get the view For each row using the data
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.room_list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.room_list_item_name);
        textView.setText(objects.get(position));
        ImageView deleteButton = (ImageView) rowView.findViewById(R.id.room_list_button_delete);
        final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        mFirebaseRef.child("rooms").child(ids.get(position)).setValue(null);
                        mFirebaseRef.child("members").child(ids.get(position)).setValue(null);
                        mFirebaseRef.child("canvas").child(ids.get(position)).setValue(null);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onRoomSelected(position);
            }
        });

        return rowView;
    }
}
