package com.example.kiwitech.socialsketch.DataTypes;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
public class ChatMessageAdapter extends ArrayAdapter<String> {

    //Context for the list view
    private final Context context;
    //List of objects to show in the List View
    private final List<String> messages;
    //List of Room IDs
    private final List<String> members;
    private final List<String> membersNames;
    //Reference to the fragment


    //Constructor for the adapter
    public ChatMessageAdapter(Context context, int resource, List<String> messages, List<String> members, List<String> membersNames) {
        super(context, resource,messages);
        this.context = context;
        this.messages = messages;
        this.members = members;
        this.membersNames = membersNames;
    }

    @Override
    public int getCount(){
        return membersNames.size();
    }

    // Get the view For each row using the data
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.chat_fragment_list_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.imageLeft = (ImageView)convertView.findViewById(R.id.ivProfileLeft);
            holder.imageRight = (ImageView)convertView.findViewById(R.id.ivProfileRight);
            holder.body = (TextView)convertView.findViewById(R.id.chat_fragment_message);
            convertView.setTag(holder);
        }

        final String message = messages.get(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = MainActivity.getThisUserID().equals(members.get(position));
        // Show-hide image based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        if (isMe) {
            holder.imageRight.setVisibility(View.VISIBLE);
            holder.imageLeft.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            holder.body.setText(message);
            return convertView;
        } else {
            holder.imageLeft.setVisibility(View.VISIBLE);
            holder.imageRight.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            holder.body.setText(membersNames.get(position) + " : " + message);
            return convertView;
        }
    }


    final class ViewHolder {
        public ImageView imageLeft;
        public ImageView imageRight;
        public TextView body;
    }

}
