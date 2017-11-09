package com.example.nesty.theapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nesty on 11/2/2017.
 */

public class OCR extends AppCompatActivity {


    private Context _context = null;
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

    public String convertBitmapToText(Bitmap bitmap) {
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
