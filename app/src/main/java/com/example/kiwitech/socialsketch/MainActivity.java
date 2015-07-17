package com.example.kiwitech.socialsketch;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.pushbots.push.Pushbots;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

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
    private static ArrayList<String> roomMembersNames = new ArrayList<>();

    private static String projectNumber = "133370271597";
    // Handler for threads running on the main activity
    private Handler h;
    //Tag for writing errors
    private static final String TAG = MainActivity.class.getSimpleName();
    //keeps track of of the login fragment
    private LoginFragment login = new LoginFragment();
    //firebase refernce
    private static Firebase mFirebaseRef;
    /**
     * Stores the list of all members in the room
     */
    private static ArrayList<String> roomMembers = new ArrayList<String>();
    /**
     *Current fragment of the activity
     */
    private static String state;
    /**
     * Current User's ID
     */
    private static String thisUserID = "";

    private static String thisUserName = "";
    /**
     * Current Rooms's ID
     */
    private static String thisRoomID;
    /**
     * Current Room's Name
     */
    private static String thisRoomName;
    /**
     * context for the main activity
     */
    private Context thisContext = this;
    /**
     * Boolean to tell whether the user wants to use app locally
     */
    private ChatFragment nchat;
    private ChooseFriendFragment nfadd;

    /**
     * Get the current room ID
     * @return String
     */
    public static String getThisRoomID() {
        return thisRoomID;
    }

    /**
     * Get the current room's name
     * @return String
     */
    public static String getThisRoomName() {
        return thisRoomName;
    }

    /**
     * Get the current Room's members
     * @return ArrayList of members
     */
    public static ArrayList<String> getRoomMembers() {
        return roomMembers;
    }

    public static ArrayList<String> getRoomMembersName(){
        return  roomMembersNames;
    }

    /**
     * Add a User to the current room
     * @param userID The User's ID
     */
    public static void addToRoomMembers(String userID) {
        MainActivity.roomMembers.add(userID);
    }
    public static void addToRoomMembersNames(String name){
        MainActivity.roomMembersNames.add(name);
    }
    /**
     * Sets the current User's ID
     * @param ID UserID
     */
    public static void setThisUserID(String ID){
        thisUserID = ID;
    }

    /**
     * gets the current User's ID
     * @return ID UserID
     */
    public static String getThisUserID(){
        return thisUserID;
    }

    /**
     * Set State of the Application
     */
    public static void setState(String cstate){
        state = cstate;
    }

    /**
     *Get State of the Application
     */
    public static String  getState(){
        return state;
    }

    public static String getThisUserName() {
        return thisUserName;
    }

    public static void setThisUserName(String thisUserName) {
        MainActivity.thisUserName = thisUserName;
    }

    // on create displays the main activity xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle("  Social Sketch");
        h = new Handler(MainActivity.this.getMainLooper());
        mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
        Pushbots.sharedInstance().init(this);
        MainActivity.setState("login");
        Button addFriend = (Button) findViewById(R.id.choose_friends_button);
        Button messaging = (Button) findViewById(R.id.chat_room_button);
        messaging.setOnClickListener(ButtonHandler);
        addFriend.setOnClickListener(ButtonHandler);
    }


    // What to do when the back button is pressed
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            if(getState().equals("friends")){
                MainActivity.setState("canvas");
                getActionBar().setDisplayHomeAsUpEnabled(false);
                invalidateOptionsMenu();
            }else if(getState().equals("chat")){
                MainActivity.setState("canvas");
                getActionBar().setDisplayHomeAsUpEnabled(false);
                invalidateOptionsMenu();
            }
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
                case R.id.chat_room_button:
                    setupChatRoom();
                    break;
                case R.id.choose_friends_button:
                    setupaddFriendSelector();
                    break;
            }
        }
    };

    /**
     * Setup the Add Friend fragment and switch to it
     */
    private void setupaddFriendSelector() {
        nfadd = new ChooseFriendFragment();
        setState("friends");
        invalidateOptionsMenu();
        getFragmentManager().beginTransaction().replace(R.id.main_window, nfadd, "Choose friends").addToBackStack("Main activity").commit();
    }

    /**
     * Setup the Add Friend fragment and switch to it
     */
    private void setupChatRoom() {
         nchat = new ChatFragment();
        setState("chat");
        invalidateOptionsMenu();
        getFragmentManager().beginTransaction().replace(R.id.main_window, nchat, "chat").addToBackStack("Main activity").commit();
    }


    // Creates the options menu
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
            if(getState().equals("login")||getState().equals("createnew")){
                MenuItem settings = menu.findItem(R.id.action_settings);
                MenuItem addBackground = menu.findItem(R.id.add_background_option);
                MenuItem logout = menu.findItem(R.id.action_logout);
                MenuItem leaveRoom = menu.findItem(R.id.action_leave_room);
                addBackground.setVisible(false);
                settings.setVisible(false);
                logout.setVisible(false);
                leaveRoom.setVisible(false);
            }else if(getState().equals("chooseRoom")){
                MenuItem addBackground = menu.findItem(R.id.add_background_option);
                MenuItem settings = menu.findItem(R.id.action_settings);
                MenuItem logout = menu.findItem(R.id.action_logout);
                MenuItem leaveRoom = menu.findItem(R.id.action_leave_room);
                settings.setVisible(true);
                logout.setVisible(true);
                leaveRoom.setVisible(false);
                addBackground.setVisible(false);

            }else if(getState().equals("chat") || getState().equals("friends")){
                MenuItem settings = menu.findItem(R.id.action_settings);
                MenuItem logout = menu.findItem(R.id.action_logout);
                MenuItem leaveRoom = menu.findItem(R.id.action_leave_room);
                MenuItem addBackground = menu.findItem(R.id.add_background_option);
                addBackground.setVisible(false);
                settings.setVisible(true);
                logout.setVisible(true);
                leaveRoom.setVisible(true);
            }
            else{
                MenuItem settings = menu.findItem(R.id.action_settings);
                MenuItem logout = menu.findItem(R.id.action_logout);
                MenuItem leaveRoom = menu.findItem(R.id.action_leave_room);
                MenuItem addBackground = menu.findItem(R.id.add_background_option);
                addBackground.setVisible(true);
                settings.setVisible(true);
                logout.setVisible(true);
                leaveRoom.setVisible(true);
            }
        return true;
    }

    /**
     * Deletes the temporary files on resume after sharing and maintains the state of the application
     */
    @Override
    public void onResume(){
        super.onResume();
        invalidateOptionsMenu();
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
        else if(state.equals("chat")){
            if(nchat == null){
                nchat = new ChatFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, nchat, "chat").commit();
        }
        else if(state.equals("friends")){
            if(nfadd == null){
                nfadd = new ChooseFriendFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, nfadd, "Choose friends").commit();
        }
        else if(state.equals("localcanvas")){
            setLocalCanvas();
            getActionBar().show();
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else {
            setSharedCanvas();
            getActionBar().show();
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Things to Do when the activity is destroyed
     */
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
     * @return true or false
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
            if(state.equals("localcanvas")){
                login.logout();
                CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                CanvasView cview = (CanvasView) canvasF.getView();
                cview.clearCanvas();
                Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
            }
            else{
            if(getState().equals("chat")|| getState().equals("friends")){
                onBackPressed();
                setState("canvas");
            }
            //Set the user as offline in the canvas when logging out
            if ((getState().equals("canvas")) && mFirebaseRef!=null) {
                mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(false);
            }
            //logout user when ever logout is selected and bring up the login fragment
            login.logout();
            CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                canvasF.removeNewSegmentListener();
                CanvasView cview = (CanvasView) canvasF.getView();
            cview.clearCanvas();
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();

            thisRoomName = "";
            thisRoomID = "";
            roomMembers.clear();
            roomMembersNames.clear();
            getFragmentManager().beginTransaction().replace(R.id.main_window, login, "Login").commit();
            }
            return true;
        }
        if (id == R.id.action_leave_room) {
            if(state.equals("localcanvas")){
                setState("chooseRoom");
                invalidateOptionsMenu();
                CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                CanvasView cview = (CanvasView) canvasF.getView();
                cview.clearCanvas();
                ChooseRoomFragment roomchooser = (ChooseRoomFragment) getFragmentManager().findFragmentById(R.layout.fragment_choose_room);
                if( roomchooser == null){
                    roomchooser = new ChooseRoomFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.main_window, roomchooser, "Choose Room").commit();
            }
            else {
            if(getState().equals("chat") || getState().equals("friends")){
                onBackPressed();
                setState("canvas");
            }
            //Set the user as offline in the canvas when leaving the room
            if(getState().equals("canvas")){
                mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(false);
            }
            //logout user when ever logout is selected and bring up the login fragment
                setState("chooseRoom");
                invalidateOptionsMenu();
                CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
            CanvasView cview = (CanvasView) canvasF.getView();
            cview.clearCanvas();
            canvasF.removeNewSegmentListener();
            ChooseRoomFragment roomchooser = (ChooseRoomFragment) getFragmentManager().findFragmentById(R.layout.fragment_choose_room);
            if( roomchooser == null){
                roomchooser = new ChooseRoomFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.main_window, roomchooser, "Choose Room").commit();

            thisRoomName = "";
            thisRoomID = "";
            roomMembers.clear();
            roomMembersNames.clear();
            }
            return true;

        }
        if (id == R.id.add_background_option){
            selectImage();
            return true;
        }
        if (id == android.R.id.home){
        onBackPressed();
        return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void selectImage() {
        if(getState().equals("localcanvas")){
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Add Background!");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Take Photo")) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/SocialSketch/Background");
                        myDir.mkdirs();
                        File f = new File(myDir, "temp.jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(intent, 2);
                    } else if (options[item].equals("Choose from Gallery")) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 3);

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }else{
        mFirebaseRef.child("rooms").child(MainActivity.getThisRoomID()).child("createdBY")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue().toString().equals(MainActivity
                                .getThisUserID())) {
                            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Add Background!");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (options[item].equals("Take Photo")) {
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        String root = Environment.getExternalStorageDirectory().toString();
                                        File myDir = new File(root + "/SocialSketch/Background");
                                        myDir.mkdirs();
                                        File f = new File(myDir, "temp.jpg");
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                                        startActivityForResult(intent, 2);
                                    } else if (options[item].equals("Choose from Gallery")) {
                                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(intent, 3);

                                    } else if (options[item].equals("Cancel")) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                            builder.show();
                        } else {
                            Toast.makeText(thisContext,"Only creator can change the Background", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
        }


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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == login.RC_GOOGLE_LOGIN) {
                login.onActivityResult(requestCode, resultCode, data);
                }
            else if (requestCode == 2) {

                File f = new File(Environment.getExternalStorageDirectory().toString()+"/SocialSketch/Background/temp.jpg");
                try {
                    FileInputStream streamIn = new FileInputStream(f);
                    final Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
                    streamIn.close();
                    CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                    final CanvasView cview = (CanvasView) canvasF.getView();
                    final CharSequence[] options = {"Fit", "Stretch"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose Image Mode");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Fit")) {
                                cview.setBitmap(bitmap, "Fit");

                            } else if (options[item].equals("Stretch")) {
                                cview.setBitmap(bitmap, "Stretch");
                            }
                        }
                    });
                    builder.show();
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "Image-"+ n +".jpg" ;
                    File fnew = new File(Environment.getExternalStorageDirectory().toString()+"/SocialSketch/Background/"+fname);
                    f.renameTo(fnew);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 3) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                FileInputStream streamIn = null;
                try {
                    streamIn = new FileInputStream(picturePath);
                    final Bitmap bitmapgal = BitmapFactory.decodeStream(streamIn);
                    streamIn.close();
                    CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
                    final CanvasView cview = (CanvasView) canvasF.getView();
                    final CharSequence[] options = {"Fit", "Stretch"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose Image Mode");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Fit")) {
                                cview.setBitmap(bitmapgal, "Fit");

                            } else if (options[item].equals("Stretch")) {
                                cview.setBitmap(bitmapgal, "Stretch");

                            }
                        }
                    });
                    builder.show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * Callback from choose friend Fragment.
     * @param action Tells us whether to add or remove the member
     * @param userID Tells us the ID of the user to perform the action on
     * @param userEmail tells us the Email id of the user to perform the action on,
     */
    @Override
    public void ChooseFriendFragmentInteraction(String action,final String userID, final String userEmail) {
        if(action.equals("remove")){
            //remove the user as a member in the room
            mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(userID).setValue(null);
            roomMembers.remove(userID);
        }
        else{
            //Add the user as a member in the room
            mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(userID).setValue(false);
            addToRoomMembers(userID);
            addToRoomMembersNames(userEmail);
        }
    }


    /**
     * Callback from the choose room fragment.
     * @param roomID The ID of the room selected
     * @param roomName The name of the room selected
     * @param local Whether the user wants to use the application locally or not
     */
    @Override
    public void ChooseRoomFragmentInteraction(String roomID, String roomName,Boolean local) {
        if(local)
            MainActivity.setState("localcanvas");
        else
        MainActivity.setState("canvas");

        //If local set the room id and name as "" and setup the local canvas
        if (local){
            setLocalCanvas();
            MainActivity.thisRoomID = roomID;
            MainActivity.thisRoomName = roomName;
        }
        //If local set the room id and name and setup the shared canvas
        else{
            MainActivity.thisRoomID = roomID;
            MainActivity.thisRoomName = roomName;
            setSharedCanvas();
            CanvasFragment canvasF = (CanvasFragment) getFragmentManager().findFragmentById(R.id.Canvas_Fragment);
            //add the listeners for friend status and new segments from the database
            canvasF.addNewSegmentListener();
            getRoomMembersFromDB(roomName);
        }
    }

    /**
     * Sets up the canvas for a local user
     */
    public void setLocalCanvas(){
        Button chooseFriends = (Button) this.findViewById(R.id.choose_friends_button);
        chooseFriends.setVisibility(View.GONE);
        Button message = (Button) this.findViewById(R.id.chat_room_button);
        message.setVisibility(View.GONE);
        ToolsPaneFragment ntools = (ToolsPaneFragment) getFragmentManager().findFragmentById(R.id.tools);
        Button clear = (Button) ntools.getView().findViewById(R.id.clear_button);
        clear.setVisibility(View.VISIBLE);
        Button undo = (Button) ntools.getView().findViewById(R.id.undo_button);
        undo.setVisibility(View.VISIBLE);
        Button redo = (Button) ntools.getView().findViewById(R.id.redo_button);
        redo.setVisibility(View.VISIBLE);
    }

    /**
     * Sets up the canvas for a remote User
     */
    public void setSharedCanvas() {
        Button chooseFriends = (Button) this.findViewById(R.id.choose_friends_button);
        chooseFriends.setVisibility(View.VISIBLE);
        Button message = (Button) this.findViewById(R.id.chat_room_button);
        message.setVisibility(View.VISIBLE);
        ToolsPaneFragment ntools = (ToolsPaneFragment) getFragmentManager().findFragmentById(R.id.tools);
        Button clear = (Button) ntools.getView().findViewById(R.id.clear_button);
        clear.setVisibility(View.GONE);
        Button undo = (Button) ntools.getView().findViewById(R.id.undo_button);
        undo.setVisibility(View.GONE);
        Button redo = (Button) ntools.getView().findViewById(R.id.redo_button);
        redo.setVisibility(View.GONE);
    }

    /**
     * Removes the chooseRoomFragment from view after room selection
     */
    private void removeChooseRoomFragment(){
        ChooseRoomFragment chooseRoom = (ChooseRoomFragment) getFragmentManager().findFragmentByTag("Choose Room");
        getFragmentManager().beginTransaction().remove(chooseRoom).commit();
    }

    /**
     * Get the members of the room from the database, store in a list locally and make a
     * shared canvas if the current user is in the list of members
     * @param roomName Name of the room for which to get the members
     */
    private void getRoomMembersFromDB(final String roomName) {
        mFirebaseRef.child("members").child(thisRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    //Add the changed user to the list if room members
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        final String key = child.getKey();
                        addToRoomMembers(key);
                        mFirebaseRef.child("users").child(key).child("email")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    addToRoomMembersNames(dataSnapshot.getValue().toString());
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                    }
                    //if the room members list contains the current user make a shared canvas else give message
                    if (MainActivity.getRoomMembers().contains(MainActivity.getThisUserID())) {
                        Toast.makeText(thisContext, "Successfully selected " + roomName, Toast.LENGTH_SHORT).show();
                        mFirebaseRef.child("members").child(MainActivity.getThisRoomID()).child(MainActivity.getThisUserID()).setValue(true);
                        removeChooseRoomFragment();
                    }
                    else{
                        Toast.makeText(thisContext, " Not a member of " + roomName, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
