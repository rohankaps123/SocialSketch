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
import com.example.kiwitech.socialsketch.DataTypes.SSRoom;
import com.example.kiwitech.socialsketch.canvas.CanvasFragment;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import java.util.Map;


/**
 * A fragment representing a list of rooms available.
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
     * Listener class to be implemented in the class that implements ChooseFriendFragment
     */
    private ChooseRoomFragmentListener mListener;
    /**
     * ListView that shows all the friends
     */
    private ListView roomlist;
    /**
     * Reference to the database
     */
    final Firebase mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
    /**
     * List of all the existing Rooms
     */
    private ArrayList<String> roomIDlist;

    /**
     * EditText that contains the email to search from
     */
    private EditText newRoomName;

    /**
     * Listener to listen for addd child events in the database
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
     * Arraylist for storing the emails for the users got using userID
     */
    private ArrayList<String> roomnamelist;

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

        roomListAdapter = new ArrayAdapter<String>(thiscontext,
                android.R.layout.simple_list_item_1, roomnamelist);
        roomlist.setAdapter(roomListAdapter);

        roomlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);
                mListener.ChooseRoomFragmentInteraction(roomIDlist.get(position), roomnamelist.get(position));
                setMemberThisRoom();
                Toast.makeText(getActivity(), "Successfully selected " + roomnamelist.get(position), Toast.LENGTH_SHORT).show();
                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
                MainActivity.setState("canvas");
            }
        });

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

    private void setMemberThisRoom() {
        mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(true);
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
                    MainActivity.setIsLocal(true);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    MainActivity.setState("canvas");
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
        mFirebaseRef.child("members").child(newRoomKey).child(MainActivity.getThisUserID()).setValue(false);
        mFirebaseRef.child("canvas").child(newRoomKey).setValue("created");
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
        public void ChooseRoomFragmentInteraction(String roomID, String roomName);
    }

}
