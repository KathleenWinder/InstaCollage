package ru.akbashev.instacollage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class MainActivity extends Activity {

    //vars to work with url
    public static final String MAIN_URL = "https://api.instagram.com/v1/users/";
    public static final String MEDIA = "/media/recent/?";
    public static final String SEARCH = "search?q=";
    public static final String ACCESS_TOKEN = "&access_token=7922048.f59def8.5f29380f2a5a4b38890670852004960d&count=-1";
    public static final String CLIENT_ID = "&client_id=4361da965c654fafa36db6803d26a562";

    //vars for asynctask
    public static final int DEFAULT = 0;
    public static final int OK = 1;
    public static final int NO_USER = 2;

    private ArrayList<InstaImages> instaImages;
    private GridView gridView;
    private DisplayImageOptions options;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);
        mImageView = (ImageView) findViewById(R.id.profileImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CheckNameTask().execute(editText.getText().toString());
            }
        });
        gridView = (GridView) findViewById(R.id.gridView);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(MainActivity.this, String.valueOf(instaImages.get(position).count), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    class CheckNameTask extends AsyncTask<String, Void, Integer> {

        private String mName;
        private String profilePicture;
        private int mId;
        private ProgressDialog mProgressDialog;

        @Override
        protected Integer doInBackground(String... name) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                HttpResponse response = null;
                request = new HttpGet(MAIN_URL + SEARCH + name[0] + CLIENT_ID);
                response = client.execute(request);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String json = reader.readLine();
                JSONObject meta = new JSONObject(json).getJSONObject("meta");
                JSONArray array = new JSONObject(json).getJSONArray("data");
                if (meta.getInt("code") == 200){
                    if (array.length() == 0)
                        return NO_USER;
                    else {
                        JSONObject user = array.getJSONObject(0);
                        String userName = user.getString("username");
                        profilePicture = user.getString("profile_picture");
                        if (userName.equalsIgnoreCase(name[0])){
                            mName = userName;
                            mId = user.getInt("id");
                            return OK;
                        }
                        else
                            return NO_USER;
                    }
                } else
                    return DEFAULT;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return DEFAULT;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Checking user");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            switch (result){
                case DEFAULT:
                    Toast.makeText(MainActivity.this,"Mistake", Toast.LENGTH_SHORT).show();
                    break;
                case OK:
                    new GetImagesTask().execute(String.valueOf(mId));
                    ImageLoader.getInstance().displayImage(profilePicture,mImageView,options);
                    break;
                case NO_USER:
                    Toast.makeText(MainActivity.this,"No such user", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class GetImagesTask extends AsyncTask<String, Void, Integer> {

        private ProgressDialog mProgressDialog;

        @Override
        protected Integer doInBackground(String... name) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                HttpResponse response = null;
                request = new HttpGet(MainActivity.MAIN_URL + name[0] + MainActivity.MEDIA  + MainActivity.ACCESS_TOKEN);
                response = client.execute(request);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String json = reader.readLine();
                JSONObject meta = new JSONObject(json).getJSONObject("meta");
                JSONArray array = new JSONObject(json).getJSONArray("data");
                if (meta.getInt("code") == 200){
                    if (array.length() == 0)
                        return MainActivity.NO_USER;
                    else {
                        instaImages = new ArrayList<InstaImages>();
                        for (int i = 0; i < array.length(); i++){
                            JSONObject current = array.getJSONObject(i);
                            if (current.getString("type").equalsIgnoreCase("image")){
                                instaImages.add(new InstaImages(current.getJSONObject("likes").getInt("count"),current.getJSONObject("images").getJSONObject("standard_resolution").getString("url")));
                            }
                        }
                        Collections.sort(instaImages, new InstaComparator());
                        return MainActivity.OK;
                    }
                } else
                    return MainActivity.DEFAULT;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return MainActivity.DEFAULT;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Downloading images");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            switch (result){
                case MainActivity.DEFAULT:
                    Toast.makeText(MainActivity.this, "Mistake", Toast.LENGTH_SHORT).show();
                    break;
                case MainActivity.OK:
                    gridView.setAdapter(new ImageAdapter(MainActivity.this,instaImages,options));
                    break;
                case MainActivity.NO_USER:
                    Toast.makeText(MainActivity.this,"No such user", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class InstaComparator implements Comparator<InstaImages> {

        @Override
        public int compare(InstaImages o1, InstaImages o2) {
            return o2.count - o1.count;
        }
    }


    }
