package ru.akbashev.instacollage;

import android.graphics.Bitmap;

public class InstaImages {

    public String url;
    public int count;
    public Bitmap bitmap;

    public InstaImages(int count, String url, Bitmap bitmap) {
        this.url = url;
        this.count = count;
        this.bitmap = bitmap;
    }
}