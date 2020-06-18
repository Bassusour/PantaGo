package com.example.pantago;

import android.graphics.Bitmap;

public class PantListObject {
    Pant pant;
    Bitmap bitmap;
    Double distance;

    public PantListObject(Pant pant, Bitmap bitmap, double distance){
        this.pant = pant;
        this.bitmap = bitmap;
        this.distance = distance;
    }
    public Pant getPant() {
        return pant;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Double getDistance() {
        return distance;


    }
}
