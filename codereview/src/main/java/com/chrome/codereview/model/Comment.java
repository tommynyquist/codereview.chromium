package com.chrome.codereview.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 27/4/14.
 */
public class Comment {

    private final String text;

    public Comment(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public static Comment from(JSONObject jsonObject) throws JSONException {
        String text = jsonObject.getString("text");
        return new Comment(text);
    }

    public static List<Comment> from(JSONArray jsonArray) throws JSONException {
        ArrayList<Comment> comments = new ArrayList<Comment>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            comments.add(from(jsonArray.getJSONObject(i)));
        }
        return comments;
    }
}
