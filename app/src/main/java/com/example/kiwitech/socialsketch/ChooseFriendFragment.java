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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.ChooseFriendsArrayAdapter;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * A fragment representing a list of Users.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link ChooseFriendFragmentListener}
 * interface.
 */
public class ChooseFriendFragment extends Fragment {
    private static final String TAG = ChooseFriendFragment.class.getSimpleName();
    private ChooseFriendFragmentListener mListener;
    private ListView userlist;
    final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
    private ArrayList<String> friendslist;
    private EditText email_search;
    private String searchstr;
    private ChildEventListener newfriendsListener;
    ArrayAdapter<String> friendlistadapter;
    private Context thiscontext;
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
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

        friendlistadapter = new ChooseFriendsArrayAdapter(thiscontext,
                R.layout.friend_list_item, friendslistemail);
        userlist.setAdapter(friendlistadapter);

        Button search = (Button) thisView.findViewById(R.id.add_friend_button);
        email_search = (EditText) thisView.findViewById(R.id.add_friend_searchbox);
        search.setOnClickListener(ButtonHandler);
        newfriendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null ){
                        friendslist.add(dataSnapshot.getKey());
                        friendslistemail.add((String)dataSnapshot.getValue());
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

        getFriendsFromDB();
        return thisView;
    }


    @Override
    public void onPause(){
        super.onPause();
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").removeEventListener(newfriendsListener);
    }

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

    private void getFriendsFromDB(){
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").addChildEventListener(newfriendsListener);
    }

    private void addUserAsFriend(){
        searchstr= email_search.getText().toString();
        mFirebaseRef.child("users").orderByChild("email").equalTo(searchstr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot querySnapshot) {
                        if (querySnapshot.getChildrenCount() == 0) {
                            Toast.makeText(getActivity(), "The User Does not exist", Toast.LENGTH_SHORT).show();

                        } else {
                            for (DataSnapshot child : querySnapshot.getChildren()) {
                                if (friendslist.isEmpty()) {

                                    mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").child(child.getKey())
                                            .setValue(searchstr,
                                                    new Firebase.CompletionListener() {
                                                        @Override
                                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                            if (firebaseError != null) {
                                                                Log.e(TAG, firebaseError.getMessage().toString());
                                                            } else {
                                                                Toast.makeText(getActivity(), "The User was added to your friend list", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                } else {
                                    if (!checkIfFriendExists(child.getKey())){
                                    mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("friends").child(child.getKey())
                                            .setValue(searchstr,
                                                    new Firebase.CompletionListener() {
                                                        @Override
                                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                            if (firebaseError != null) {
                                                                Log.e(TAG, firebaseError.getMessage().toString());
                                                            } else {
                                                                Toast.makeText(getActivity(), "The User was added to your friend list", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "The User is already your friend", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });
    }

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
        // TODO: Update argument type and name
        public void ChooseFriendFragmentInteraction(String id);
    }

}
