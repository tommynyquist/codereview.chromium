package com.vasilinets.chrome.codereview;

import android.app.Application;

import com.vasilinets.chrome.codereview.model.AccountSettings;

/**
 * Created by sergeyv on 13/4/14.
 */
public class CodereviewApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountSettings.initializeSettings(this);

    }
}
