package com.ncsoft.platform.activitystream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class LoginActivity extends Activity {
    private final String TAG_REQUEST = "TAG_LOGIN";
    private static final String JIRA_AUTH_PATH = "/rest/auth/1/session";
    private static final String JIRA_ACTIVITY_Stream_PATH = "/activity";
    private static final String JIRA_ISSUE_WEB_PATH = "/browse/";

    private EditText mJiraUrlView;
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private RequestQueue mVolleyQueue;
    private LoginInfo mLoginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(R.string.login_page);

        mLoginInfo = LoginInfo.getInstance();

        mJiraUrlView = (EditText) findViewById(R.id.jira_url);
        mUserNameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mJiraUrlView.getText().toString();
                if (TextUtils.isEmpty(url)) {
                    mJiraUrlView.setError(getString(R.string.field_required));
                    mJiraUrlView.requestFocus();
                    return;
                }

                String id = mUserNameView.getText().toString();
                if (TextUtils.isEmpty(id)) {
                    mUserNameView.setError(getString(R.string.field_required));
                    mUserNameView.requestFocus();
                    return;
                }

                String password = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError(getString(R.string.field_required));
                    mPasswordView.requestFocus();
                    return;
                }

                showProgress(true);

                mLoginInfo.setJiraUrl(url);
                mLoginInfo.setJiraAuthUrl(url + JIRA_AUTH_PATH);
                mLoginInfo.setJiraStreamUrl(url + JIRA_ACTIVITY_Stream_PATH);
                mLoginInfo.setJiraIssueWebUrl(url + JIRA_ISSUE_WEB_PATH);
                mLoginInfo.setId(id);
                mLoginInfo.setPassword(password);

                startJiraAuthLogin();
            }
        });


        mVolleyQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mVolleyQueue.cancelAll(TAG_REQUEST);
    }

    public void startJiraAuthLogin() {

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, mLoginInfo.getJiraAuthUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String session = response.getJSONObject("session").getString("value");
                    mLoginInfo.setSession(session);
                } catch (Exception e) {
                    showProgress(false);
                    showToast(e.getMessage());
                }

                Intent i = new Intent(LoginActivity.this, StreamActivity.class);
                startActivity(i);
                showProgress(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
                showToast(error.getMessage());
            }
        }) {
            @Override
            public byte[] getBody() {

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", mLoginInfo.getId());
                jsonObject.addProperty("password", mLoginInfo.getPassword());

                return jsonObject.toString().getBytes();
            }
        };
        jsonRequest.setShouldCache(true);
        jsonRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(jsonRequest);
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
