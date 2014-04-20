package com.chrome.codereview.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sergeyv on 13/4/14.
 */
public class Issue {

    private final String owner;
    private final String subject;
    private final boolean closed;
    private final List<Message> messages;
    private final List<Reviewer> reviewers;
    private final Date lastModified;
    private final int id;

    public Issue(String owner, String subject, boolean closed, List<Message> messages, List<Reviewer> reviewers, Date lastModified, int id) {
        this.owner = owner;
        this.subject = subject;
        this.closed = closed;
        this.messages = messages;
        this.reviewers = reviewers;
        this.lastModified = lastModified;
        this.id = id;
    }

    public static Issue fromJSONObject(JSONObject jsonObject) {
        try {
            String owner = jsonObject.getString("owner");
            String subject = jsonObject.getString("subject");
            boolean isClosed = jsonObject.getBoolean("closed");
            int issue = jsonObject.getInt("issue");
            List<Message> messages = jsonObject.has("messages") ? Message.from(jsonObject.getJSONArray("messages")) : Collections.<Message>emptyList();
            List<Reviewer> reviewers = Reviewer.from(jsonObject.getJSONArray("reviewers"), messages);
            String lastModifiedString = jsonObject.getString("modified");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date lastModified = format.parse(lastModifiedString);
            return new Issue(owner, subject, isClosed, messages, reviewers, lastModified, issue);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Issue> fromJSONArray(JSONArray jsonArray) {
        int length = jsonArray.length();
        ArrayList<Issue> result = new ArrayList<Issue>(length);
        for (int i = 0; i < length; i++) {
            try {
                result.add(fromJSONObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public String subject() {
        return subject;
    }

    public String owner() {
        return owner;
    }

    public List<Message> messages() {
        return messages;
    }

    public List<Reviewer> reviewers() {
        return reviewers;
    }

    public int id() {
        return id;
    }

    public boolean isClosed() {
        return closed;
    }

    public Date lastModified() {
        return lastModified;
    }
}
