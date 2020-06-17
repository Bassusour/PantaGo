package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private TextView textViewUser;
    private ImageView image;
    private LoginButton fbLogin;
    private Button loginBtn;
    private Button logOutBtn;
    private EditText emailInput;
    private EditText passwordInput;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG = "FacebookAuthentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        textViewUser = findViewById(R.id.text_user);
        image = findViewById(R.id.imageView);
        fbLogin = findViewById(R.id.fbLogin);
        fbLogin.setPermissions("email", "public_profile");
        loginBtn = findViewById(R.id.loginButton);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        logOutBtn = findViewById(R.id.logoutBtn);

        //Callbackmanager registers the state of the login
        mCallbackManager = CallbackManager.Factory.create();
        fbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
                //Skifte activity
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError " + error);
            }
        });

        //Listener which checks when authenticator changes (login or logout)
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    updateUI(user);
                } else {
                    updateUI(null);
                }
            }
        };

        //Checks if signed in or out
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    mFirebaseAuth.signOut();
                }
            }
        };



        Button buttonCreateUser = findViewById(R.id.buttonCreateNewUser);
        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateUserActivity.class);
                startActivity(intent);
            }
        });

        //Login with email and password
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if(email.length() <= 0 || password.length() <= 0) {
                    Toast.makeText(LoginActivity.this, "Indtast et input",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mFirebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                    updateUI(user);
                                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                    // ...
                                }

                                // ...
                            }
                        });
            }
        });

        //Logout
        logOutBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Gives callbackmanager data
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    //Checks validity for facebook credentials
    private void handleFacebookToken(AccessToken token) {
        Log.d(TAG, "handleFacebookToken"+token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"sign in with credential successful");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    Log.d(TAG,"sign in with credential failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            textViewUser.setText(user.getDisplayName());
            if(user.getPhotoUrl() != null){
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl = photoUrl + "?type=large";
                Picasso.get().load(photoUrl).into(image);
            }
        }
        else {
            textViewUser.setText("");
            image.setImageResource(R.drawable.panda_logo1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Adds listener to firebaseAuth
        mFirebaseAuth.addAuthStateListener(authStateListener);
        //Gets and updates UI with current user
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(authStateListener != null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}