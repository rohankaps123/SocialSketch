package com.example.kiwitech.socialsketch;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {


    public LoginFragment() {
        // Required empty public constructor
    }

    Fragment thisFragment = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.fragment_login, container, false);
        Button login = (Button) thisView.findViewById(R.id.login_button);
        Button new_account = (Button) thisView.findViewById(R.id.create_account_button);
        Button login_google = (Button) thisView.findViewById(R.id.login_google);
        login.setOnClickListener(ButtonHandler);
        new_account.setOnClickListener(ButtonHandler);
        login_google.setOnClickListener(ButtonHandler);
        return thisView;
    }

    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_button:
                    loginUser();
                    break;
                case R.id.create_account_button:
                    CreateNewUserFragment create = new CreateNewUserFragment();
                    getFragmentManager().beginTransaction().replace(R.id.login_window,create , "Create New user").commit();
                    break;
                case R.id.login_google:
                    loginGoogle();
                    break;
            }
        }
    };

    private void loginUser(){
        View thisView = thisFragment.getView();
        EditText email = (EditText) thisView.findViewById(R.id.login_email_id);
        EditText password = (EditText) thisView.findViewById(R.id.login_password);

        Firebase ref = new Firebase("https://socialsketch.firebaseio.com");
        ref.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
                Toast.makeText(getActivity(), "There was an error verifying your credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginGoogle(){
        Firebase ref = new Firebase("https://socialsketch.firebaseio.com");
        ref.authWithOAuthToken("google", "<OAuth Token>", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
            Toast.makeText(getActivity(), "There was an error verifying your credentials" + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
