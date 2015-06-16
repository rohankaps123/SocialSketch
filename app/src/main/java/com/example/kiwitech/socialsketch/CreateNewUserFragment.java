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
 * A simple {@link Fragment} subclass.
 */
public class CreateNewUserFragment extends Fragment {


    public CreateNewUserFragment() {
        // Required empty public constructor
    }

    Fragment thisFragment = this;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.fragment_create_new_user, container, false);
        Button create_new = (Button) thisView.findViewById(R.id.create_account_new_button);
        create_new.setOnClickListener(ButtonHandler);
        return thisView;
    }

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

    private void createNewUser(){
        View thisView = thisFragment.getView();
        EditText email = (EditText) thisView.findViewById(R.id.create_account_email);
        EditText password = (EditText) thisView.findViewById(R.id.create_account_password);

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
