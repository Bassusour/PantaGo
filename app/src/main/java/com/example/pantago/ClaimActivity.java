package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.widget.Button;;

public class ClaimActivity extends AppCompatActivity {

    public static boolean claimed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_claim);
        Button claim = (Button) findViewById(R.id.buttonClaim);
    }



}