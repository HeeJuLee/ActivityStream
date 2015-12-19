package com.ncsoft.platform.activitystream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ncsoft.platform.activitystream.StreamParser.Entry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StreamActivity extends Activity {

    private final String TAG_REQUEST = "TAG_ACTIVITY_STREAM";
    private LoginInfo mLoginInfo;
    private ListView mListView;
    private EntryAdapter mEntryAdapter;
    private List<Entry> mEntries;
    private RequestQueue mVolleyQueue;
    boolean mLastFlag = false;
    private String mLastId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        mEntries = new ArrayList<Entry>();
        mLoginInfo = LoginInfo.getInstance();
        mVolleyQueue = Volley.newRequestQueue(this);

        mListView = (ListView) findViewById(R.id.entry_list);
        mListView.setOnItemClickListener(mItemClickListener);
        mListView.setOnScrollListener(mScrollListener);
        mEntryAdapter = new EntryAdapter(this);
        mListView.setAdapter(mEntryAdapter);

        requestActivityStream(makeAvtivityStreamUrl(mLoginInfo.getJiraStreamUrl(), null, null));
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Entry entry = mEntries.get(position);
            Uri url = Uri.parse(mLoginInfo.getmJiraIssueWebUrl() + entry.getIssueKey());

            Intent i = new Intent(StreamActivity.this, IssueActivity.class);
            i.setData(url);
            startActivity(i);
        }
    };

    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mLastFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mLastFlag) {
                Toast.makeText(StreamActivity.this, "more", Toast.LENGTH_SHORT).show();

                String lastUpdateDate = null;
                if (mEntries.size() > 0) {
                    Entry entry = mEntries.get(mEntries.size() - 1);
                    lastUpdateDate = entry.getUpdated();
                    mLastId = entry.getId();
                }

                requestActivityStream(makeAvtivityStreamUrl(mLoginInfo.getJiraStreamUrl(), null, lastUpdateDate));
            }
        }
    };

    private String makeAvtivityStreamUrl(String url, String projectKey, String updateDate) {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?providers=issues");
        if(projectKey != null)
            sb.append("&streams=key+IS+").append(projectKey);
        if(updateDate != null) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.KOREA);
                Date date = df.parse(updateDate);
                long unixtime = date.getTime();
                sb.append("&streams=update-date+BEFORE+").append(unixtime);
            } catch(ParseException e) {
                Toast.makeText(StreamActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        return sb.toString();
    }

    private void requestActivityStream(String activityStreamUrl) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, activityStreamUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parsingActivityStream(response);
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(StreamActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("cookie", "JSESSIONID=" + mLoginInfo.getSession());

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
                headers.put("cookie", "JSESSIONID=" + mLoginInfo.getSession());

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
            if(entries.size() > 0 && mLastId != null)
            {
                // Jira ActivityStream에서 update-date 파라미터를 밀리세컨까지 제대로 지원하지 않고 있음
                // 따라서 맨 마지막 entry가 다시 오는 경우가 있음. 이를 체크해서 나오지 않도록 함.
                if(mLastId.equals(entries.get(0).getId()))
                    entries.remove(0);
            }
            mEntries.addAll(entries);
            mEntryAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                holder.updateDate = (TextView) convertView.findViewById(R.id.entry_update_date);
                holder.issueKey = (TextView) convertView.findViewById(R.id.entry_issue_key_summary);
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

            String imageUrl = mEntries.get(position).getAuthorImageLink();
            getAvataImage(imageUrl, holder.image);
            holder.authorName.setText(mEntries.get(position).getAuthorName());

            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.KOREA);
                Date date = df.parse(mEntries.get(position).getUpdated());
                df = new SimpleDateFormat("MM/dd");
                holder.updateDate.setText(df.format(date));
            } catch(ParseException e) {
                Toast.makeText(StreamActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            String issueKeySummary = mEntries.get(position).getIssueKey() + " " + mEntries.get(position).getIssueSummary();
            holder.issueKey.setText(issueKeySummary);

            if(mEntries.get(position).getTargetIssueWebLink() != null)
                holder.issueWebLink = mEntries.get(position).getTargetIssueWebLink();
            else
                holder.issueWebLink = mEntries.get(position).getObjectIssueWebLink();
            holder.content.loadData(mEntries.get(position).getContent(), "text/html; charset=UTF-8", null);

            return convertView;
        }

        class ViewHolder {
            ImageView image;
            TextView authorName;
            TextView updateDate;
            TextView issueKey;
            String issueWebLink;
            WebView content;
        }

    }

}