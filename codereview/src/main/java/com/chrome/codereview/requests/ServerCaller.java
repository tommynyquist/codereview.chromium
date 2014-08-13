package com.chrome.codereview.requests;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.chrome.codereview.CodereviewApplication;
import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.Diff;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PublishData;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by sergeyv on 16/4/14.
 */
public class ServerCaller {

    public enum State {
        OK,
        NEEDS_ACCOUNT,
        NEEDS_AUTHORIZATION;
    }

    public static final Uri BASE_URL = Uri.parse("https://codereview.chromium.org");
    public static final Uri SECONDARY_URL = Uri.parse("https://chromiumcodereview.appspot.com");

    private static final String AUTH_COOKIE_NAME = "SACSID";
    private static final String XSRF_TOKEN_PREFERENCE = "ServerCaller_XSRF_TOKEN_PREFERENCE";
    private static final String XSRF_TOKEN_TIME_PREFERENCE = "ServerCaller_XSRF_TOKEN_TIME_PREFERENCE";
    private static final int TOKEN_LIFE_TIME = 25;//min
    private static final String CHROMIUM_EMAIL = "@chromium.org";
    private static final String TOKEN_TYPE = "ah";

    private static final Uri SEARCH_URL = BASE_URL.buildUpon().appendPath("search").appendQueryParameter("format", "json").build();
    private static final Uri XSRF_URL = BASE_URL.buildUpon().appendPath("xsrf_token").build();
    private static final Uri AUTH_COOKIE_URL = BASE_URL.buildUpon().appendEncodedPath("_ah/login").appendQueryParameter("continue", "nowhere").build();
    private static final Uri ISSUE_API_URL = BASE_URL.buildUpon().appendPath("api").build();
    private static final Uri INLINE_DRAFT = BASE_URL.buildUpon().appendPath("inline_draft").build();
    private static final String PUBLISH = "publish";
    private static final String ISSUE_PATH = "issue";
    private static final Uri DOWNLOAD_DIFF = BASE_URL.buildUpon().appendPath("download").build();
    private static final String COMMIT_PATH = "edit_flags";
    private static final int N_THREADS = 3;

    private final AndroidHttpClient httpClient;
    private final BasicHttpContext httpContext;
    private final ExecutorService service;
    private Account chromiumAccount;
    private State state;
    private Context context;

