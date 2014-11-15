package com.chrome.codereview.model;

import android.text.TextUtils;

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
    private final String description;
    private final boolean closed;
    private final List<Message> messages;
    private final List<Reviewer> reviewers;
    private Date lastModified;
    private final List<PatchSet> patchSets;
    private final String ccd;
    private final int id;
    private final boolean isInCQ;

    public Issue(String owner, String subject, String description, boolean closed, List<Message> messages, List<Reviewer> reviewers, Date lastModified, List<PatchSet> patchSets, String ccd, int id, boolean isInCQ) {
        this.owner = owner;
        this.subject = subject;
        this.description = description;
        this.closed = closed;
        this.messages = messages;
        this.reviewers = reviewers;
        this.lastModified = lastModified;
        this.patchSets = patchSets;
        this.ccd = ccd;
        this.id = id;
        this.isInCQ = isInCQ;
    }

    public static Issue fromJSONObject(JSONObject jsonObject, List<PatchSet> patchSets) {
        try {
            String owner = jsonObject.getString("owner");
            String subject = jsonObject.getString("subject");
            String description = jsonObject.getString("description");
            boolean isClosed = jsonObject.getBoolean("closed");
            int issue = jsonObject.getInt("issue");
            List<Message> messages = jsonObject.has("messages") ? Message.from(jsonObject.getJSONArray("messages")) : Collections.<Message>emptyList();
            List<Reviewer> reviewers = Reviewer.from(jsonObject.getJSONArray("reviewers"), messages);
            Date lastModified = DateUtils.getDate(jsonObject, "modified");
            JSONArray ccJsonArray = jsonObject.getJSONArray("cc");
            List<String> ccList = new ArrayList<String>(ccJsonArray.length());
            for (int i = 0; i < ccJsonArray.length(); i++) {
                ccList.add(ccJsonArray.getString(i));
            }
            boolean isInCQ = jsonObject.getBoolean("commit");
            return new Issue(owner, subject, description, isClosed, messages, reviewers, lastModified, patchSets, TextUtils.join(", ", ccList), issue, isInCQ);
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

    public void setLastModified(long time) {
        this.lastModified = new Date(time);
    }

    public String subject() {
        return subject;
    }

    public String owner() {
        return owner;
    }

    public String description() {
        return description;
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

    public String ccdString() {
        return ccd;
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

    public boolean isInCQ() {
        return isInCQ;
    }
}

