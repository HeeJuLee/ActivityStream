package com.ncsoft.platform.activitystream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class IssueActivity extends Activity {
    private String mUrl;
    private WebView mWebView;
    private LoginInfo mLoginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);

        mLoginInfo = LoginInfo.getInstance();
        mUrl = getIntent().getData().toString();

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setCookie(mLoginInfo.getJiraUrl(), "JSESSIONID=" + mLoginInfo.getSession());

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        ///final TextView titleTextView = (TextView) findViewById(R.id.title_text_view);

        progressBar.setMax(100);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

            public void onReceivedTitle(WebView webView, String title) {
               // titleTextView.setText(title);
            }
        });

        mWebView.loadUrl(mUrl);
    }

}
