package com.example.pantago;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.invoke.ConstantCallSite;

public class UploadActivity extends AppCompatActivity {

    private Button uploadButton;
    private EditText amountText;
    private EditText commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        uploadButton = (Button) findViewById(R.id.buttonUpload);
        amountText = (EditText) findViewById(R.id.number_of_objects);
        commentText = (EditText) findViewById(R.id.upload_comment);

        uploadButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try{
                    if(amountText.getText().toString() != null){
                        int amount = Integer.parseInt(amountText.getText().toString());
                        String comment = commentText.getText().toString();
                    }else{
                        Toast.makeText(getApplicationContext(),"Please fill amount of bottles/cans", Toast.LENGTH_SHORT);
                    }

                }catch(Exception e){

                }
            }
        });
    }


}