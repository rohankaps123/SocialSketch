package com.example.kiwitech.socialsketch.DataTypes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.kiwitech.socialsketch.R;

import java.util.List;

/**
 * Created by kiwitech on 26/6/15.
 */
public class ChooseFriendsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> objects;

    public ChooseFriendsArrayAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.friend_list_item_email);
        textView.setText(objects.get(position));
        CheckBox added_check = (CheckBox) rowView.findViewById(R.id.friend_list_item_checkbox);

        added_check.setChecked(true);
        return rowView;
    }
}
