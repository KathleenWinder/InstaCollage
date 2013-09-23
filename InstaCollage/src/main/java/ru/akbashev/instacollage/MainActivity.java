package ru.akbashev.instacollage;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashSet;

public class MainActivity extends Activity {

    //vars to work with url
    public static final String MAIN_URL = "https://api.instagram.com/v1/users/";
    public static final int USER_ID = 0;
    public static final String MEDIA = "/media/recent/?";
    public static final String SEARCH = "search?q=";
    public static final String ACCESS_TOKEN = "&access_token=7922048.f59def8.5f29380f2a5a4b38890670852004960d";
    public static final String CLIENT_ID = "&client_id=4361da965c654fafa36db6803d26a562";

    //vars for asynctask
    private static final int DEFAULT = 0;
    private static final int OK = 1;
    private static final int NO_USER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                    Toast.makeText(MainActivity.this,"WELL DONE! User - " + mName + ", ID = " + mId, Toast.LENGTH_SHORT).show();
                    break;
                case NO_USER:
                    Toast.makeText(MainActivity.this,"No such user", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    
}
