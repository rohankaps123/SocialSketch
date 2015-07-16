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
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
                    //Add userId to the list of friends
                    members.add(key);
                    //Add email to the list of the friends
                    messages.add(str);
                    membersNames.add(MainActivity.getRoomMembersName().get(MainActivity.getRoomMembers().indexOf(key)));
                    messageAdapter.notifyDataSetChanged();
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
    public void onDestroy(){
        super.onDestroy();
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
        postChat("New message in " + MainActivity.getThisRoomName(),MainActivity.getThisRoomID());
        mFirebaseRef.child("messages").child(MainActivity.getThisRoomID())
                .child(keyPush).child(MainActivity.getThisUserID()).setValue(message.getText().toString());
        message.setText("");
    }


    public void postChat(final String message, final String tag) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> messageData = new HashMap<String, String>();
                messageData.put("platform", "1");
                messageData.put("tags", tag);
                messageData.put("except_alias",MainActivity.getThisUserID());
                messageData.put("msg", message);
                String json = new GsonBuilder().create().toJson(messageData, Map.class);
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("https://api.pushbots.com/push/all");

                try {
                    httppost.addHeader("x-pushbots-appid", "55a616cb1779595f718b4567");
                    httppost.addHeader("x-pushbots-secret", "b4cd0c3abba32b1f764002e47b410f7e");
                    httppost.addHeader("Content-Type", "application/json");
                    httppost.setEntity(new StringEntity(json));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    }


}
