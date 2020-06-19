package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.example.pantago.Pant;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;;import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
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
import android.widget.TextView;;

public class ClaimActivity extends AppCompatActivity {

    String TAG = "pantaGo";
    public static boolean claimed;
    FirebaseAuth firebaseAuth;
    FusedLocationProviderClient fusedLocationProviderClient;
    private ClaimActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
        TextView textViewQuantity = findViewById(R.id.textViewClaimQuantity);
        TextView textViewDescription = findViewById(R.id.textViewClaimDescription);
        Button collectButton = findViewById(R.id.buttonCollect);
        collectButton.setVisibility(View.INVISIBLE);



        Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();

        String currentUser = firebaseAuth.getCurrentUser().getUid();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

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

                textViewQuantity.setText(pant.getQuantity() + "");
                textViewDescription.setText(pant.getDescription());

                String claimerUID = pant.getClaimerUID();
                if (currentUser.equals(claimerUID)) {
                    claim.setText("UNCLAIM");
                    if (dataSnapshot.exists()) {

                        if (currentUser.equals(claimerUID)) {
                            claim.setText("UNCLAIM");
                            collectButton.setVisibility(View.VISIBLE);
                        }
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            ;
        });

        double latitudeMarker = intent.getDoubleExtra("latitudeMarker", 0);
        double longitudeMarker = intent.getDoubleExtra("longitudeMarker", 0);


        claim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (claim.getText().equals("UNCLAIM")) {
                            pantRef.child("claimerUID").setValue("");
                            finish();
                        }

                        if (claim.getText().equals("CLAIM")) {
                            if (Math.pow(latitudeMarker - location.getLatitude(), 2) + Math.pow(longitudeMarker - location.getLongitude(), 2) <= 0.001) {
                                pantRef.child("claimerUID").setValue(currentUser);
                                finish();
                            }
                        }
                    }
                });


            }
        });

        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (Math.pow(latitudeMarker - location.getLatitude(), 2) + Math.pow(longitudeMarker - location.getLongitude(), 2) <= 0.000001) {
                            pantRef.removeValue();
                            finish();
                        }
                    }
                });

          }
       });

    }



}