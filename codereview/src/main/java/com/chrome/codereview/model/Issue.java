package com.chrome.codereview.model;

import com.chrome.codereview.utils.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    private final List<PatchSet> patchSets;
    private final int id;

    public Issue(String owner, String subject, boolean closed, List<Message> messages, List<Reviewer> reviewers, Date lastModified, List<PatchSet> patchSets, int id) {
        this.owner = owner;
        this.subject = subject;
        this.closed = closed;
        this.messages = messages;
        this.reviewers = reviewers;
        this.lastModified = lastModified;
        this.patchSets = patchSets;
        this.id = id;
    }

    public static Issue fromJSONObject(JSONObject jsonObject, List<PatchSet> patchSets) {
        try {
            String owner = jsonObject.getString("owner");
            String subject = jsonObject.getString("subject");
            boolean isClosed = jsonObject.getBoolean("closed");
            int issue = jsonObject.getInt("issue");
            List<Message> messages = jsonObject.has("messages") ? Message.from(jsonObject.getJSONArray("messages")) : Collections.<Message>emptyList();
            List<Reviewer> reviewers = Reviewer.from(jsonObject.getJSONArray("reviewers"), messages);
            Date lastModified = DateUtils.getDate(jsonObject, "modified");
            return new Issue(owner, subject, isClosed, messages, reviewers, lastModified, patchSets, issue);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Issue fromJSONObject(JSONObject jsonObject) {
        return fromJSONObject(jsonObject, Collections.<PatchSet>emptyList());
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

    public List<PatchSet> patchSets() {
        return patchSets;
    }
}

