package com.example.facedetector;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class MyFaceDetector {

    private Context context;

    public MyFaceDetector(Context context) {
        this.context = context;
    }

    public boolean containsFace(Bitmap photo) {
        FaceDetector faceDetector = new
                FaceDetector.Builder(context.getApplicationContext()).setTrackingEnabled(false)
                .build();

        if(!faceDetector.isOperational()){
            //new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
            return false;
        }
        //Bitmap bmp = BitmapFactory.decodeByteArray(photo, 0, photo.length);
        Frame frame = new Frame.Builder().setBitmap(photo).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        return faces.size() > 0;
    }

    public Bitmap frameTheFace(Bitmap photo) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        tempCanvas.drawBitmap(photo, 0, 0, null);

        FaceDetector faceDetector = new
                FaceDetector.Builder(context.getApplicationContext()).setTrackingEnabled(false)
                .build();


        Frame frame = new Frame.Builder().setBitmap(photo).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        if (faces.size() == 0) {
            return photo;
        }
        for(int i = 0; i < faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }
        return tempBitmap;
    }


}
