package com.vasilinets.chrome.codereview;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

public class LoginActivity extends Activity {

    private static final String AUTH_COOKIE_NAME = "SACSID";
    private static final String CHROMIUM_EMAIL = "@chromium.org";
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String TOKEN_TYPE = "ah";

    private static final int ADD_GOOGLE_ACCOUNT = 1;
    private static final int GOOGLE_ACCOUNT_PERMISSION = 2;
    public static final String COOKIE_PREFERENCE = "cookie";

    private class GetCookieAsyncTask extends AsyncTask<String, Void, Void> {

        public static final String AUTH_COOKIE_URL = "https://codereview.chromium.org/_ah/login?continue=localhost&auth=";

        @Override
        protected Void doInBackground(String... param) {
            String authToken = param[0];
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet method = new HttpGet(AUTH_COOKIE_URL + authToken);
            final HttpParams getParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(getParams, false);
            method.setParams(getParams);

            try {
                HttpResponse res = client.execute(method);
                Header[] headers = res.getHeaders("Set-Cookie");
                if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
                    return null;
                }

                for (Cookie cookie : client.getCookieStore().getCookies()) {
                    if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                        saveCookie(cookie.getValue());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void saveCookie(String cookie) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            preferences.edit().putString(COOKIE_PREFERENCE, cookie).commit();
        }

    }

    private Account chromiumAccount = null;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_review_login);
        String cookie = PreferenceManager.getDefaultSharedPreferences(this).getString(COOKIE_PREFERENCE, null);
        accountManager = AccountManager.get(this);
        if (cookie == null) {
            authenticate();
        } else  {
            TextView textView = (TextView) findViewById(R.id.cookie);
            textView.setText(cookie);
        }
    }

    private void authenticate() {
        initializeChromiumAccount();
        if (chromiumAccount == null) {
            startAddGoogleAccountIntent();
        } else {
            getToken();
        }
    }

    private void startAddGoogleAccountIntent() {
        Intent addAccountIntent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        addAccountIntent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{GOOGLE_ACCOUNT_TYPE});
        this.startActivityForResult(addAccountIntent, LoginActivity.ADD_GOOGLE_ACCOUNT);
    }

    private void getToken() {
        accountManager.getAuthToken(chromiumAccount, TOKEN_TYPE, null, this, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle authTokenBundle = future.getResult();
                    Intent intent = (Intent) authTokenBundle.get(AccountManager.KEY_INTENT);
                    if (intent != null) {
                        startActivityForResult(intent, LoginActivity.GOOGLE_ACCOUNT_PERMISSION);
                        return;
                    }
                    String authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
                    GetCookieAsyncTask task = new GetCookieAsyncTask();
                    task.execute(authToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_GOOGLE_ACCOUNT:
                authenticate();
                break;
            case GOOGLE_ACCOUNT_PERMISSION:
                //FIXME: support disagreement
                getToken();
                break;
        }
    }

    private void initializeChromiumAccount() {
        Account[] accounts = accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            if (accounts[i].name.endsWith(CHROMIUM_EMAIL)) {
                chromiumAccount = accounts[i];
            }
        }
    }

}
