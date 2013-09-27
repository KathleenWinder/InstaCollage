package ru.akbashev.instacollage;

import android.graphics.Bitmap;

public class InstaImages {

    public String url;
    public int count;
    public Bitmap bitmap;
    public boolean state;

    public InstaImages(int count, String url, Bitmap bitmap, boolean state) {
        this.url = url;
        this.count = count;
        this.bitmap = bitmap;
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

}