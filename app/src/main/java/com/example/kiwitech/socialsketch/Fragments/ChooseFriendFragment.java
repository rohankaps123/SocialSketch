package com.example.kiwitech.socialsketch.Fragments;
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
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.ChooseFriendsArrayAdapter;
import com.example.kiwitech.socialsketch.MainActivity;
import com.example.kiwitech.socialsketch.R;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A fragment representing a list of friends to add to the canvas for shared drawing
 * Activities containing this fragment MUST implement the {@link ChooseFriendFragmentListener}
 * interface.
 * @author Rohan Kapoor
 * @since 1.0
 */
public class ChooseFriendFragment extends Fragment {
    /**
     * Tag for logging data
     */
    private static final String TAG = ChooseFriendFragment.class.getSimpleName();
    /**
     * Listener class to be implemented in the class that implements ChooseFriendFragment
     */
    private ChooseFriendFragmentListener mListener;
    /**
     * ListView that shows all the friends
     */
    private ListView userlist;
    /**
     * Reference to the database
     */
    final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
    /**
     * List of all the existing friends
     */
    private ArrayList<String> friendslist;
    /**
     * EditText that contains the email to search from
     */
    private EditText email_search;
    /**
     * String that will be searched (email)
     */
    private String searchstr;
    /**
     * Listener to listen for addd child events in the database
     */
    private ChildEventListener newfriendsListener;
    /**
     * Adapter for listView
     */
    ArrayAdapter<String> friendlistadapter;
    /**
     * context for the fragment
     */
    private Context thiscontext;
    /**
     * Arraylist for storing the emails for the users got using userID
     */
    private ArrayList<String> friendslistemail;

    /*
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChooseFriendFragment() {
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
        View thisView = inflater.inflate(R.layout.friend_list, container, false);
        userlist = (ListView) thisView.findViewById(R.id.add_friend_list_view);
        friendslist = new ArrayList<String>();
        friendslistemail = new ArrayList<String>();
        // Set Adapter for listview
        friendlistadapter = new ChooseFriendsArrayAdapter(thiscontext,
                R.layout.friend_list_item, friendslistemail,friendslist);
        userlist.setAdapter(friendlistadapter);
        //Set What to do on Row click
        userlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);
                CheckBox added_check = (CheckBox) view.findViewById(R.id.friend_list_item_checkbox);
                //Set checked if it was checked and unchecked if it was unchecked.
                //Remove as a member if it was checked and add as a member if it was unchecked
                if(added_check.isChecked()){
                    added_check.setChecked(false);
                    untag(MainActivity.getThisRoomID(),friendslist.get(position));
                    postData(MainActivity.getThisUserName() + " Removed you from the group " + MainActivity.getThisRoomName(), friendslist.get(position));
                    mListener.ChooseFriendFragmentInteraction("remove", friendslist.get(position), friendslistemail.get(position));
                }
                else{
                    added_check.setChecked(true);
                    postData(MainActivity.getThisUserName() + " added you to the group " + MainActivity.getThisRoomName(), friendslist.get(position));
                    tag(MainActivity.getThisRoomID(), friendslist.get(position));
                    mListener.ChooseFriendFragmentInteraction("add",friendslist.get(position), friendslistemail.get(position));
                }
            }
        });

        Button search = (Button) thisView.findViewById(R.id.add_friend_button);
        email_search = (EditText) thisView.findViewById(R.id.add_friend_searchbox);
        search.setOnClickListener(ButtonHandler);
        // Create New listener for getting data from the firebase. The reference is stored in newfriendsListener
        // So that it can be removed on pause
        newfriendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null ){
                    //Add userId to the list of friends
                    friendslist.add(dataSnapshot.getKey());
                    //Add email to the list of the friends
                    friendslistemail.add((String)dataSnapshot.getValue());
                    //Notify the adapter that the data is changed
                    friendlistadapter.notifyDataSetChanged();
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
        getFriendsFromDB();
        return thisView;
    }


    @Override
    public void onPause(){
        super.onPause();
        //Remove the eventListener
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").removeEventListener(newfriendsListener);
    }

    /**
     * Onlick listener to keep track of the add frind button
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            switch (v.getId()) {
                case R.id.add_friend_button:
                    //close the keyboard on click
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    addUserAsFriend();
                    break;
            }
        }
    };

    /**
     * Get friends fromt he DataBase
     */
    private void getFriendsFromDB(){
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").addChildEventListener(newfriendsListener);
    }

    /**
     * Add the user as a friend to the database using emai ID
     */
    private void addUserAsFriend(){
        searchstr= email_search.getText().toString();
        mFirebaseRef.child("users").orderByChild("email").equalTo(searchstr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot querySnapshot) {
                        if (querySnapshot.getChildrenCount() == 0) {
                            //User Does not exist if the query was empty
                            Toast.makeText(getActivity(), "The User Does not exist", Toast.LENGTH_SHORT).show();

                        } else {
                            /*Add the Query result (USERID associated with the email) into the database in the friends field.
                            If the user does not exist in the list already.
                            */
                            for (final DataSnapshot child : querySnapshot.getChildren()) {
                                if (!checkIfFriendExists(child.getKey())) {
                                    mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").child(child.getKey())
                                            .setValue(searchstr,
                                                    new Firebase.CompletionListener() {
                                                        @Override
                                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                            if (firebaseError != null) {
                                                                Log.e(TAG, firebaseError.getMessage().toString());
                                                            } else {
                                                                postData(MainActivity.getThisUserName() + " added you as a friend", child.getKey());
                                                                Toast.makeText(getActivity(), "The User was added to your friend list", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                } else {
                                    Toast.makeText(getActivity(), "The User is already your friend", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });
    }


    public void postData(final String message, final String target) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> messageData = new HashMap<String, String>();
                messageData.put("platform", "1");
                messageData.put("alias", target);
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



    public void untag(final String tag, final String target) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> messageData = new HashMap<String, String>();
                messageData.put("platform", "1");
                messageData.put("tag", tag);
                messageData.put("alias", target);
                String json = new GsonBuilder().create().toJson(messageData, Map.class);
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPut httpput = new HttpPut("https://api.pushbots.com/tag/del");

                try {
                    httpput.addHeader("x-pushbots-appid", "55a616cb1779595f718b4567");
                    httpput.addHeader("x-pushbots-secret", "b4cd0c3abba32b1f764002e47b410f7e");
                    httpput.addHeader("Content-Type", "application/json");
                    httpput.setEntity(new StringEntity(json));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httpput);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    }
    public void tag(final String tag, final String target) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> messageData = new HashMap<String, String>();
                messageData.put("platform", "1");
                messageData.put("tag", tag);
                messageData.put("alias", target);
                String json = new GsonBuilder().create().toJson(messageData, Map.class);
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPut httpput = new HttpPut("https://api.pushbots.com/tag");

                try {
                    httpput.addHeader("x-pushbots-appid", "55a616cb1779595f718b4567");
                    httpput.addHeader("x-pushbots-secret", "b4cd0c3abba32b1f764002e47b410f7e");
                    httpput.addHeader("Content-Type", "application/json");
                    httpput.setEntity(new StringEntity(json));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httpput);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        }).start();
    }
    /**
     * Check if the user exists in the friends list already
     * @param ID Takes in the USERID of the user
     * @return Returns true or false
     */
    private boolean checkIfFriendExists(String ID) {
        if(friendslist.contains(ID))
            return true;
        else
            return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ChooseFriendFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ChooseFriendFragmentListener {
        public void ChooseFriendFragmentInteraction(String action,String userID, String userEmail);
    }

}
