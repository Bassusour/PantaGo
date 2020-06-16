package com.example.pantago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.invoke.ConstantCallSite;
import java.util.Collections;
import java.util.List;

public class UploadActivity extends AppCompatActivity {

    private Button uploadButton;
    private EditText amountText;
    private EditText commentText;
    private TextView cameraText;
    private SurfaceView pictureView;

    String TAG = "pantaGo";

    private CameraManager cameraManager;
    String[] camList;
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        uploadButton = (Button) findViewById(R.id.buttonUpload);
        amountText = (EditText) findViewById(R.id.number_of_objects);
        commentText = (EditText) findViewById(R.id.upload_comment);
        pictureView = (SurfaceView) findViewById(R.id.pictureView);
        //cameraText = (TextView) findViewById(R.id.camera_icon)

       /* cameraText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the camera to upload picture
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "No camera permission");
                    return;
                }else{
                    try {
                        cameraManager.openCamera(camList[0], callback, null);
                    } catch (CameraAccessException e) {
                        Log.i(TAG, "fucko the cam");
                    }
                }

            }
        });*/



        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sAmount = amountText.getText().toString();
                Log.i(TAG, "onClick()");
                try{
                    if(!sAmount.isEmpty()){
                        if(isInteger(sAmount)) {
                            int amount = Integer.parseInt(sAmount);
                            String comment = commentText.getText().toString();
                            Log.i(TAG, "in amount filled");

                            Intent intent = getIntent();
                            double latitude = intent.getDoubleExtra("latitude", 0);
                            double longitude = intent.getDoubleExtra("longitude", 0);
                            intent.putExtra("amount",amountText.getText().toString());
                            intent.putExtra("comment",commentText.getText().toString());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }else{
                            Toast toast = Toast.makeText(mContext,"Please insert a number", Toast.LENGTH_SHORT);
                            toast.show();
                        }



                    }else{
                        Log.i(TAG, "in toast");
                        Toast toast = Toast.makeText(mContext,"Please fill amount of bottles/cans", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch(Exception e){

                }
            }
        });
    }

    public boolean isInteger(String s){
        boolean isValidInteger = false;
        try
        {
            Integer.parseInt(s);

            // s is a valid integer

            isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
            // s is not an integer
        }

        return isValidInteger;
    }


}
