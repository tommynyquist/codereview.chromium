package com.chrome.codereview.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by sergeyv on 21/4/14.
 */
public class PatchSetFile {

    enum Status {
        ADDED,
        MODIFIED,
    }

    private final Status status;
    private final String path;
    private final int numAdded;
    private final int numRemoved;

    public PatchSetFile(Status status, String path, int numAdded, int numRemoved) {
        this.status = status;
        this.path = path;
        this.numAdded = numAdded;
        this.numRemoved = numRemoved;
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

    public static PatchSetFile from(String path, JSONObject metaData) throws JSONException {
        int numAdded = metaData.getInt("num_added");
        int numRemoved = metaData.getInt("num_removed");
        String statusString = metaData.getString("status");
        Status status = null;
        if (TextUtils.equals(statusString, "M")) {
            status = Status.MODIFIED;
        } else if (TextUtils.equals(statusString, "A")) {
            status = Status.ADDED;
        }
        if (status == null) {
            throw new IllegalArgumentException("Unknown status: " + statusString);
        }
        return new PatchSetFile(status, path, numAdded, numRemoved);
    }
}
