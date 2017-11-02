package com.example.nesty.theapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.photo.BitmapPhoto;
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


        private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
        private boolean hasCameraPermission;
        private CameraView cameraView;

        private FotoapparatSwitcher fotoapparatSwitcher;
        private Fotoapparat frontFotoapparat;
        private Fotoapparat backFotoapparat;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_meniu);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();//In this way the VM ignores the file URI exposure.
            StrictMode.setVmPolicy(builder.build());// pe scurt, ma lasa sca folosesc Uri pentru versiuni mai noi de 23API
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
            PhotoResult photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();


            try {
                PendingResult<BitmapPhoto> result = photoResult.toBitmap();
                BitmapPhoto bitmapPhoto = result.await();
                Bitmap bitmap = bitmapPhoto.bitmap;

                bitmap.
                croppedBitmap = crop(bitmap);
                ocrtText = ocr(croppedBitmap);
                TesxtToSpeech(ocrText);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File photoFile = new File(getExternalFilesDir("cam_app"), getPicture());
            photoFile.getAbsolutePath();
            photoResult.saveToFile(photoFile);

            Intent media = fotoapparatSwitcher.getCurrentFotoapparat().takePicture() ;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
            File image = null;
            try {
                image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri photoURI = Uri.fromFile(image);
            media.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(media, 1);
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