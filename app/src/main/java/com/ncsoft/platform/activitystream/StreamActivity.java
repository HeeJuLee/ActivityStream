package com.ncsoft.platform.activitystream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ncsoft.platform.activitystream.StreamParser.Entry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamActivity extends AppCompatActivity {

    private final String TAG_REQUEST = "TAG_ACTIVITY_STREAM";
    private final String HOME_TEST_ACTIVITY_URL = "http://192.168.0.6:8080/activity";
    private final String NC_TEST_ACTIVITY_URL = "http://172.20.49.215:8080/activity";
    private final String NC_JIRA_ACTIVITY_URL = "http://jira.korea.ncsoft.corp/activity";
    //private final String FILTER_PARAM = "?maxResults=10&streams=key+IS+OSDT&providers=issues&os_authType=basic&title=Activity+Stream";
    private final String FILTER_PARAM = "?maxResults=50&providers=issues&os_authType=basic&title=Activity+Stream";

    private ListView mListView;
    private EntryAdapter mEntryAdapter;

    private JSONObject mSession;
    private List<Entry> mEntries;
    private RequestQueue mVolleyQueue;
    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        mEntries = new ArrayList<Entry>();
        mEntryAdapter = new EntryAdapter(this);
        mListView = (ListView) findViewById(R.id.entry_list);
        mListView.setAdapter(mEntryAdapter);

        try {
            mSession = new JSONObject(getIntent().getStringExtra("SESSION_RESULT"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize Volley Request Queue
        mVolleyQueue = Volley.newRequestQueue(this);

        getActivityStream(HOME_TEST_ACTIVITY_URL + FILTER_PARAM);
    }

    private void getActivityStream(String strActivityUrl) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, strActivityUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parsingActivityStream(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StreamActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();

                try {
                    String value = mSession.getJSONObject("session").getString("value");
                    headers.put("cookie", "JSESSIONID=" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return headers;
            }
        };

        stringRequest.setShouldCache(true);
        stringRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(stringRequest);
    }

    private void getAvataImage(String imageUrl, final ImageView imageView) {

        ImageRequest imgRequest = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        }, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                imageView.setImageResource(R.drawable.defalut_avatar_normal);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();

                try {
                    String value = mSession.getJSONObject("session").getString("value");
                    headers.put("cookie", "JSESSIONID=" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return headers;
            }
        };

        imgRequest.setShouldCache(true);
        imgRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(imgRequest);
    }

    private void parsingActivityStream(String response) {
        try {
            StreamParser streamParser = new StreamParser();
            List<Entry> entries = streamParser.parse(response);
            mEntries.addAll(entries);
            mEntryAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private class EntryAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public EntryAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            if(mEntries == null)
                return 0;
            else
                return mEntries.size();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_entry, null);

                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.entry_image);
                holder.authorName = (TextView) convertView.findViewById(R.id.entry_author_name);
                holder.authorId = (TextView) convertView.findViewById(R.id.entry_author_id);
                holder.issueKey = (TextView) convertView.findViewById(R.id.entry_issue_key);
                holder.issueSummary = (TextView) convertView.findViewById(R.id.entry_issue_summary);
                holder.content = (WebView) convertView.findViewById(R.id.entry_content);
                holder.content.setHorizontalScrollBarEnabled(false);
                holder.content.setVerticalScrollBarEnabled(false);
                holder.content.setBackgroundColor(0);
                //holder.content.getSettings().setJavaScriptEnabled(true);
                holder.content.getSettings().setDefaultFontSize(10);
                holder.content.getSettings().setDefaultTextEncodingName("UTF-8");


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.content.loadData(mEntries.get(position).getContent(), "text/html; charset=UTF-8", null);

            String imageUrl = mEntries.get(position).getAuthorImageLink();
            getAvataImage(imageUrl, holder.image);
            holder.authorName.setText(mEntries.get(position).getAuthorName());
            //holder.authorId.setText(mEntries.get(position).getAuthorId());

            String issueKey, issueSummary;
            if(mEntries.get(position).getTargetIssueKey() != null)
                issueKey = mEntries.get(position).getTargetIssueKey();
            else
                issueKey = mEntries.get(position).getObjectIssueKey();
            if(mEntries.get(position).getTargetIssueSummary() != null)
                issueSummary = mEntries.get(position).getTargetIssueSummary();
            else
                issueSummary = mEntries.get(position).getObjectIssueSummary();
            holder.issueKey.setText(issueKey + "  " + issueSummary);

            if(mEntries.get(position).getTargetIssueWebLink() != null)
                holder.issueWebLink = mEntries.get(position).getTargetIssueWebLink();
            else
                holder.issueWebLink = mEntries.get(position).getObjectIssueWebLink();

            return convertView;
        }

        class ViewHolder {
            ImageView image;
            TextView authorName;
            TextView authorId;
            TextView issueKey;
            TextView issueSummary;
            String issueWebLink;
            WebView content;
        }

    }

}