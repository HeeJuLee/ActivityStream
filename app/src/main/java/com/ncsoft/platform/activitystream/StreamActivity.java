package com.ncsoft.platform.activitystream;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class StreamActivity extends AppCompatActivity {

    private RequestQueue mVolleyQueue;
    private final String TAG_REQUEST = "TAG_ACTIVITY_STREAM";

    private TextView mResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        mResultTextView = (TextView) findViewById(R.id.result_text_view);

        try {
            JSONObject jsonObject = new JSONObject(getIntent().getStringExtra("SESSION_RESULT"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        mVolleyQueue = Volley.newRequestQueue(this);

        getActivityStream();
        getRomeRss();
    }


    private void getRomeRss() {
        /*
        feedUrl = new URL(rss);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));
        List entries = feed.getEntries();
        Toast.makeText(this, "#Feeds retrieved: " + entries.size(), Toast.LENGTH_SHORT).show();

        Iterator iterator = entries.listIterator();
        while (iterator.hasNext())
        {
            SyndEntry ent = (SyndEntry) iterator.next();
            String title = ent.getTitle();
            adapter.add(title);
        }
        adapter.notifyDataSetChanged();
        */
    }


    private void getActivityStream() {

        String url = "http://172.20.49.215:8080/activity";

        Uri.Builder builder = Uri.parse(url).buildUpon();
        /*
        builder.appendQueryParameter("api_key", "75ee6c644cad38dc8e53d3598c8e6b6c");
        builder.appendQueryParameter("method", "flickr.interestingness.getList");
        builder.appendQueryParameter("format", "json");
        builder.appendQueryParameter("nojsoncallback", "1");
        */

        StringRequest stringRequest = new StringRequest(Request.Method.GET, builder.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mResultTextView.setText(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle your error types accordingly.For Timeout & No connection error, you can show 'retry' button.
                        // For AuthFailure, you can re login with user credentials.
                        // For ClientError, 400 & 401, Errors happening on client side when sending api request.
                        // In this case you can check how client is forming the api and debug accordingly.
                        // For ServerError 5xx, you can do retry or handle accordingly.
                        if (error instanceof NetworkError) {
                        } else if (error instanceof ClientError) {
                        } else if (error instanceof ServerError) {
                        } else if (error instanceof AuthFailureError) {
                        } else if (error instanceof ParseError) {
                        } else if (error instanceof NoConnectionError) {
                        } else if (error instanceof TimeoutError) {
                        }

                        mResultTextView.setText(error.getMessage());
                    }
                }
        );

        stringRequest.setShouldCache(true);
        stringRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(stringRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}