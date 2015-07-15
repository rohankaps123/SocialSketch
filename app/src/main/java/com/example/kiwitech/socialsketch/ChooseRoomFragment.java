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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.ChooseFriendsArrayAdapter;
import com.example.kiwitech.socialsketch.DataTypes.ChooseRoomArrayAdapter;
import com.example.kiwitech.socialsketch.DataTypes.SSRoom;
import com.example.kiwitech.socialsketch.canvas.CanvasFragment;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.pushbots.push.Pushbots;

import java.util.ArrayList;
import java.util.Map;


/**
 * A fragment representing a list of rooms available and an interface for selecting the room
 * Activities containing this fragment MUST implement the {@link ChooseRoomFragmentListener}
 * interface.
 * @author Rohan Kapoor
 * @since 1.0
 */
public class ChooseRoomFragment extends Fragment {
    /**
     * Tag for logging data
     */
    private static final String TAG = ChooseRoomFragment.class.getSimpleName();
    /**
     * Listener class to be implemented in the class that use ChooseRoomFragment
     */
    private ChooseRoomFragmentListener mListener;
    /**
     * ListView that shows all the rooms
     */
    private ListView roomlist;
    /**
     * Reference to the database
     */
    final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
    /**
     * List of all the existing Rooms with IDs
     */
    private ArrayList<String> roomIDlist;

    /**
     * EditText that contains the email to search from
     */
    private EditText newRoomName;

    /**
     * Listener to listen for add child events in the database
     */
    private ChildEventListener newRoomNameListener;
    /**
     * Adapter for listView
     */
    ArrayAdapter<String> roomListAdapter;
    /**
     * context for the fragment
     */
    private Context thiscontext;
    /**
     * Arraylist for storing the names for the rooms got using userID
     */
    private ArrayList<String> roomnamelist;
    /**
     * Reference to the fragment
     */
    private Fragment thisFragment = this;

    /*
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChooseRoomFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        thiscontext = container.getContext();
        View thisView = inflater.inflate(R.layout.fragment_choose_room, container, false);
        roomlist = (ListView) thisView.findViewById(R.id.choose_room_list_view);
        roomIDlist = new ArrayList<String>();
        roomnamelist = new ArrayList<String>();
        roomListAdapter = new ChooseRoomArrayAdapter(thiscontext,
                R.layout.room_list_item, roomnamelist,roomIDlist,this);
        roomlist.setAdapter(roomListAdapter);

        Button newRoomButton = (Button) thisView.findViewById(R.id.create_room_button);
        Button localUseButton = (Button) thisView.findViewById(R.id.use_local_button);
        localUseButton.setOnClickListener(ButtonHandler);
        newRoomButton.setOnClickListener(ButtonHandler);
        newRoomName = (EditText) thisView.findViewById(R.id.choose_room_new_name);
        // Create New listener for getting data from the firebase. The reference is stored in newfriendsListener
        // So that it can be removed on pause
        newRoomNameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null ){
                    roomIDlist.add((String) dataSnapshot.getKey());
                    Map<String, Object> newRoom = (Map<String, Object>) dataSnapshot.getValue();
                    roomnamelist.add((String) newRoom.get("name"));
                    roomListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null ){
                    roomIDlist.remove((String) dataSnapshot.getKey());
                    Map<String, Object> oldRoom = (Map<String, Object>) dataSnapshot.getValue();
                    roomnamelist.remove((String) oldRoom.get("name"));
                    roomListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        //Get The most recent list of the friends from the database
        getRoomsFromDB();
        return thisView;
    }

    public void onRoomSelected(int position){
        getActivity().invalidateOptionsMenu();
        mListener.ChooseRoomFragmentInteraction(roomIDlist.get(position), roomnamelist.get(position), false);

    }
    @Override
    public void onPause(){
        super.onPause();
        //Remove the eventListener
        mFirebaseRef.child("rooms").removeEventListener(newRoomNameListener);
    }

    /**
     * Onlick listener to keep track of the add frind button
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            switch (v.getId()) {
                case R.id.create_room_button:
                    //close the keyboard on click
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    addRoom();
                    break;
                case R.id.use_local_button:
                    //close the keyboard on click
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mListener.ChooseRoomFragmentInteraction("", "", true);
                    getActivity().invalidateOptionsMenu();
                    getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
                    break;
            }
        }
    };

    /**
     * Get friends fromt he DataBase
     */
    private void getRoomsFromDB(){
        mFirebaseRef.child("rooms").addChildEventListener(newRoomNameListener);
    }

    /**
     * Add the user as a friend to the database using emai ID
     */
    private void addRoom(){
        SSRoom newRoom = new SSRoom(newRoomName.getText().toString(),MainActivity.getThisUserID());
        Firebase roomsRef = mFirebaseRef.child("rooms");
        String newRoomKey = roomsRef.push().getKey();
        roomsRef.child(newRoomKey).setValue(newRoom);
        Pushbots.sharedInstance().tag(newRoomKey);
        mFirebaseRef.child("members").child(newRoomKey).child(MainActivity.getThisUserID()).setValue(false);
        Toast.makeText(getActivity(), "Successfully created a new Room", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ChooseRoomFragmentListener) activity;
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

    public void onRemoveRoomSelected(final int position) {
        mFirebaseRef.child("rooms").child(roomIDlist.get(position)).child("createdBY").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String creator = (String) dataSnapshot.getValue();
                if (dataSnapshot != null && creator.equals(MainActivity.getThisUserID())) {
                    mFirebaseRef.child("members").child(roomIDlist.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean memberOnline = false;
                            for(DataSnapshot child : dataSnapshot.getChildren()){
                                if((Boolean)child.getValue()){
                                    memberOnline = true;
                                }
                            }
                            if (!memberOnline){
                                Pushbots.sharedInstance().untag(roomIDlist.get(position));
                                mFirebaseRef.child("rooms").child(roomIDlist.get(position)).setValue(null);
                            mFirebaseRef.child("members").child(roomIDlist.get(position)).setValue(null);
                            mFirebaseRef.child("canvas").child(roomIDlist.get(position)).setValue(null);
                                mFirebaseRef.child("messages").child(roomIDlist.get(position)).setValue(null);
                                Toast.makeText(getActivity(), "Successfully deleted the Room", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getActivity(), "Error: Members Online", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "You did not create the room", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
    public interface ChooseRoomFragmentListener {
        // TODO: Update argument type and name
        public void ChooseRoomFragmentInteraction(String roomID, String roomName, Boolean local);
    }

}
