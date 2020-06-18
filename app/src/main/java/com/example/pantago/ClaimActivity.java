package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;;import com.google.android.gms.maps.model.LatLng;
import android.view.Window;
import android.widget.Button;;

public class ClaimActivity extends AppCompatActivity {

    String TAG = "pandaGo";
    public static boolean claimed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
        Button tilbage = findViewById(R.id.buttonTilbage);

        Intent intent = getIntent();

       claim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitudeUser = intent.getDoubleExtra("latitudeUser", 0);
                double longitudeUser = intent.getDoubleExtra("longitudeUser", 0);
                double latitudeMarker = intent.getDoubleExtra("latitudeMarker", 0);
                double longitudeMarker = intent.getDoubleExtra("longitudeMarker", 0);
                Log.i(TAG, String.valueOf(latitudeMarker));
                Log.i(TAG, String.valueOf(latitudeUser));
                if (Math.pow(latitudeMarker-latitudeUser,2) + Math.pow(longitudeMarker-longitudeUser,2) <= 0.000001 ) {
                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
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