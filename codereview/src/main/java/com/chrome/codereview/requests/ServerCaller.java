package com.chrome.codereview.requests;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.chrome.codereview.CodereviewApplication;
import com.chrome.codereview.model.Issue;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by sergeyv on 16/4/14.
 */
public class ServerCaller {

    public enum State {
        OK,
        NEEDS_ACCOUNT,
        NEEDS_AUTHORIZATION;
    }

    private static final String AUTH_COOKIE_NAME = "SACSID";
    private static final String COOKIE_PREFERENCE = "ServerCaller_COOKIE_PREFERENCE";
    private static final String XSRF_TOKEN_PREFERENCE = "ServerCaller_XSRF_TOKEN_PREFERENCE";
    private static final Uri BASE_URL = Uri.parse("https://codereview.chromium.org");
    private static final String CHROMIUM_EMAIL = "@chromium.org";
    private static final String TOKEN_TYPE = "ah";

    private static final Uri SEARCH_URL = BASE_URL.buildUpon().appendPath("search").appendQueryParameter("format", "json").build();
    private static final Uri XSRF_URL = BASE_URL.buildUpon().appendPath("xsrf_token").build();
    private static final Uri AUTH_COOKIE_URL = BASE_URL.buildUpon().appendEncodedPath("_ah/login").appendQueryParameter("continue", "nowhere").build();
    private static final Uri ISSUE_API_URL = BASE_URL.buildUpon().appendPath("api").build();

    private final DefaultHttpClient httpClient;
    private Account chromiumAccount;
    private State state;
    private Context context;

    public ServerCaller(Context context) {
        this.context = context;
        httpClient = new DefaultHttpClient();
        reset();
    }

    public void reset() {
        initChromiumAccount();
        if (chromiumAccount == null) {
            state = State.NEEDS_ACCOUNT;
            clearCookieAndToken();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = sharedPreferences.getString(COOKIE_PREFERENCE, null);

        if (cookie == null) {
            state = State.NEEDS_AUTHORIZATION;
            clearCookieAndToken();
            return;
        }

        httpClient.getCookieStore().addCookie(new BasicClientCookie(AUTH_COOKIE_NAME, cookie));
        state = State.OK;
    }

    public static ServerCaller from(Context context) {
        return ((CodereviewApplication) context.getApplicationContext()).serverCaller();
    }

    public ServerCaller.State getState() {
        return state;
    }

    public String getAccountName() {
        return chromiumAccount.name;
    }

    public List<Issue> loadMineIssues() {
        if (chromiumAccount == null) {
            return Collections.emptyList();
        }
        return search(new SearchOptions.Builder().owner(chromiumAccount.name).withMessages().create());
    }

    public void tryToAuthenticate() throws UserRecoverableAuthException, GoogleAuthException, IOException, AuthenticationException {
        String token = GoogleAuthUtil.getToken(this.context, chromiumAccount.name, TOKEN_TYPE);
        loadAndSaveCookie(token);
        loadAndSaveXSRFToken();
    }

    private void initChromiumAccount() {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            if (accounts[i].name.endsWith(CHROMIUM_EMAIL)) {
                chromiumAccount = accounts[i];
            }
        }
    }

    private void loadAndSaveXSRFToken() throws IOException {
        HttpGet get = new HttpGet(XSRF_URL.toString());
        get.addHeader("X-Requesting-XSRF-Token", "");
        String xsrfToken = executeRequest(get);
        save(XSRF_TOKEN_PREFERENCE, xsrfToken);
    }

    private void loadAndSaveCookie(String authToken) throws AuthenticationException, IOException {
        String url = AUTH_COOKIE_URL.buildUpon().appendQueryParameter("auth", authToken).build().toString();
        HttpGet method = new HttpGet(url);
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        HttpResponse res = httpClient.execute(method);
        Header[] headers = res.getHeaders("Set-Cookie");
        if (res.getEntity() != null) {
            res.getEntity().consumeContent();
        }
        if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
            throw new AuthenticationException("Failed to get cookie");
        }

        for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
            if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                save(COOKIE_PREFERENCE, cookie.getValue());
                return;
            }
        }

        throw new AuthenticationException("Failed to get cookie");
    }

    public Issue loadIssue(int issueId) {
        Uri uri = ISSUE_API_URL.buildUpon().appendPath(issueId + "").appendQueryParameter("messages", "true").build();
        try {
            return Issue.fromJSONObject(executeGetJSONRequest(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Issue> search(SearchOptions options) {
        Uri.Builder builder = SEARCH_URL.buildUpon();
        options.fillParameters(builder);
        try {
            return Issue.fromJSONArray(executeGetJSONRequest(builder.build()).getJSONArray("results"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private String executeRequest(HttpUriRequest request) throws IOException {
        HttpResponse response = httpClient.execute(request);
        String entity = EntityUtils.toString(response.getEntity());
        return entity;
    }

    private JSONObject executeGetJSONRequest(Uri uri) throws IOException, JSONException {
        return new JSONObject(executeRequest(new HttpGet(uri.toString())));
    }


    private void save(String name, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(name, value).apply();
    }

    private void clearCookieAndToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove(COOKIE_PREFERENCE).remove(XSRF_TOKEN_PREFERENCE).apply();
    }

}