    public ServerCaller(Context context) {
        this.context = context;
        service = Executors.newFixedThreadPool(N_THREADS);
        httpClient = AndroidHttpClient.newInstance("");
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, new PersistentCookieStore(context));
        reset();
    }

    public void reset() {
        initChromiumAccount();
        if (chromiumAccount == null) {
            state = State.NEEDS_ACCOUNT;
            clearToken();
            return;
        }

        if (hasValidAuthCookie()) {
            state = State.OK;
        } else {
            state = State.NEEDS_AUTHORIZATION;
            clearToken();
        }
    }

    public static ServerCaller from(Context context) {
        return ((CodereviewApplication) context.getApplicationContext()).serverCaller();
    }

    public ServerCaller.State getState() {
        return state;
    }

    public String getAccountName() {
        if (chromiumAccount == null) {
            return null;
        }
       return chromiumAccount.name;
    }

    public List<Issue> loadIssuesForUser(String accountName) {
        if (accountName == null) {
            return null;
        }
        Future<List<Issue>> futureMineIssues = service.submit(createSearchCallable(new SearchOptions.Builder().owner(accountName).closeState(SearchOptions.CloseState.OPEN).withMessages().create()));
        Future<List<Issue>> futureCcIssues = service.submit(createSearchCallable(new SearchOptions.Builder().cc(accountName).closeState(SearchOptions.CloseState.OPEN).withMessages().create()));
        Future<List<Issue>> futureOnReviewIssues = service.submit(createSearchCallable(new SearchOptions.Builder().reviewer(accountName).closeState(SearchOptions.CloseState.OPEN).withMessages().create()));
        try {
            List<Issue> mineIssues = futureMineIssues.get();
            List<Issue> ccIssues = futureCcIssues.get();
            List<Issue> onReviewIssues = futureOnReviewIssues.get();
            mineIssues.addAll(ccIssues);
            mineIssues.addAll(onReviewIssues);
            return mineIssues;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<Issue>();
    }

    public void tryToAuthenticate() throws UserRecoverableAuthException, GoogleAuthException, IOException, AuthenticationException {
        String token = GoogleAuthUtil.getToken(this.context, chromiumAccount.name, TOKEN_TYPE);
        loadCookie(token);
        loadAndSaveXSRFToken();
    }

    private void initChromiumAccount() {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].name.endsWith(CHROMIUM_EMAIL)) {
                chromiumAccount = accounts[i];
            }
        }
    }

    public void publish(PublishData data) throws IOException, AuthenticationException, GoogleAuthException {
        Uri uri = BASE_URL.buildUpon().appendPath(data.issueId() + "").appendPath(PUBLISH).build();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("xsrf_token", getXsrfToken()));
        nameValuePairs.addAll(data.toList());
        executePost(uri, nameValuePairs);
    }

    public Diff loadDiff(int issueId, int patchSetId) throws IOException {
        HttpGet get = new HttpGet(DOWNLOAD_DIFF.buildUpon().appendPath(ISSUE_PATH + issueId + "_" + patchSetId + ".diff").build().toString());
        String diff = executeRequest(get);
        return new Diff(patchSetId, diff);
    }

    public FileDiff loadDiff(int issueId, int patchSetId, int patchId) throws IOException {
        HttpGet get = new HttpGet(DOWNLOAD_DIFF.buildUpon().appendPath(ISSUE_PATH + issueId + "_" + patchSetId + "_" + patchId + ".diff").build().toString());
        String diff = executeRequest(get);
        return FileDiff.from(diff);
    }

    private void loadAndSaveXSRFToken() throws IOException {
        HttpGet get = new HttpGet(XSRF_URL.toString());
        get.addHeader("X-Requesting-XSRF-Token", "");
        String xsrfToken = executeRequest(get);
        save(XSRF_TOKEN_PREFERENCE, xsrfToken);
        save(XSRF_TOKEN_TIME_PREFERENCE, System.currentTimeMillis());
    }

    private String getXsrfToken() throws IOException, GoogleAuthException, AuthenticationException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long timeDiff = System.currentTimeMillis() - preferences.getLong(XSRF_TOKEN_TIME_PREFERENCE, 0);
        if (!preferences.contains(XSRF_TOKEN_PREFERENCE) || TimeUnit.MINUTES.toMillis(TOKEN_LIFE_TIME) < timeDiff) {
            tryToAuthenticate();
        }
        return preferences.getString(XSRF_TOKEN_PREFERENCE, "");
    }

    private boolean hasValidAuthCookie() {
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals(AUTH_COOKIE_NAME) && !cookie.isExpired(new Date())) {
                state = State.OK;
                return true;
            }
        }
        return false;
    }

    private void loadCookie(String authToken) throws AuthenticationException, IOException {
        String url = AUTH_COOKIE_URL.buildUpon().appendQueryParameter("auth", authToken).build().toString();
        HttpGet method = new HttpGet(url);
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        HttpResponse res = httpClient.execute(method, httpContext);
        Header[] headers = res.getHeaders("Set-Cookie");
        if (res.getEntity() != null) {
            res.getEntity().consumeContent();
        }
        if (res.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY || headers.length == 0) {
            throw new AuthenticationException("Failed to get cookie");
        }

        if (!hasValidAuthCookie())
            throw new AuthenticationException("Failed to get cookie");
    }

    public PatchSet loadPatchSet(int issueId, int patchSetId) {
        Uri uri = ISSUE_API_URL.buildUpon().appendPath(issueId + "").appendPath(patchSetId + "").appendQueryParameter("comments", "true").build();
        try {
            return PatchSet.from(patchSetId, executeGetJSONRequest(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Issue loadIssueWithPatchSetData(final int issueId) {
        Uri uri = ISSUE_API_URL.buildUpon().appendPath(issueId + "").appendQueryParameter("messages", "true").build();
        try {
            JSONObject jsonObject = executeGetJSONRequest(uri);
            JSONArray patchSetsJson = jsonObject.getJSONArray("patchsets");
            List<Future<PatchSet>> futures = new ArrayList<Future<PatchSet>>(patchSetsJson.length());
            for (int i = 0; i < patchSetsJson.length(); i++) {
                final int patchSetId = patchSetsJson.getInt(i);
                futures.add(service.submit(new Callable<PatchSet>() {
                    @Override
                    public PatchSet call() throws Exception {
                        return loadPatchSet(issueId, patchSetId);
                    }
                }));
            }

            List<PatchSet> patchSets = new ArrayList<PatchSet>();
            for (Future<PatchSet> future : futures) {
                PatchSet patchSet = future.get();
                if (patchSet != null) {
                    patchSets.add(patchSet);
                }
            }
            return Issue.fromJSONObject(jsonObject, patchSets);
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

    public Callable<List<Issue>> createSearchCallable(final SearchOptions options) {
        return new Callable<List<Issue>>() {
            @Override
            public List<Issue> call() throws Exception {
                return search(options);
            }
        };
    }

    public void inlineDraft(int issueId, int patchSetId, int patchId, Comment comment) throws IOException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("side", comment.left() ? "a" : "b"));
        nameValuePairs.add(new BasicNameValuePair("snapshot", comment.left() ? "old" : "new"));
        nameValuePairs.add(new BasicNameValuePair("lineno", comment.line() + ""));
        nameValuePairs.add(new BasicNameValuePair("issue", issueId + ""));
        nameValuePairs.add(new BasicNameValuePair("patchset", patchSetId + ""));
        nameValuePairs.add(new BasicNameValuePair("patch", patchId + ""));
        nameValuePairs.add(new BasicNameValuePair("text", comment.text()));
        if (!TextUtils.isEmpty(comment.messageId())) {
            nameValuePairs.add(new BasicNameValuePair("message_id", comment.messageId()));
        }
        executePost(INLINE_DRAFT, nameValuePairs);
    }

    public void checkCQBit(int issueId, int patchSetId, boolean commit) throws GoogleAuthException, IOException, AuthenticationException {
        Uri uri = BASE_URL.buildUpon().appendPath(issueId + "").appendPath(COMMIT_PATH).build();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("xsrf_token", getXsrfToken()));
        nameValuePairs.add(new BasicNameValuePair("commit", commit ? "1" : "0"));
        nameValuePairs.add(new BasicNameValuePair("last_patchset", patchSetId + ""));
        executePost(uri, nameValuePairs);
    }

    private void executePost(Uri uri, List<? extends NameValuePair> parameters) throws IOException {
        HttpPost post = new HttpPost(uri.toString());
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        post.setEntity(formEntity);
        HttpResponse response = httpClient.execute(post, httpContext);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
    }

    private String executeRequest(HttpUriRequest request) throws IOException {
        request.addHeader("Accept-Encoding", "gzip");
        BasicHttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");
        request.setParams(params);
        HttpResponse response = httpClient.execute(request, httpContext);
        HttpEntity entity = response.getEntity();
        Header contentEncodingHeader = entity.getContentEncoding();
        if (contentEncodingHeader != null) {
            HeaderElement[] encodings = contentEncodingHeader.getElements();
            for (int i = 0; i < encodings.length; i++) {
                if (encodings[i].getName().equalsIgnoreCase("gzip")) {
                    entity = new GzipDecompressingEntity(entity);
                    break;
                }
            }
        }
        String entityString = EntityUtils.toString(entity);
        return entityString;
    }

    private JSONObject executeGetJSONRequest(Uri uri) throws IOException, JSONException {
        return new JSONObject(executeRequest(new HttpGet(uri.toString())));
    }

    private void save(String name, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(name, value).apply();
    }

    private void save(String name, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(name, value).apply();
    }

    private void clearToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove(XSRF_TOKEN_PREFERENCE).apply();
    }

}
