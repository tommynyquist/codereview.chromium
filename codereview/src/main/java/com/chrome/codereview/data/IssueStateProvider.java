package com.chrome.codereview.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class IssueStateProvider extends ContentProvider {

    public static final String COLUMN_ISSUE_ID = "ISSUE_ID";
    public static final String COLUMN_MODIFICATION_TIME = "TIME";

    private static final String HIDDEN_TABLE = "hidden_issues";

    private static final String SQL_CREATE_HIDDEN_ISSUE_TABLE = "CREATE TABLE " +
            HIDDEN_TABLE + " " +                      // Table's name
            "(" +                           // The columns in the table
            " _ID INTEGER PRIMARY KEY, " +
            COLUMN_ISSUE_ID + " INTEGER," +
            COLUMN_MODIFICATION_TIME + " INTEGER" +
            ")";


    private static final String AUTHORITY = "com.chromium.codereview.issue.state";

    private static final int HIDDEN_CODE = 1;
    private static final String HIDDEN = "hidden";
    public static final Uri HIDDEN_ISSUES_URI = Uri.parse("content://" + AUTHORITY + "/" + HIDDEN);

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, HIDDEN, HIDDEN_CODE);
    }

    private static class DBHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "issue.db";
        private static final int VERSION = 1;

        public DBHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_HIDDEN_ISSUE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private DBHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case HIDDEN_CODE:
                database.insert(HIDDEN_TABLE, null, values);
                return uri;
        }

        throw new IllegalArgumentException("Unknown uri " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case HIDDEN_CODE:
                return database.query(HIDDEN_TABLE, projection, selection, selectionArgs, null, null, null);
        }

        throw new IllegalArgumentException("Unknown uri " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Update is not supported");
    }
}
