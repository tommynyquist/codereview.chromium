package com.chrome.codereview;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getAction() == Intent.ACTION_VIEW) {
            int issueId = issueId(getIntent());
            if (issueId == -1) {
                failGracefully();
                return;
            }
            getIntent().putExtra(IssueDetailsFragment.EXTRA_ISSUE_ID, issueId);
        }
        setContentView(R.layout.activity_issue_detail);
    }

    private void failGracefully() {
        Toast.makeText(this, getString(R.string.fail_to_open_link), Toast.LENGTH_LONG).show();
        startOtherBestChoice();
        finish();
    }

    private int issueId(Intent intent) {
        List<String> segments = intent.getData().getPathSegments();
        if (segments.size() != 1) {
            return -1;
        }
        String issueId = segments.get(0);
        try {
            return Integer.parseInt(issueId);
        } catch (Exception e) {
            return -1;
        }
    }

    private void startOtherBestChoice() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(getIntent().getData());
        PackageManager packageManager = getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && !TextUtils.equals(resolveInfo.activityInfo.packageName, getPackageName())) {
            startActivity(intent);
            return;
        }

        List<ResolveInfo> resolved = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info: resolved) {
            String packageName = info.activityInfo.packageName;
            if (!TextUtils.equals(packageName, getPackageName())) {
                intent.setPackage(packageName);
                startActivity(intent);
                return;
            }
        }
    }

}
