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
import android.content.res.Configuration;
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
    private EditText emailInput;
    private EditText passwordInput;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG = "FacebookAuthentication";
    private static final String KEY_EMAIL = "email_key";
    private static final String KEY_PASSWORD = "password_key";
    private boolean orientation = false;

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

        //Saved state
        if(savedInstanceState != null){
            emailInput.setText(savedInstanceState.getString(KEY_EMAIL));
            passwordInput.setText(savedInstanceState.getString(KEY_PASSWORD));
        }

        if(mFirebaseAuth.getCurrentUser()!=null && orientation == false){
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            String userEmail = user.getEmail().toString();
            Toast.makeText(LoginActivity.this, "Welcome back " + userEmail,
                    Toast.LENGTH_LONG).show();
            updateUI(user);
            //textViewUser.setText("logged in as2: " + userEmail.toString());
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        }



        //Callbackmanager registers the state of the login
        mCallbackManager = CallbackManager.Factory.create();
        fbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
                //Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                //startActivity(intent);
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
                                    LoginManager.getInstance().logOut();
                                    updateUI(user);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    // updateUI(null);
                                    // ...
                                }

                                // ...
                            }
                        });
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
                    emailInput.setText("");
                    passwordInput.setText("");
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG,"sign in with credential failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    //updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            textViewUser.setText("logged in as: " + user.getEmail().toString());
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
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstansteState) {
        super.onSaveInstanceState(savedInstansteState);
        savedInstansteState.putString(KEY_EMAIL, emailInput.getText().toString());
        savedInstansteState.putString(KEY_PASSWORD, passwordInput.getText().toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = true;
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            orientation = true;
        }
    }
}