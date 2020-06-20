package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;;
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
import android.widget.TextView;
;

public class RemoveActivity extends AppCompatActivity {

    String TAG = "pandaGo";
    FirebaseAuth firebaseAuth;
    Pant pant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_remove);
        Button remove = (Button) findViewById(R.id.removeButton);
        TextView textViewQuantity = findViewById(R.id.textViewRemoveQuantity);
        TextView textViewDescription = findViewById(R.id.textViewRemoveDescription);
        ImageView imageViewPant = findViewById(R.id.imageViewPantRemove);

        Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        String key = intent.getStringExtra("pantKey");

        FirebaseStorage.getInstance().getReference().child("Pictures/"+key+".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageViewPant.setImageBitmap(map);

            }
        });
        DatabaseReference pantRef = databaseReference.child("pants").child(key);

        pantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    pant = dataSnapshot.getValue(Pant.class);
                    String ammount = getString(R.string.object_label);
                    String comment = getString(R.string.comment);
                    textViewQuantity.setText(ammount + pant.getQuantity() + "");
                    textViewDescription.setText(comment + pant.getDescription());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                databaseReference.child("claimers").child(pant.getClaimerUID()).removeValue();
                pantRef.removeValue();
                setResult(Activity.RESULT_OK, getIntent());
                finish();
            }
        });


    }



}