package com.chrome.codereview;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffActivity extends Activity {

    public static final String PATH_EXTRA = "PATH_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_diff);
        getActionBar().setTitle(Uri.parse(getIntent().getStringExtra(PATH_EXTRA)).getLastPathSegment());
    }
}
