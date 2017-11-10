package com.example.nesty.theapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.opencv.core.CvType.CV_32SC1;

/**
 * Created by nesty on 11/2/2017.
 */

public class OCR extends AppCompatActivity {


    private Context _context = null;
    private static final String TAG = OCR.class.getSimpleName();

    public OCR()
    {
    }
    static{ System.loadLibrary("opencv_java"); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getApplicationContext(); //activity has a context now
    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String bitmapFilePath = intent.getStringExtra("bitmapFilePath");
        // BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFilePath, options);
        String text = convertBitmapToText(bitmap);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("detectedText", text);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void debugSaveBitmapToFile(Bitmap bitmap)
    {
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

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(image.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Mat computeXProjection(Mat image) {
        // Mat reducedImage = new Mat();
        // double f = 0.5d;
        // mgproc.resize(image, reducedImage, new Size(f*image.cols(), f*image.rows()));
        Mat bw = new Mat();
        Imgproc.threshold(image, bw, 128, 1, Imgproc.THRESH_BINARY| Imgproc.THRESH_OTSU);
        Mat horizontal = new Mat();
        Core.reduce(bw, horizontal, 0, Core.REDUCE_SUM, CV_32SC1);

        return horizontal;
    }

    private ArrayList<Integer> findXCropPositions(Mat projection)
    {

        return null;
    }

    private Bitmap crop(Bitmap bitmap)
    {
        Mat matImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, matImage);
        Mat gray = new Mat(matImage.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(matImage, gray, Imgproc.COLOR_RGB2GRAY, 4);
        Mat edge = new Mat();
        Mat dst = new Mat();
        Imgproc.Canny(gray, edge, 100, 200);
        Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2RGBA, 4);
        Bitmap resultBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, resultBitmap);

        Mat xprojection  = computeXProjection(edge);
        List<Integer> xCropPositions = findXCropPositions(xprojection);


        Imgproc.medianBlur(dst, dst, 5);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 10));
        Imgproc.dilate(dst, dst, kernel,  new Point(0, 1), 70);

        Utils.matToBitmap(dst, resultBitmap);



        debugSaveBitmapToFile(resultBitmap);

        return resultBitmap;
    }

    private String convertBitmapToText(Bitmap bitmap) {

        Bitmap croppedBitmap = crop(bitmap);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(_context).build();
        try {
            if (!textRecognizer.isOperational()) {
                new AlertDialog.
                        Builder(_context).
                        setMessage("Text recognizer could not be set up on your device").show();

            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue());
                    detectedText.append("\n");
                }
            }

            return detectedText.toString();

        } finally {
            textRecognizer.release();
        }
    }


}
