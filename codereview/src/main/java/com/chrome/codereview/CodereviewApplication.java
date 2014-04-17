package com.chrome.codereview;

import android.app.Application;

import com.chrome.codereview.requests.ServerCaller;

/**
 * Created by sergeyv on 13/4/14.
 */
public class CodereviewApplication extends Application {

    private ServerCaller serverCaller;

    @Override
    public void onCreate() {
        super.onCreate();
        serverCaller = new ServerCaller(this);
    }

    public ServerCaller serverCaller() {
        return serverCaller;
    }
}
