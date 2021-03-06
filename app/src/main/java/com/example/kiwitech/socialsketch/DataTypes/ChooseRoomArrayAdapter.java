package com.example.kiwitech.socialsketch.DataTypes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.Fragments.ChooseRoomFragment;
import com.example.kiwitech.socialsketch.R;

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
    private final List<String> roomNames;
    //List of Room IDs
    private final List<String> roomIds;
    //Reference to the fragment
    private final ChooseRoomFragment fragment;

    //Constructor for the adapter
    public ChooseRoomArrayAdapter(Context context, int resource, List<String> email,List<String> ids, ChooseRoomFragment fragment) {
        super(context, resource, email);
        this.context = context;
        this.roomNames = email;
        this.roomIds = ids;
        this.fragment = fragment;
    }

    // Get the view For each row using the data
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.room_list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.room_list_item_name);
        textView.setText(roomNames.get(position));
        ImageView deleteButton = (ImageView) rowView.findViewById(R.id.room_list_button_delete);
        // Actions to on clicking different parts of the row view
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onRemoveRoomSelected(position);
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
