package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    int id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();

        Button createUserBtn = findViewById(R.id.makeUser);
        createUserBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Pant pant = new Pant("Samle op ude foran døren", 4);
                ref.child("pants").child(Integer.toString(id)).setValue(pant);
                Log.i("tag",ref.child("id").getKey());
            }
        });
    }



}