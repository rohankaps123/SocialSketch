package com.example.kiwitech.socialsketch;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.ChatMessageAdapter;
import com.example.kiwitech.socialsketch.DataTypes.ChooseFriendsArrayAdapter;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;



/**
 * A fragment representing a list of friends to add to the canvas for shared drawing
 * @author Rohan Kapoor
 * @since 1.0
 */
public class ChatFragment extends Fragment {
    /**
     * Tag for logging data
     */
    private static final String TAG = ChatFragment.class.getSimpleName();

    /**
     * ListView that shows all the friends
     */
    private ListView messageViewList;
    /**
     * Reference to the database
     */
    final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
    /**
     * List of all the existing friends
     */
    private ArrayList<String> members;
    private ArrayList<String> messages;
    private ArrayList<String> membersNames;


    /**
     * Listener to listen for addd child events in the database
     */
    private ChildEventListener newMessageListener;
    /**
     * Adapter for listView
     */
    ArrayAdapter<String> messageAdapter;
    /**
     * context for the fragment
     */
    private Context thiscontext;


    /*
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the back button in the actionbar
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // On Back button in action bar pressed return to main activity
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        thiscontext = container.getContext();
        View thisView = inflater.inflate(R.layout.fragment_chat, container, false);
        messageViewList = (ListView) thisView.findViewById(R.id.chat_fragment_listview);
        messages = new ArrayList<String>();
        members = new ArrayList<String>();
        membersNames = new ArrayList<String>();
        // Set Adapter for listview
        messageAdapter = new ChatMessageAdapter(thiscontext,
                R.layout.chat_fragment_list_item, messages,members,membersNames);
        messageViewList.setAdapter(messageAdapter);


        Button send = (Button) thisView.findViewById(R.id.chat_fragment_button_send);

        send.setOnClickListener(ButtonHandler);
        // Create New listener for getting data from the firebase. The reference is stored in newfriendsListener
        // So that it can be removed on pause
        newMessageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot!= null ){
                    String key = "";
                    String str = "";
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        key = child.getKey();
                        str = (String) child.getValue();
                    }
                    mFirebaseRef.child("users").child(key).child("email")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    membersNames.add((String) dataSnapshot.getValue());
                                    //Notify the adapter that the data is changed
                                    messageAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                        //Add userId to the list of friends
                    members.add(key);
                    //Add email to the list of the friends
                    messages.add(str);

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        //Get The most recent list of the friends from the database
        getOldMessagesFromDB();
        return thisView;
    }


    @Override
    public void onPause(){
        super.onPause();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        //Remove the eventListener
        mFirebaseRef.child("messages").child(MainActivity.getThisRoomID()).removeEventListener(newMessageListener);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        //Remove the eventListener
        mFirebaseRef.child("messages").child(MainActivity.getThisRoomID()).removeEventListener(newMessageListener);
    }

    /**
     * Onlick listener to keep track of the add frind button
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            switch (v.getId()) {
                case R.id.chat_fragment_button_send:
                    //close the keyboard on click
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    sendMessage();
                    break;
            }
        }
    };

    /**
     * Get friends fromt he DataBase
     */
    private void getOldMessagesFromDB(){
        mFirebaseRef.child("messages").child(MainActivity.getThisRoomID()).addChildEventListener(newMessageListener);
    }

    /**
     * Add the user as a friend to the database using emai ID
     */
    private void sendMessage(){
        EditText message = (EditText) getView().findViewById(R.id.chat_fragment_message_edittext);
        String keyPush = mFirebaseRef.child("messages").child(MainActivity.getThisRoomID()).push().getKey();
        mFirebaseRef.child("messages").child(MainActivity.getThisRoomID())
                .child(keyPush).child(MainActivity.getThisUserID()).setValue(message.getText().toString());
    }

}