package com.example.nesty.theapp;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.Loggers.fileLogger;
import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.log.Loggers.loggers;
import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;

public class Meniu extends AppCompatActivity implements View.OnClickListener,GestureDetector.OnGestureListener {

        private GestureDetectorCompat gestureObject;
        public static final int OCR_ACTIVITY_CODE = 123;
        public static final int Shh = 1234;
        private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
        private boolean hasCameraPermission;
        private CameraView cameraView;
        TextToSpeech t1;
        public String _pictureFilePath;

        private FotoapparatSwitcher fotoapparatSwitcher;
        private Fotoapparat frontFotoapparat;
        private Fotoapparat backFotoapparat;
        private static final String DEBUG_TAG = "Gestures";

        private GestureDetectorCompat mDetector;
        String detectedText ;
        int n=1;
        int r=0;
        int r2=0;
        int jr=0;
        int ch=0;
        int cac=0;
        public int x=0;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    private static final String TAG = "TextToSpeechDemo";


    private static final String LAST_SPOKEN = "lastSpoken";


    private TextToSpeech tts;
    private int stopp=0;



    static{ System.loadLibrary("opencv_java"); }
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_meniu);
            MediaPlayer rang= MediaPlayer.create(this,R.raw.takepicture);
            rang.start();

            cameraView = (CameraView) findViewById(R.id.camera_view);
            hasCameraPermission = permissionsDelegate.hasCameraPermission();

            if (hasCameraPermission) {
                cameraView.setVisibility(View.VISIBLE);
            } else {
                permissionsDelegate.requestCameraPermission();
            }

            setupFotoapparat();


            gestureDetector = new GestureDetector(this);
            gestureListener = new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            };
            cameraView.setOnClickListener(Meniu.this);
            cameraView.setOnTouchListener(gestureListener);


        }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                t1.stop();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if(jr==0)
                {
                    next(jr);
                    jr++;
                }
                else
                {
                    next(jr);
                    jr=0;

                }

            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }



        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {


        File root = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        int m=0;
        while(m<2) {
            String imageFileName = m + ".txt";
            File file1 = new File(root,imageFileName);
            file1.delete();
            m++;
        }

        MediaPlayer ring = MediaPlayer.create(this, R.raw.cam);
        ring.start();
        takePicture();
        cac = 0;
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        fotoapparatSwitcher.getCurrentFotoapparat().autoFocus();
    }




    @Override
    public boolean onTouchEvent(MotionEvent event){

        return super.onTouchEvent(event);
    }







    private void setupFotoapparat() {
            frontFotoapparat = createFotoapparat(LensPosition.FRONT);
            backFotoapparat = createFotoapparat(LensPosition.BACK);
            fotoapparatSwitcher = FotoapparatSwitcher.withDefault(backFotoapparat);
        }



        private boolean canSwitchCameras() {
            return frontFotoapparat.isAvailable() == backFotoapparat.isAvailable();
        }

        private Fotoapparat createFotoapparat(LensPosition position) {
            return Fotoapparat
                    .with(this)
                    .into(cameraView)
                    .previewScaleType(ScaleType.CENTER_CROP)
                    .photoSize(standardRatio(biggestSize()))
                    .lensPosition(lensPosition(position))
                    .focusMode(firstAvailable(
                            continuousFocus(),
                            autoFocus(),
                            fixed()
                    ))
                    .flash(autoFlash() )
                    .frameProcessor(new SampleFrameProcessor())
                    .logger(loggers(
                            logcat(),
                            fileLogger(this)
                    ))
                    .cameraErrorCallback(new CameraErrorCallback() {
                        @Override
                        public void onError(CameraException e) {
                            Toast.makeText(Meniu.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .build();
        }
    private String generatePictureName() { // genereaza un nume unic
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss"); // data si ora
        String timestamp = sdf.format(new Date());
        return timestamp +".jpg";
    }

        private void takePicture() {

            Fotoapparat.with(this);
            PhotoResult photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();
            final File file = new File(getExternalFilesDir("cam_app"), generatePictureName());
            photoResult.saveToFile(file).whenDone(new PendingResult.Callback<Void>() {
                @Override
                public void onResult(Void t) {
                    Intent ocr = new Intent(Meniu.this, OCR.class);
                    ocr.putExtra("bitmapFilePath", file.getAbsolutePath());
                    startActivityForResult(ocr, Meniu.OCR_ACTIVITY_CODE);

                }
            });
        }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case OCR_ACTIVITY_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    final String detectedText = data.getStringExtra("detectedText");

                    t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {


                        @Override
                        public void onInit(int status) {
                            jr=0;


                            if (status != TextToSpeech.ERROR) {

                                t1.setLanguage(Locale.US);
                                t1.setSpeechRate(0.90f);
                                if(ch==0)
                                {
                                    int suc=t1.speak("Swipe up to start reading the text. Swipe up again to read next page. Swipe down to stop", TextToSpeech.QUEUE_FLUSH, null);
                                    ch++;
                                }
                            }
                        }


                    });




                }
                break;
            }
        }

    }
    public void next(int v)
    {

            String imageFileName = v + ".txt";
            File sdcard = getExternalFilesDir(Environment.DIRECTORY_DCIM);

            File file = new File(sdcard, imageFileName);
//Read text from filetry {
            String ret = "";
            try {
                InputStream inputStream = new FileInputStream(file);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }



            int success = t1.speak(ret, TextToSpeech.QUEUE_FLUSH, null);
            n=0;

    }



        @Override
        protected void onStart() {
            super.onStart();
            if (hasCameraPermission) {
                fotoapparatSwitcher.start();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            t1.stop();
            if (hasCameraPermission) {
                fotoapparatSwitcher.stop();
            }
        }
        public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
     }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
                fotoapparatSwitcher.start();
                cameraView.setVisibility(View.VISIBLE);
            }
        }

    @Override
    public void onClick(View v) {
        takePicture();

    }

    private class SampleFrameProcessor implements FrameProcessor {

            @Override
            public void processFrame(Frame frame) {
                // Perform frame processing, if needed
            }

        }




}