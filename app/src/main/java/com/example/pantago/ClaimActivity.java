package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;;

public class ClaimActivity extends AppCompatActivity {

    public static boolean claimed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
    }



}