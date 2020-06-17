package com.example.pantago;

import android.app.AppComponentFactory;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.internal.WebDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateUserActivity extends AppCompatActivity {

    EditText email,password,password2;
    Button buttonCreateUser;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createuser);

        email = findViewById(R.id.editTextAddEmailAddress);
        password = findViewById(R.id.editTextAddPassword1);
        password2 = findViewById(R.id.editTextAddPassword2);
        buttonCreateUser = findViewById(R.id.buttonCreateUser);
        progressBar = findViewById(R.id.progressBarCreateUser);
        firebaseAuth = FirebaseAuth.getInstance();

        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                String sEmail = email.getText().toString();
                String sPassword = password.getText().toString();
                String sPassword2 = password2.getText().toString();
                if(sEmail.isEmpty()){
                    email.setError("Email required");
                    return;
                }
                if(!sEmail.trim().matches(emailPattern)){
                    email.setError("Invalid email address");
                    return;
                }
                if(sPassword.isEmpty()){
                    password.setError("Password required");
                    return;
                }
                if(sPassword2.isEmpty()){
                    password2.setError("Password required");
                    return;
                }
                if(sPassword.length()<6){
                    password.setError("Password must be longer");
                    return;
                }
                if(!sPassword.equals(sPassword2)){
                    password.setError("Password does not match");
                    password2.setError("Password does not match");
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(CreateUserActivity.this, "User creation successful.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Toast.makeText(CreateUserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }

                });
                progressBar.setVisibility(View.INVISIBLE);


            }
        });




    }

    private void updateUI(FirebaseUser user) {
        if(null != user){
            finish();
        }
    }
}