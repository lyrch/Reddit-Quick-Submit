package com.redditandroiddevelopers.RedditQuickSubmit;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SubmitLinkActivity extends Activity {


    private static final String TAG = "SubLink";
    private static final int LOGIN_RQ = 1;
    private String Cookie;
    private String response = "";
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submitlink);

        AsyncHttpClient myClient = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        
        if(!settings.getBoolean("haveAccount",false)){
            Log.i(TAG, "Retrieving Login info");
            Intent loginIntent = new Intent(this,LoginActivity.class);
            loginIntent.putExtra("needsLogin",true);
            startActivityForResult(loginIntent, LOGIN_RQ);
        }
        
        if(action != null && action.equals(Intent.ACTION_SEND)){
            Log.i(TAG,"Entering from Share");
            EditText uriText = (EditText) findViewById(R.id.linkForm);
            uriText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            EditText titleText = (EditText) findViewById(R.id.titleForm);
            titleText.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
        }
        
        Cookie = myCookieStore.getCookies().toString();

        final Button submit = (Button) findViewById(R.id.submitLinkButton);

        submit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AsyncTask task = new submitLink().execute();
            }
        });

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LOGIN_RQ){
            if(resultCode == RESULT_CANCELED){
                Log.i(TAG,"Ignored Login.");
                finish();
            } 
            settings = PreferenceManager.getDefaultSharedPreferences(this);
        }
    }

    class submitLink extends AsyncTask<String, Void, SubmitLinkActivity> {

        JsonParser parser = new JsonParser();

        protected SubmitLinkActivity doInBackground(String... urls) {

            URL url = null;
            try {
                url = new URL("http://www.reddit.com/api/submit");
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //

            Log.v(TAG, "OUR COOKIE IS " + Cookie);
            final EditText titleText = (EditText) findViewById(R.id.titleForm);
            final EditText linkText = (EditText) findViewById(R.id.linkForm);
            final EditText subRedditText = (EditText) findViewById(R.id.subredditForm);
            InputStream is;

            String modhash = settings.getString("modhash", "");
            String cookie = settings.getString("cookie", "");
            String cookieValue = settings.getString("cookieValue", "");

            String data = "?uh=" + modhash + "&kind=" + "link" + "&sr="
                    + subRedditText.getText().toString() + "&title="
                    + titleText.getText().toString() + "&url="
                    + linkText.getText().toString() + "&r="
                    + subRedditText.getText().toString() + "&renderstyle=html";

            Log.v(TAG, "OUR DATA IS " + data);
            HttpURLConnection ycConnection = null;
            try {
                ycConnection = (HttpURLConnection) url.openConnection();
                ycConnection.setRequestMethod("POST");
                ycConnection.setDoOutput(true);
                ycConnection.setUseCaches(false);
                ycConnection.setRequestProperty("Cookie", "reddit_session="
                        + cookie);
                ycConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8");
                ycConnection.setRequestProperty("Content-Length", String
                        .valueOf(data.length()));
                ycConnection.setDoInput(true);
                ycConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(ycConnection
                        .getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
                is = ycConnection.getInputStream();
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(is));
                String line;

                while ((line = rd.readLine()) != null) {
                    response += line;
                    response += '\r';
                }
                for (Entry<String, List<String>> r : ycConnection
                	.getHeaderFields().entrySet()) {
                    System.out.println(r.getKey() + ": " + r.getValue());
                }
                rd.close();
                System.out.println(response.toString());

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(SubmitLinkActivity feed) {

            Toast.makeText(getApplicationContext(), "submitted to reddit!",
                    Toast.LENGTH_LONG).show();
            finish();
            Intent myIntent = new Intent(getApplicationContext(),
                    RedditQuickSubmitActivity.class);
            startActivityForResult(myIntent, 0);
        }
    }

}
