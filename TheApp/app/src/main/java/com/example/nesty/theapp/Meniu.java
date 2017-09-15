package com.example.nesty.theapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Meniu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meniu);
        Button opCamera= (Button) findViewById(R.id.openCamera);//am creat button
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();//In this way the VM ignores the file URI exposure.
        StrictMode.setVmPolicy(builder.build());// pe scurt, ma lasa sca folosesc Uri pentru versiuni mai noi de 23API
        opCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//invoke camera
                //File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //salveaza pe sdcard in directoru Pictures
                File file =getFile();
               // String pictureName = getPicture();// folosim getPicture ca sa creem un nume unic pentru poza
               // File imageFile = new File(pictureDirectory,pictureName);//creeaza un path
                Uri pictureUri1 = Uri.fromFile(file);// convert to Uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri1);// semna pentru ca vrem sa salvam imaginea
                startActivityForResult(intent,0);//porneste aplicatia
            }

            private String getPicture() { // genereaza un nume unic
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss"); // data si ora
                String timestamp = sdf.format(new Date());
                return timestamp +".jpg";
            }
            private File getFile(){ // creeaza un folder
                File folder = new File("sdcard/camera_app");
                if(!folder.exists()) //creeaza folderu daca nu exista
                {
                    folder.mkdir();
                }
                File image_file = new File (folder,getPicture());//creeaza un path
                return image_file;
            }
        });
    }


}