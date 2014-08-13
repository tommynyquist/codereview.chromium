package com.chrome.codereview;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;

import com.chrome.codereview.data.IssueStateProvider;
import com.chrome.codereview.requests.ServerCaller;

public class CleanUpService extends IntentService {

    private static final String ALREADY_SCHEDULED = "CleanUpService_ALREADY_STARTED";

    public static void scheduleCleanUp(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean alreadyScheduled = preferences.getBoolean(ALREADY_SCHEDULED, false);
        if (alreadyScheduled) {
            return;
        }
        preferences.edit().putBoolean(ALREADY_SCHEDULED, true).apply();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getService(context, 0, new Intent(context, CleanUpService.class), 0);
        //just in case
        alarmManager.cancel(intent);
        alarmManager.setInexactRepeating(AlarmManager.RTC, AlarmManager.INTERVAL_DAY * 7, AlarmManager.INTERVAL_DAY * 7, intent);
    }

    public CleanUpService() {
        super("clean up thread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        cleanUpClosedIssues();
    }

    private void cleanUpClosedIssues() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(IssueStateProvider.HIDDEN_ISSUES_URI, null, null, null, null);
        SparseBooleanArray issuesIds = new SparseBooleanArray();
        int issueIdColumn = cursor.getColumnIndex(IssueStateProvider.COLUMN_ISSUE_ID);
        while (cursor.moveToNext()) {
            issuesIds.put(cursor.getInt(issueIdColumn), true);
        }
        cursor.close();
        ServerCaller serverCaller = ServerCaller.from(getApplicationContext());
        for (int i = 0; i < issuesIds.size(); i++) {
            int issueId = issuesIds.keyAt(i);
            if (serverCaller.isClosedOrDeleted(issueId)) {
                contentResolver.delete(IssueStateProvider.HIDDEN_ISSUES_URI, IssueStateProvider.COLUMN_ISSUE_ID + "=?", new String[]{issueId + ""});
            }
        }
    }
}
