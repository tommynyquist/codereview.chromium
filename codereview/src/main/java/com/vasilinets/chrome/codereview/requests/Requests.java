package com.vasilinets.chrome.codereview.requests;

import android.net.Uri;

import com.vasilinets.chrome.codereview.model.AccountSettings;
import com.vasilinets.chrome.codereview.model.Issue;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by sergeyv on 13/4/14.
 */
public class Requests {

    private static final String SEARCH_URL = "https://codereview.chromium.org/search?format=json";

    public static List<Issue> getMineIssues() {
        String username = AccountSettings.get().getAccountName();
        if (username == null) {
            return Collections.emptyList();
        }
        return search(new SearchOptions.Builder().owner(username).create());
    }

    private static List<Issue> search(SearchOptions options) {
        Uri.Builder builder = Uri.parse(SEARCH_URL).buildUpon();
        options.fillParameters(builder);
        HttpGet get = new HttpGet(builder.build().toString());
        HttpClient client = new DefaultHttpClient();

        try {
            HttpResponse response = client.execute(get);
            String entity = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(entity);
            return Issue.fromJSONArray(jsonObject.getJSONArray("results"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
