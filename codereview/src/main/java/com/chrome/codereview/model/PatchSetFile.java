package com.chrome.codereview.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by sergeyv on 21/4/14.
 */
public class PatchSetFile {

    public enum Status {
        ADDED,
        MODIFIED,
        DELETED,
    }

    private final Status status;
    private final String path;
    private final int numAdded;
    private final int numRemoved;
    private final List<Comment> comments;

    public PatchSetFile(Status status, String path, int numAdded, int numRemoved, List<Comment> comments) {
        this.status = status;
        this.path = path;
        this.numAdded = numAdded;
        this.numRemoved = numRemoved;
        this.comments = comments;
    }

    public Status status() {
        return status;
    }

    public String path() {
        return path;
    }

    public int added() {
        return numAdded;
    }

    public int numAdded() {
        return numAdded;
    }

    public int numRemoved() {
        return numRemoved;
    }

    public int numberOfComments() {
        return this.comments.size();
    }

    public static PatchSetFile from(String path, JSONObject metaData) throws JSONException {
        int numAdded = metaData.getInt("num_added");
        int numRemoved = metaData.getInt("num_removed");
        String statusString = metaData.getString("status");
        Status status = null;
        if (TextUtils.equals(statusString, "M")) {
            status = Status.MODIFIED;
        } else if (TextUtils.equals(statusString, "A")) {
            status = Status.ADDED;
        } else if (TextUtils.equals(statusString, "D")) {
            status = Status.DELETED;
        }
        if (status == null) {
            throw new IllegalArgumentException("Unknown status: " + statusString);
        }
        List<Comment> comments = Comment.from(metaData.getJSONArray("messages"));
        return new PatchSetFile(status, path, numAdded, numRemoved, comments);
    }
}
