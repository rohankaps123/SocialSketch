package com.example.kiwitech.socialsketch;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kiwitech.socialsketch.DataTypes.SSUser;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Fragment
 * Manages the login using firebase
 *
 * @author Rohan Kapoor
 * @since 1.0
 */
public class LoginFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //Get Tag to be used in the error codes
    private static final String TAG = LoginFragment.class.getSimpleName();

    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;

    /* A reference to the Firebase */
    private Firebase mFirebaseRef;

    /* Data from the authenticated user */
    private AuthData mAuthData;

    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean mGoogleLoginClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
     * sign-in. */
    private ConnectionResult mGoogleConnectionResult;

    /**
     * Result reference to the fragment callback
     */
    public static final int RESULT_OK = -1;


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Reference to the login fragment
     */
    private Fragment thisFragment = this;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getActionBar().hide();
        // Inflate the layout for this fragment
        View thisView = inflater.inflate(R.layout.fragment_login, container, false);
        mFirebaseRef = new Firebase("https://socialsketch.firebaseio.com");
        Button login = (Button) thisView.findViewById(R.id.login_button);
        Button new_account = (Button) thisView.findViewById(R.id.create_account_button);
        SignInButton login_google = (SignInButton) thisView.findViewById(R.id.login_google);
        login.setOnClickListener(ButtonHandler);
        new_account.setOnClickListener(ButtonHandler);
        login_google.setOnClickListener(ButtonHandler);

         /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .build();

               /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(getActivity());
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating Login");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
        mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.hide();
                setAuthenticatedUser(authData);
            }
        });

        return thisView;
    }


    /**
     * On click listener for the Buttons
     */
    private View.OnClickListener ButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            switch (v.getId()) {
                case R.id.login_button:
                    //close the keyboard on click
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    //login user with email password
                    loginUser();
                    break;
                case R.id.create_account_button:
                    // create a new account. Switches to a new fragment for creating account
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    CreateNewUserFragment create = new CreateNewUserFragment();
                    getFragmentManager().beginTransaction().replace(R.id.login_window,create , "Create New user").addToBackStack("Create New user").commit();
                    MainActivity.setState("createnew");
                    break;
                case R.id.login_google:
                    // connect to google API
                    mGoogleLoginClicked = true;
                    if (!mGoogleApiClient.isConnecting()) {
                        if (mGoogleConnectionResult != null) {
                            resolveSignInError();
                        } else if (mGoogleApiClient.isConnected()) {
                            getGoogleOAuthTokenAndLogin();
                        } else {
                    /* connect API now */
                            Log.d(TAG, "Trying to connect to Google API");
                            mGoogleApiClient.connect();
                        }
                    }
                    break;
            }
        }
    };

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    public void logout() {
        if (this.mAuthData != null) {
            mFirebaseRef.unauth();
            if (this.mAuthData.getProvider().equals("google")) {
                /* Logout from Google+ */
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
            MainActivity.setState("login");
            setUserOffineDB();

        }
    }

    private void setUserOffineDB() {
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("online").setValue(false);
    }

    /**
     * Logins the user with email and password
     */
    private void loginUser(){
        View thisView = thisFragment.getView();
        //get email and password
        EditText email = (EditText) thisView.findViewById(R.id.login_email_id);
        EditText password = (EditText) thisView.findViewById(R.id.login_password);
        mAuthProgressDialog.show();
        //authenticate with firebase
        mFirebaseRef.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                //Set authentication data
                setAuthenticatedUser(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                mAuthProgressDialog.hide();
                // there was an error
                Toast.makeText(getActivity(), "There was an error verifying your credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Map<String, String> options = new HashMap<String, String>();
        if (requestCode == RC_GOOGLE_LOGIN) {
            /* This was a request by the Google API */
            if (resultCode != RESULT_OK) {
                mGoogleLoginClicked = false;
            }
            mGoogleIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }


    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if(authData !=null){
            mAuthData = authData;
            setUserID(authData.getProviderData().get("email").toString());
            setUserOnlineDB();
            Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
            getActivity().getActionBar().show();
            MainActivity.setState("canvas");
            getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
        }
        else{
            return;
        }
    }

    private void setUserOnlineDB() {
        Log.e(TAG, MainActivity.getThisUserID());
        mFirebaseRef.child("users").child(MainActivity.getThisUserID()).child("online").setValue(true);
    }

    private void setUserID(String email) {
        mFirebaseRef.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot querySnapshot) {
                        if(querySnapshot.getChildrenCount() != 0){
                            for(DataSnapshot child : querySnapshot.getChildren()){
                                MainActivity.setThisUserID(child.getKey());
                            }
                        }
                        else{
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });
    }


    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {
        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            AddUserIfNotExist(authData);
            mAuthProgressDialog.hide();
            Log.i(TAG, provider + " auth successful");
            setAuthenticatedUser(authData);
        }

        /**
         * Add user to the firebase if it does not already exist.
         * @param authData authentication data to be passed
         */
        private void AddUserIfNotExist(final AuthData authData) {
            final String email = authData.getProviderData().get("email").toString();
            mFirebaseRef.child("users").orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot querySnapshot) {
                            if(querySnapshot.getChildrenCount() != 0){
                            }
                            else{
                                SSUser nuser = new SSUser(authData.getProviderData().get("displayName").toString(),email,"","");
                                Firebase usersRef =  mFirebaseRef.child("users");
                                usersRef.push().setValue(nuser);
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError error) {
                        }
                    });
        }


        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.hide();
            showErrorDialog(firebaseError.toString());
        }
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(getActivity(), RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        mAuthProgressDialog.show();
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(getActivity(), Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    mFirebaseRef.authWithOAuthToken("google", token, new AuthResultHandler("google"));
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                    showErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
        getGoogleOAuthTokenAndLogin();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, result.toString());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }
}
