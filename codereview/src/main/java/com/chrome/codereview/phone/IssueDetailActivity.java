package com.chrome.codereview.phone;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.chrome.codereview.R;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_issue_detail);
    }
}
