package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import com.example.pantago.Pant;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;;import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.Window;
import android.widget.Button;;

public class ClaimActivity extends AppCompatActivity {

    String TAG = "pandaGo";
    public static boolean claimed;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
        Button tilbage = findViewById(R.id.buttonTilbage);


        Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();

        String currentUser = firebaseAuth.getCurrentUser().getUid();


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        String key = intent.getStringExtra("pantKey");

        DatabaseReference pantRef = databaseReference.child("pants").child(key);
        pantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pant pant = dataSnapshot.getValue(Pant.class);
                String claimerUID = pant.getClaimerUID();
                if(currentUser.equals(claimerUID)){
                    claim.setText("UNCLAIM");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


       claim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitudeUser = intent.getDoubleExtra("latitudeUser", 0);
                double longitudeUser = intent.getDoubleExtra("longitudeUser", 0);
                double latitudeMarker = intent.getDoubleExtra("latitudeMarker", 0);
                double longitudeMarker = intent.getDoubleExtra("longitudeMarker", 0);
                Log.i(TAG, String.valueOf(latitudeMarker));
                Log.i(TAG, String.valueOf(latitudeUser));

                if(claim.getText().equals("UNCLAIM")){
                    pantRef.child("claimerUID").setValue("");
                    setResult(Activity.RESULT_OK);
                    finish();
                }

                if(claim.getText().equals("CLAIM")){
                    if (Math.pow(latitudeMarker-latitudeUser,2) + Math.pow(longitudeMarker-longitudeUser,2) <= 0.000001 ) {
                        pantRef.child("claimerUID").setValue(currentUser);
                        setResult(Activity.RESULT_OK, getIntent());
                        finish();
                    }
                }

            }
        });

        tilbage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



}