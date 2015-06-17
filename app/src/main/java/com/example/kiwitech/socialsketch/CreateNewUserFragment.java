package com.example.kiwitech.socialsketch;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

/**
 * Create new user Fragment
 * Manages creating new user using firebase
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class CreateNewUserFragment extends Fragment {


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
        Button create_new = (Button) thisView.findViewById(R.id.create_account_new_button);
        create_new.setOnClickListener(ButtonHandler);
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
            }
        }
    };

    /**
     * Creates new user in the database
     */
    private void createNewUser(){
        View thisView = thisFragment.getView();
        //gets details from the view
        EditText name = (EditText) thisView.findViewById(R.id.create_account_name);
        EditText dob = (EditText) thisView.findViewById(R.id.create_account_dob);
        EditText email = (EditText) thisView.findViewById(R.id.create_account_email);
        EditText password = (EditText) thisView.findViewById(R.id.create_account_password);

        //If any of the Fields are undefined error
        if(name.getText().toString() =="" || dob.getText().toString() ==""
                || email.getText().toString() =="" || password.getText().toString() ==""){
            Toast.makeText(getActivity(), "One or more Fields is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // creates a firebase reference
        Firebase ref = new Firebase("https://socialsketch.firebaseio.com");
        ref.createUser(email.getText().toString(), password.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Toast.makeText(getActivity(),"Successfully created user account" , Toast.LENGTH_SHORT).show();
                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(getActivity(), "There was an error creating your account", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
