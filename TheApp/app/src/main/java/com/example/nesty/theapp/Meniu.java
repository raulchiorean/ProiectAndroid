package com.example.nesty.theapp;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class Meniu extends AppCompatActivity {
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meniu);
        Button opCamera= (Button) findViewById(R.id.openCamera);
        opCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file =getFile();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent,0);
            }
        });
    }
    private File getFile(){
        File folder = new File("sdcard/camera_app");
        if(!folder.exists())
        {
            folder.mkdir();

        }
        File image_file = new File (folder,i+"cam_image.jpg");
        i++;
        return image_file;
    }

}