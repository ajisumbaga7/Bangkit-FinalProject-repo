package com.bangkit.pneumoniadetector;

import android.graphics.Bitmap;

public class bitmap_helper {
    private Bitmap bitmap = null;
    private static final bitmap_helper instance = new bitmap_helper();

    public bitmap_helper(){

    }

    public static bitmap_helper getInstance(){
        return instance;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setBitmap (Bitmap bitmap){
        this.bitmap = bitmap;
    }
}
