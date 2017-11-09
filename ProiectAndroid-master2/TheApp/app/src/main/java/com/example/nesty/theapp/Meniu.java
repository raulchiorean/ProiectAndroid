package com.example.nesty.theapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
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
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;

public class Meniu extends AppCompatActivity {

        private GestureDetectorCompat gestureObject;
        public static final int OCR_ACTIVITY_CODE = 123;
        private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
        private boolean hasCameraPermission;
        private CameraView cameraView;
        TextToSpeech t1;
        public String _pictureFilePath;

        private FotoapparatSwitcher fotoapparatSwitcher;
        private Fotoapparat frontFotoapparat;
        private Fotoapparat backFotoapparat;

        private GestureDetectorCompat mDetector;
    static{ System.loadLibrary("opencv_java"); }
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_meniu);

            cameraView = (CameraView) findViewById(R.id.camera_view);
            hasCameraPermission = permissionsDelegate.hasCameraPermission();

            if (hasCameraPermission) {
                cameraView.setVisibility(View.VISIBLE);
            } else {
                permissionsDelegate.requestCameraPermission();
            }

            setupFotoapparat();

            takePictureOnClick();
            focusOnLongClick();
        }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }




    private void setupFotoapparat() {
            frontFotoapparat = createFotoapparat(LensPosition.FRONT);
            backFotoapparat = createFotoapparat(LensPosition.BACK);
            fotoapparatSwitcher = FotoapparatSwitcher.withDefault(backFotoapparat);
        }







        private void focusOnLongClick() {
            cameraView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    fotoapparatSwitcher.getCurrentFotoapparat().autoFocus();

                    return true;
                }
            });
        }

        private void takePictureOnClick() {
            cameraView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });
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
                    .flash(firstAvailable(
                            autoRedEye(),
                            autoFlash(),
                            torch(),
                            off()
                    ))
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
    private String getPicture() { // genereaza un nume unic
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss"); // data si ora
        String timestamp = sdf.format(new Date());
        return timestamp +".jpg";
    }

        private void takePicture() {

            Fotoapparat.with(this);
            PhotoResult photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();
        /*    try {
                PendingResult<BitmapPhoto> result = photoResult.toBitmap();
                BitmapPhoto bitmapPhoto = result.await();
                Bitmap bitmap = bitmapPhoto.bitmap;

            //   bitmap.croppedBitmap = crop(bitmap);
            //    ocrtText = ocr(croppedBitmap);

             //   TesxtToSpeech(ocrText);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */


            final File file = new File(getExternalFilesDir("cam_app"), getPicture());
            _pictureFilePath=file.getAbsolutePath();


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
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    Bitmap myBitmap32 = BitmapFactory.decodeFile(_pictureFilePath);
                    Mat matImage = new Mat(myBitmap32.getHeight(), myBitmap32.getWidth(), CvType.CV_8UC3);
                    Utils.bitmapToMat(myBitmap32, matImage);
                    Mat gray = new Mat(matImage.size(), CvType.CV_8UC1);
                    Imgproc.cvtColor(matImage, gray, Imgproc.COLOR_RGB2GRAY, 4);
                    Mat edge = new Mat();
                    Mat dst = new Mat();
                    Imgproc.Canny(gray, edge, 60, 80);
                    Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2RGBA, 4);
                    Bitmap resultBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(dst, resultBitmap);

                    final String detectedText = data.getStringExtra("detectedText");

                    t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t1.setLanguage(Locale.US);
                                t1.setSpeechRate(0.72f);
                                int success = t1.speak(detectedText, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }


                    });


                }
                break;
            }
        }
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
            if (hasCameraPermission) {
                fotoapparatSwitcher.stop();
            }
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

        private class SampleFrameProcessor implements FrameProcessor {

            @Override
            public void processFrame(Frame frame) {
                // Perform frame processing, if needed
            }

        }




}