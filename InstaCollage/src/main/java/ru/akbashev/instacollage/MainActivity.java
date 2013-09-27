package ru.akbashev.instacollage;

import android.content.Context;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

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

    private ArrayList<InstaImages> instaImages = new ArrayList<InstaImages>();
    private ArrayList<Integer> mPositions = new ArrayList<Integer>();
    private Map<Integer,Bitmap> mBitmaps = new HashMap<Integer,Bitmap>();
    private GridView gridView;
    private DisplayImageOptions options;
    private ImageView mImageView;
    private ImageAdapter adapter;
    private Button send;
    private Button cancel;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);
        send = (Button) findViewById(R.id.send);
        cancel = (Button) findViewById(R.id.cancel);
        textView = (TextView) findViewById(R.id.textView);
        mImageView = (ImageView) findViewById(R.id.profileImage);
        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new ImageAdapter(MainActivity.this,instaImages,options);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CheckNameTask().execute(editText.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                onCancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSend();
            }
        });

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(612, 612)
                .discCacheExtraOptions(612, 612, Bitmap.CompressFormat.JPEG, 75, null)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    onItemSelect(position);
            }
        });
    }

    void onCancel(){
        for (int i = 0; i < mPositions.size(); i++)
            instaImages.get(mPositions.get(i)).state = false;
        mBitmaps.clear();
        mPositions.clear();
        adapter.notifyDataSetChanged();
        textView.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
    }

    void onSend(){
        Bitmap bmOverlay = Bitmap.createBitmap(mBitmaps.get(mPositions.get(0)).getWidth()*2, mBitmaps.get(mPositions.get(0)).getHeight()*2, mBitmaps.get(mPositions.get(0)).getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(mBitmaps.get(mPositions.get(0)), 0f, 0f, null);
        canvas.drawBitmap(mBitmaps.get(mPositions.get(1)), mBitmaps.get(mPositions.get(0)).getWidth(), 0f, null);
        canvas.drawBitmap(mBitmaps.get(mPositions.get(2)), 0f, mBitmaps.get(mPositions.get(0)).getHeight(), null);
        canvas.drawBitmap(mBitmaps.get(mPositions.get(3)), mBitmaps.get(mPositions.get(0)).getHeight(), mBitmaps.get(mPositions.get(0)).getWidth(), null);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmOverlay.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] byteArray = stream.toByteArray();

        FragmentManager fm = getSupportFragmentManager();
        SendDialogFragment dF = new SendDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putByteArray("pathOfBmp", byteArray);
        dF.setArguments(bundle);
        dF.show(fm, null);
    }

    void onItemSelect(int position){
        if (mBitmaps.get(position) == null){
            if (mBitmaps.size() < 4 && instaImages.get(position).bitmap != null){
                mBitmaps.put(position,instaImages.get(position).bitmap);
                mPositions.add(position);
                instaImages.get(position).state = true;
                adapter.notifyDataSetChanged();
                textView.setText(String.valueOf(mBitmaps.size()));
                textView.setVisibility(View.VISIBLE);
            }
            if (mBitmaps.size() == 4){
                send.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
            }
        }else {
            mBitmaps.remove(position);
            mPositions.remove((Object) position);
            instaImages.get(position).state = false;
            adapter.notifyDataSetChanged();
            textView.setText(String.valueOf(mBitmaps.size()));
            send.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
        }
        if (mPositions.size() == 0){
            textView.setVisibility(View.GONE);
        }
    }

    class CheckNameTask extends AsyncTask<String, Void, Integer> {

        private String mName;
        private String profilePicture;
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
            Toast.makeText(MainActivity.this,"Проверяем, существует ли такой пользователь...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            switch (result){
                case DEFAULT:
                    Toast.makeText(MainActivity.this,"Ошибка!", Toast.LENGTH_SHORT).show();
                    break;
                case OK:
                    new GetImagesTask().execute(String.valueOf(mId));
                    instaImages = new ArrayList<InstaImages>();
                    adapter = new ImageAdapter(MainActivity.this,instaImages,options);
                    gridView.setAdapter(adapter);
                    ImageLoader.getInstance().displayImage(profilePicture,mImageView,options);
                    break;
                case NO_USER:
                    Toast.makeText(MainActivity.this,"Такого пользователя не существует...", Toast.LENGTH_SHORT).show();
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
                                instaImages.add(new InstaImages(current.getJSONObject("likes").getInt("count"),current.getJSONObject("images").getJSONObject("standard_resolution").getString("url"), null, false));
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
            mProgressDialog.setMessage("Выбираем для вас лучшие фотографии пользователя...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            switch (result){
                case MainActivity.DEFAULT:
                    Toast.makeText(MainActivity.this, "Ошибка!", Toast.LENGTH_SHORT).show();
                    break;
                case MainActivity.OK:
                    adapter = new ImageAdapter(MainActivity.this,instaImages,options);
                    gridView.setAdapter(adapter);
                    break;
                case MainActivity.NO_USER:
                    Toast.makeText(MainActivity.this,"У этого пользователя нет фотографий...", Toast.LENGTH_SHORT).show();
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
