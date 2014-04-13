package com.vasilinets.chrome.codereview;

import android.app.Activity;
import android.os.Bundle;

public class IssuesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);

        getFragmentManager().beginTransaction().add(R.id.issues_main, new IssuesListFragment()).commit();
    }

}
