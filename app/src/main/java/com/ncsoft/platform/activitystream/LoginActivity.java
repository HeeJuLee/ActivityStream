package com.ncsoft.platform.activitystream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText mJiraUrlView;
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String mJiraUrl;
    private String mUserName;
    private String mPassword;

    private RequestQueue mVolleyQueue;

    private final String TAG_REQUEST = "TAG_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mJiraUrlView = (EditText) findViewById(R.id.jira_url);
        mUserNameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mJiraUrl = mJiraUrlView.getText().toString();
        mUserName = mUserNameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        Button mLogInButton = (Button) findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginJsonRequest();
            }
        });

        mVolleyQueue = Volley.newRequestQueue(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    @Override
    public void onStop() {
        super.onStop();

        mVolleyQueue.cancelAll(TAG_REQUEST);
    }

    public boolean checkValidate() {

        if (TextUtils.isEmpty(mJiraUrl)) {
            mJiraUrlView.setError(getString(R.string.field_required));
            mJiraUrlView.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mUserName)) {
            mUserNameView.setError(getString(R.string.field_required));
            mUserNameView.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.field_required));
            mPasswordView.requestFocus();
            return false;
        }

        return true;
    }

    public void startLoginJsonRequest() {

        if(checkValidate() == false)
            return;

        showProgress(true);

        String authUrl = mJiraUrl + "/rest/auth/1/session";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, authUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showProgress(false);
                        showToast("SUCCESS: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if( error instanceof NetworkError) {
                        } else if( error instanceof ClientError) {
                        } else if( error instanceof ServerError) {
                        } else if( error instanceof AuthFailureError) {
                        } else if( error instanceof ParseError) {
                        } else if( error instanceof NoConnectionError) {
                        } else if( error instanceof TimeoutError) {
                        }

                        showProgress(false);
                        showToast(error.getMessage());
                    }
                })
        {
            @Override
            public byte[] getBody() {

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", mUserName);
                jsonObject.addProperty("password", mPassword);

                VolleyLog.d(jsonObject.toString());

                return jsonObject.toString().getBytes();
            }
        };

        jsonRequest.setShouldCache(true);
        jsonRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(jsonRequest);
    }

    public void startLoginStringRequest() {

        if(checkValidate() == false)
            return;

        showProgress(true);

        String authUrl = mJiraUrl + "/rest/auth/1/session";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, authUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showProgress(false);
                        showToast(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if( error instanceof NetworkError) {
                        } else if( error instanceof ClientError) {
                        } else if( error instanceof ServerError) {
                        } else if( error instanceof AuthFailureError) {
                        } else if( error instanceof ParseError) {
                        } else if( error instanceof NoConnectionError) {
                        } else if( error instanceof TimeoutError) {
                        }

                        showProgress(false);
                        showToast(error.getMessage());
                        VolleyLog.d(error.getMessage());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("username", mUserName);
                params.put("password", mPassword);

                return params;
            }
        };

        stringRequest.setShouldCache(true);
        stringRequest.setTag(TAG_REQUEST);
        mVolleyQueue.add(stringRequest);
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
