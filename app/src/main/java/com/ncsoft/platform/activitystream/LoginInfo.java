package com.ncsoft.platform.activitystream;

/**
 * Created by hjlee on 2015-12-19.
 */
public class LoginInfo {
    private String mJiraUrl;
    private String mJiraStreamUrl;
    private String mJiraAuthUrl;
    private String mId;
    private String mPassword;
    private String mSession;

    private static LoginInfo mLoginInfo;

    private LoginInfo() {
    }

    public static LoginInfo getInstance() {
        if(mLoginInfo == null) {
            mLoginInfo = new LoginInfo();
        }
        return mLoginInfo;
    }

    public String getJiraUrl() {
        return mJiraUrl;
    }

    public String getJiraAuthUrl() {
        return mJiraAuthUrl;
    }

    public String getJiraStreamUrl() {
        return mJiraStreamUrl;
    }

    public String getId() {
        return mId;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getSession() {
        return mSession;
    }

    public void setJiraUrl(String url) {
        this.mJiraUrl = url;
    }

    public void setJiraAuthUrl(String url) {
        this.mJiraAuthUrl = url;
    }

    public void setJiraStreamUrl(String streamUrl) {
        this.mJiraStreamUrl = streamUrl;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public void setSession(String session) {
        this.mSession = session;
    }
}
