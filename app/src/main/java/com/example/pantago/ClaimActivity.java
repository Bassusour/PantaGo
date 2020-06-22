package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;;import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;;
import android.widget.Toast;

public class ClaimActivity extends AppCompatActivity {

    String TAG = "pantaGo";
    public static boolean claimed;
    FirebaseAuth firebaseAuth;
    FusedLocationProviderClient fusedLocationProviderClient;
    private ClaimActivity mContext;

    private double maxCollectDistance = 0.05;
    private double maxClaimDistance = 3.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
        TextView textViewQuantity = findViewById(R.id.textViewClaimQuantity);
        TextView textViewDescription = findViewById(R.id.textViewClaimDescription);
        ImageView imageViewPant = findViewById(R.id.imageViewPant);
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
        Log.i(TAG, key);

        FirebaseStorage.getInstance().getReference().child("Pictures/"+key+".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageViewPant.setImageBitmap(map);

            }
        });

        DatabaseReference pantRef = databaseReference.child("pants").child( key);
        pantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ammount = getString(R.string.object_label);
                String comment = getString(R.string.comment);

                if (dataSnapshot.exists()) {
                    Pant pant = dataSnapshot.getValue(Pant.class);
                    String claimerUID = pant.getClaimerUID();
                    if (currentUser.equals(claimerUID)) {
                        claim.setText(getResources().getString(R.string.unclaim_button));
                        collectButton.setVisibility(View.VISIBLE);
                    }
                    textViewQuantity.setText(ammount + pant.getQuantity() + "");
                    textViewDescription.setText(comment + pant.getDescription());
                }else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        double latitudeMarker = intent.getDoubleExtra("latitudeMarker", 0);
        double longitudeMarker = intent.getDoubleExtra("longitudeMarker", 0);

        claim.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (claim.getText().equals(getResources().getString(R.string.unclaim_button))) {
                        pantRef.child("claimerUID").setValue("");
                        databaseReference.child("claimers").child(currentUser).removeValue();
                        finish();
                    }

                    if (claim.getText().equals(getResources().getString(R.string.claim_button))) {
                        databaseReference.child("claimers").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Toast.makeText(mContext, getResources().getString(R.string.cannot_claim), Toast.LENGTH_LONG).show();

                                }else{
                                    if (MapsActivity.getDistance(location.getLatitude(), location.getLongitude(), latitudeMarker, longitudeMarker)/1000.0 < maxClaimDistance) {
                                        pantRef.child("claimerUID").setValue(currentUser);
                                        databaseReference.child("claimers").child(currentUser).setValue(currentUser);
                                        finish();

                                    } else {
                                        double len = MapsActivity.getDistance(latitudeMarker, longitudeMarker, location.getLatitude(), location.getLongitude())/1000;
                                        Toast.makeText(mContext, getResources().getString(R.string.far_toast_first) + " " +String.format("%.1f", len)+ getResources().getString(R.string.far_toast_second), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                }
            });


        });

        collectButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return;
            }
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (MapsActivity.getDistance(location.getLatitude(), location.getLongitude(), latitudeMarker, longitudeMarker)/1000.0 < maxCollectDistance) {
                        pantRef.removeValue();
                        databaseReference.child("claimers").child(currentUser).removeValue();
                        finish();
                    } else {
                        double len = MapsActivity.getDistance(latitudeMarker, longitudeMarker, location.getLatitude(), location.getLongitude())/1000;
                        Toast.makeText(mContext, getResources().getString(R.string.collect_toast_first) + " " +String.format("%.1f", len)+ getResources().getString(R.string.collect_toast_second), Toast.LENGTH_LONG).show();
                    }
                }
            });

      });

    }



}