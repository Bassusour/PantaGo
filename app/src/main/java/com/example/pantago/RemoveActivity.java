package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.Window;
;

public class RemoveActivity extends AppCompatActivity {

    String TAG = "pandaGo";
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_remove);
        Button remove = (Button) findViewById(R.id.removeButton);

        Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        String key = intent.getStringExtra("pantKey");

        DatabaseReference pantRef = databaseReference.child("pants").child(key);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pantRef.removeValue();
                setResult(Activity.RESULT_OK, getIntent());
                finish();
            }
        });


    }



}