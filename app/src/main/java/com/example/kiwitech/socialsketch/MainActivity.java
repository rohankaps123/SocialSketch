package com.example.kiwitech.socialsketch;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.SSRoom;
import com.example.kiwitech.socialsketch.canvas.CanvasFragment;
import com.example.kiwitech.socialsketch.canvas.CanvasView;
import com.example.kiwitech.socialsketch.tools_pane.ToolsPaneFragment;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import afzkl.development.colorpickerview.dialog.ColorPickerDialogFragment;


/**
 * Main Activity
 * Handles communications between fragments
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class MainActivity extends Activity implements ToolsPaneFragment.OnButtonSelectedListener,
        ColorPickerDialogFragment.ColorPickerDialogListener,
        ChooseFriendFragment.ChooseFriendFragmentListener,
        ChooseRoomFragment.ChooseRoomFragmentListener {
    // Handler for threads running on the main activity
    private Handler h;
    private static final String TAG = MainActivity.class.getSimpleName();
    //keeps track of of the login fragment
    private LoginFragment login = new LoginFragment();
    private Firebase mFirebaseRef;
    private static ArrayList<String> roomMembers = new ArrayList<String>();
    //Current state of the activity
    private static String state;
    private static String thisUserID = "";
    private static String thisRoomID;
    private static String thisRoomName;

    private static Boolean isLocal = true;

    public static String getThisRoomID() {
        return thisRoomID;
    }

    public static String getThisRoomName() {
        return thisRoomName;
    }

    public static ArrayList<String> getRoomMembers() {
        return roomMembers;
    }

    public static void addToRoomMembers(String userID) {
        MainActivity.roomMembers.add(userID);
    }

    public static Boolean getIsLocal() {
        return isLocal;
    }

    public static void setIsLocal(Boolean isLocal) {
        MainActivity.isLocal = isLocal;
    }


    // on create displays the main activity xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        h = new Handler(MainActivity.this.getMainLooper());
        mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
        MainActivity.setState("login");
        getFragmentManager().beginTransaction().replace(R.id.main_window,login , "Login").commit();
        Button addFriend = (Button) findViewById(R.id.choose_friends_button);
        addFriend.setOnClickListener(ButtonHandler);
    }


    public static void setThisUserID(String ID){
        thisUserID = ID;
    }

    public static String getThisUserID(){
        return thisUserID;
    }

    //Set State of the Application
    public static void setState(String cstate){
        state = cstate;
    }

    //Get State of the Application
    public static String  getState(){
        return state;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * On click listener for the Buttons
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.choose_friends_button:
                    setupaddFriendSelector();
                    break;
            }
        }
    };

    private void setupaddFriendSelector() {
        ChooseFriendFragment nfadd = new ChooseFriendFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_window, nfadd, "Choose friends").addToBackStack("Main activity").commit();
    }

    // Creates the options menu
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Deletes the temporary files on resume after sharing and maintains the state of the application
     */
    @Override
    public void onResume(){
        super.onResume();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SocialSketch/temp");
        if(myDir.exists()){
            for (File child : myDir.listFiles()){
                child.delete();
            }
            myDir.delete();
        }
        if(state.equals("login")){
            if(login == null){
                login = new LoginFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
        }
        else if(state.equals("createnew")){
            if(login == null){
                login = new LoginFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
            CreateNewUserFragment create = (CreateNewUserFragment) getFragmentManager().findFragmentById(R.layout.fragment_create_new_user);
            if( create == null){
                create = new CreateNewUserFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.login_window,create , "Create New user").commit();
        }
        else if(state.equals("chooseRoom")){
            if(login == null){
                login = new LoginFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
            ChooseRoomFragment roomchooser = (ChooseRoomFragment) getFragmentManager().findFragmentById(R.layout.fragment_choose_room);
            if( roomchooser == null){
                roomchooser = new ChooseRoomFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.login_window,roomchooser, "Choose Room").commit();
        }
        else
        getActionBar().show();
        getActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        thisRoomName = "";
        thisRoomID = "";
        roomMembers.clear();
    }
    /**
     * On Options menu item selected
     * @param item do something if this item is selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            //logout user when ever logout is selected and bring up the login fragment
            login.logout();
            CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
            CanvasView cview = (CanvasView) canvasF.getView();
            cview.clearCanvas();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            if(state.equals("canvas")) {
                mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(false);
            }
            thisRoomName = "";
            thisRoomID = "";
            roomMembers.clear();
            getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
            return true;
        }
        if (id == R.id.action_leave_room) {
            //logout user when ever logout is selected and bring up the login fragment
            setState("chooseRoom");
            ChooseRoomFragment roomchooser = (ChooseRoomFragment) getFragmentManager().findFragmentById(R.layout.fragment_choose_room);
            if( roomchooser == null){
                roomchooser = new ChooseRoomFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, roomchooser, "Choose Room").commit();
            mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(false);
            thisRoomName = "";
            thisRoomID = "";
            roomMembers.clear();
            return true;
        }

        if (id == android.R.id.home){
        onBackPressed();
        return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * listener interface implementation to communicate from toolFragment to canvas fragment
     * @param what_button  Int referencing the selected button
     */
    public void OnButtonSelected(int what_button){
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        CanvasView cview = (CanvasView) canvasF.getView();
        switch (what_button) {
            case 1:
                //Get Current color and send it to the activity so that a new color pick dialog can start
                int cColor = cview.getCurrentPathColor();
                canvasF.onColorChangeSelected(cColor);
                break;
            case 2:
                cview.setBrush();
                canvasF.setupSliderDialog(cview.getBrush_size(), 2);
                break;
            case 3:
                cview.setEraser();
                canvasF.setupSliderDialog(cview.getEraser_size(), 3);
                break;
            case 4:
                cview.clearCanvas();
                break;
            case 5:
                cview.pathUndo();
                break;
            case 6:
                cview.pathRedo();
                break;
            case 7:
                canvasF.SaveImage(cview.getCanvas_bitmap(), h);
                break;
            case 8:
                canvasF.shareImage(cview.getCanvas_bitmap());
        }
    }



    /**
     * When this activity is called back after login from google servers
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == login.RC_GOOGLE_LOGIN) {
            login.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Send data to Canvas Fragment. Listener for Color Picker Dialog.
    @Override
    public void onColorSelected(int dialogId, int color) {
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        canvasF.onColorSelected(dialogId, color);

    }

    //Send data to Canvas Fragment. Listener for Color Picker Dialog.
    @Override
    public void onDialogDismissed(int dialogId) {
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        canvasF.onDialogDismissed(dialogId);
    }

    @Override
    public void ChooseFriendFragmentInteraction(String action,final String userID, final String userEmail) {
        if(action.equals("remove")){
            mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(userID).setValue(null);
            roomMembers.remove(userID);
        }
        else{
        mFirebaseRef.child("users").child(userID).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals(true)) {
                    mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(userID).setValue(true);
                }
                else{
                    mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(userID).setValue(false);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        addToRoomMembers(userID);
        }
    }



    @Override
    public void ChooseRoomFragmentInteraction(String roomID, String roomName) {
        setIsLocal(false);
        MainActivity.thisRoomID = roomID;
        MainActivity.thisRoomName = roomName;
        getRoomMembersFromDB();
        CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
        canvasF.addNewSegmentListener();
    }

    private void getRoomMembersFromDB() {
        mFirebaseRef.child("members").child(thisRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        addToRoomMembers(child.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
