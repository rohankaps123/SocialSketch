package com.example.kiwitech.socialsketch.DataTypes;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.MainActivity;
import com.example.kiwitech.socialsketch.R;

import java.util.List;

/**
 * A Class representing an Array Adpater to display a list of friends to be selected and bound to the room.
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class ChooseFriendsArrayAdapter extends ArrayAdapter<String> {
   //Context for the list view
    private final Context context;
    //List of objects to show in the List View
    private final List<String> objects;
    private final List<String> ids;


    public ChooseFriendsArrayAdapter(Context context, int resource, List<String> email, List<String> ID) {
        super(context, resource, email);
        this.context = context;
        this.objects = email;
        this.ids = ID;
    }

    // Get the view For each row using the data
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_list_item, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.friend_list_item_email);
        textView.setText(objects.get(position));
        CheckBox added_check = (CheckBox) rowView.findViewById(R.id.friend_list_item_checkbox);

        if(MainActivity.getRoomMembers().contains(ids.get(position))){
            added_check.setChecked(true);
        }
        else{
            added_check.setChecked(false);
        }
        return rowView;
    }

}
