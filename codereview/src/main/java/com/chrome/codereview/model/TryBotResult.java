package com.chrome.codereview.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 24/6/14.
 */
public class TryBotResult {

    public enum Result {
        SUCCESS,
        FAILURE,
        WAITING,
        RUNNING
    }

    private final String url;
    private final Result result;
    private final String builder;

    public TryBotResult(String url, int result, String builder) {
        this.url = url;
        switch (result) {
            case 0:
            case 1:
                this.result = Result.SUCCESS;
                break;
            case 2:
                this.result = Result.FAILURE;
                break;
            case -1:
                this.result = Result.RUNNING;
                break;
            default:
                throw new IllegalArgumentException("Unknown result: " + result);
        }
        this.builder = builder;
    }

    public String url() {
        return url;
    }

    public Result result() {
        return result;
    }

    public String builder() {
        return builder;
    }

    public static TryBotResult from(JSONObject jsonObject) throws JSONException {
        String url = jsonObject.getString("url");
        String builder = jsonObject.getString("builder");
        int result = jsonObject.getInt("result");
        return new TryBotResult(url, result, builder);
    }

    public static List<TryBotResult> from(JSONArray jsonArray) throws JSONException {
        List<TryBotResult> results = new ArrayList<TryBotResult>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            results.add(from(jsonArray.getJSONObject(i)));
        }
        return results;
    }

    @Override
    public String toString() {
        return builder;
    }
}
