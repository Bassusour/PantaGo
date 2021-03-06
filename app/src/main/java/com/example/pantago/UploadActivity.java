package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class UploadActivity extends AppCompatActivity {

    private Button uploadButton;
    private EditText amountText;
    private EditText commentText;
    private TextView cameraText;
    private ImageView pictureView;

    String TAG = "pantaGo";
    private final int REQUEST_IMAGE_CAPTURE = 1;

    private CameraManager cameraManager;
    String[] camList;
    Context mContext = this;
    private Bitmap bitmap;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(UploadActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE);
        }
        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_upload);
        uploadButton = (Button) findViewById(R.id.buttonUpload);
        amountText = (EditText) findViewById(R.id.number_of_objects);
        commentText = (EditText) findViewById(R.id.upload_comment);
        pictureView = (ImageView) findViewById(R.id.pictureView);

        pictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                    Intent cam =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cam, REQUEST_IMAGE_CAPTURE);
                } else {
                    ActivityCompat.requestPermissions(UploadActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_IMAGE_CAPTURE);
                }

            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sAmount = amountText.getText().toString();
                Log.i(TAG, "onClick()");
                try{
                    if(!sAmount.isEmpty()){
                        if(isPositiveInteger(sAmount)) {
                            if(bitmap != null){
                                int amount = Integer.parseInt(sAmount);
                                String comment = commentText.getText().toString();
                                Log.i(TAG, "in amount filled");

                                Intent intent = getIntent();
                                double latitude = intent.getDoubleExtra("latitude", 0);
                                double longitude = intent.getDoubleExtra("longitude", 0);

                                intent.putExtra("amount",amountText.getText().toString());
                                intent.putExtra("comment",commentText.getText().toString());

                                Pant pant = new Pant(comment, amount, latitude, longitude);

                                Log.i(TAG, String.valueOf(pant.getLatitude()));
                                Log.i(TAG, String.valueOf(pant.getLongitude()));

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                FirebaseStorage storage = FirebaseStorage.getInstance();


                                DatabaseReference databaseReference = database.getReference();

                                String pantID = databaseReference.push().getKey();
                                StorageReference storageReference = storage.getReference().child("Pictures/"+pantID+".jpg");

                                pant.setPantKey(pantID);

                                pant.setOwnerUID(firebaseAuth.getCurrentUser().getUid());

                                databaseReference.child("pants").child(pantID).setValue(pant);



                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                UploadTask uploadTask = storageReference.putBytes(data);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.i(TAG,"no");
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Log.i(TAG,"YES");
                                    }
                                });

                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }else{
                                Toast toast = Toast.makeText(mContext,getResources().getString(R.string.insert_picture), Toast.LENGTH_SHORT);
                                toast.show();
                            }

                        }else{
                            amountText.requestFocus();
                            amountText.setError(getResources().getString(R.string.not_number));
                            Toast toast = Toast.makeText(mContext,getResources().getString(R.string.insert_number), Toast.LENGTH_SHORT);
                            //toast.show();
                        }



                    }else{
                        Log.i(TAG, "in toast");
                        amountText.requestFocus();
                        amountText.setError(getResources().getString(R.string.no_amount));
                        Toast toast = Toast.makeText(mContext,getResources().getString(R.string.fill_amount), Toast.LENGTH_SHORT);
                        //toast.show();
                    }

                }catch(Exception e){

                }
            }
        });
    }

    public boolean isPositiveInteger(String s){
        int i;
        boolean isValidInteger = false;
        try
        {
            i = Integer.parseInt(s);
            // s is a valid integer
            isValidInteger = true;
            if(i < 1){
                isValidInteger = false;
            }
        }
        catch (NumberFormatException ex)
        {
            // s is not an integer
        }

        return isValidInteger;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            pictureView.setImageBitmap(bitmap);
        }
    }


}
