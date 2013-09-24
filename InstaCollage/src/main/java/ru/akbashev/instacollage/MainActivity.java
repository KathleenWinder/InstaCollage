package ru.akbashev.instacollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
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
    private static final int DEFAULT = 0;
    private static final int OK = 1;
    private static final int NO_USER = 2;
    private ArrayList<InstaImages> instaImages;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        final EditText editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CheckNameTask().execute(editText.getText().toString());
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
        private int mId;

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
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            switch (result){
                case DEFAULT:
                    Toast.makeText(MainActivity.this,"Mistake", Toast.LENGTH_SHORT).show();
                    break;
                case OK:
                    new GetImagesTask().execute(String.valueOf(mId));
                    Toast.makeText(MainActivity.this,"WELL DONE! User - " + mName + ", ID = " + mId, Toast.LENGTH_SHORT).show();
                    break;
                case NO_USER:
                    Toast.makeText(MainActivity.this,"No such user", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class GetImagesTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... name) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                HttpResponse response = null;
                request = new HttpGet(MAIN_URL + name[0] + MEDIA  + ACCESS_TOKEN);
                response = client.execute(request);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String json = reader.readLine();
                JSONObject meta = new JSONObject(json).getJSONObject("meta");
                JSONArray array = new JSONObject(json).getJSONArray("data");
                if (meta.getInt("code") == 200){
                    if (array.length() == 0)
                        return NO_USER;
                    else {
                        instaImages = new ArrayList<InstaImages>();
                        for (int i = 0; i < array.length(); i++){
                            JSONObject current = array.getJSONObject(i);
                            if (current.getString("type").equalsIgnoreCase("image")){
                                instaImages.add(new InstaImages(current.getJSONObject("likes").getInt("count"),current.getJSONObject("images").getJSONObject("standard_resolution").getString("url")));
                            }
                        }
                        Collections.sort(instaImages, new InstaComparator());
                        return OK;
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
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            switch (result){
                case DEFAULT:
                    Toast.makeText(MainActivity.this,"Mistake", Toast.LENGTH_SHORT).show();
                    break;
                case OK:
                    File cacheDir = StorageUtils.getCacheDirectory(MainActivity.this);
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MainActivity.this)
                            .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                            .discCacheExtraOptions(480, 800, Bitmap.CompressFormat.JPEG, 75, null)
                            .threadPriority(Thread.NORM_PRIORITY - 1) // default
                            .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                            .denyCacheImageMultipleSizesInMemory()
                            .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                            .memoryCacheSize(2 * 1024 * 1024)
                            .memoryCacheSizePercentage(13) // default
                            .discCache(new UnlimitedDiscCache(cacheDir)) // default
                            .discCacheSize(50 * 1024 * 1024)
                            .discCacheFileCount(100)
                            .discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                            .imageDownloader(new BaseImageDownloader(MainActivity.this)) // default
                            .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                            .writeDebugLogs()
                            .build();
                    ImageLoader.getInstance().init(config);
                    ImageLoader.getInstance().displayImage(instaImages.get(0).url,imageView);

                    Toast.makeText(MainActivity.this,"WELL DONE!", Toast.LENGTH_SHORT).show();
                    break;
                case NO_USER:
                    Toast.makeText(MainActivity.this,"No such user", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class InstaImages {

        public String url;
        public int count;

        public InstaImages(int count, String url) {
            this.url = url;
            this.count = count;
        }

    }

    public class InstaComparator implements Comparator<InstaImages> {

        @Override
        public int compare(InstaImages o1, InstaImages o2) {
            return o2.count - o1.count;
        }
    }


    }
