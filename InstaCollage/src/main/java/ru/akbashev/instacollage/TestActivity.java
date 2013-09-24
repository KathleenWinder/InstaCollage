package ru.akbashev.instacollage;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class TestActivity extends Activity {

    private static final String MAIN_URL = "https://api.instagram.com/v1/users/";
    private static final int USER_ID = 0;
    private static final String ACCESS_TOKEN = "/media/recent/?access_token=7922048.f59def8.5f29380f2a5a4b38890670852004960d&count=-1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }
    
}

