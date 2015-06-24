package com.example.kiwitech.socialsketch;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.Pair;
import com.example.kiwitech.socialsketch.DataTypes.SSUser;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 * Create new user Fragment
 * Manages creating new user using firebase
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class CreateNewUserFragment extends Fragment {
    private ProgressDialog mCreateProgressDialog;

    public CreateNewUserFragment() {
        // Required empty public constructor
    }

    /**
     * Reference to the fragment
     */
    private Fragment thisFragment = this;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.fragment_create_new_user, container, false);
        getActivity().getActionBar().hide();
        Button create_new = (Button) thisView.findViewById(R.id.create_account_new_button);
        Button back = (Button) thisView.findViewById(R.id.create_account_back_button);
        create_new.setOnClickListener(ButtonHandler);
        back.setOnClickListener(ButtonHandler);
        setHasOptionsMenu(false);
        return thisView;
    }

    /**
     * Listener for the button click
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.create_account_new_button:
                    createNewUser();
                    break;
                case R.id.create_account_back_button:
                    MainActivity.setState("login");
                    getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
                    break;
            }
        }
    };

    /**
     * Creates new user in the database
     */
    private void createNewUser(){
        mCreateProgressDialog = new ProgressDialog(getActivity());
        mCreateProgressDialog.setTitle("Loading");
        mCreateProgressDialog.setMessage("Create new Account");
        mCreateProgressDialog.setCancelable(false);
        mCreateProgressDialog.show();
        View thisView = thisFragment.getView();
        //gets details from the view
        final EditText name = (EditText) thisView.findViewById(R.id.create_account_name);
        final EditText dob = (EditText) thisView.findViewById(R.id.create_account_dob);
        final EditText email = (EditText) thisView.findViewById(R.id.create_account_email);
        final EditText password = (EditText) thisView.findViewById(R.id.create_account_password);
        //If any of the Fields are undefined error
        if(name.getText().toString().equals("") || dob.getText().toString().equals("")
                || email.getText().toString().equals("") || password.getText().toString().equals("")){
            mCreateProgressDialog.hide();
            Toast.makeText(getActivity(), "One or more Fields is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // creates a firebase reference
        final Firebase ref = new Firebase("https://socialsketch.firebaseio.com");
        ref.createUser(email.getText().toString(), password.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
               // Add User to the Database Too
                SSUser nuser = new SSUser(name.getText().toString(),email.getText().toString(),
                        password.getText().toString(),dob.getText().toString());
                Firebase usersRef = ref.child("users");
                usersRef.push().setValue(nuser);
                mCreateProgressDialog.hide();
                Toast.makeText(getActivity(),"Successfully created user account" , Toast.LENGTH_SHORT).show();
                MainActivity.setState("login");
                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                mCreateProgressDialog.hide();
                MainActivity.setState("createnew");
                Toast.makeText(getActivity(), "There was an error creating your account", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
