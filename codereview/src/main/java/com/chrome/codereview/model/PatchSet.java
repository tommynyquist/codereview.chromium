package com.chrome.codereview.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sergeyv on 21/4/14.
 */
public class PatchSet {

    private final String message;
    private final List<PatchSetFile> files;
    private final int numComments;

    public PatchSet(String message, List<PatchSetFile> files, int numComments) {
        this.numComments = numComments;
        this.message = message != null ? message : "";
        this.files = files;
    }

    public static PatchSet from(JSONObject jsonObject) throws JSONException {
        JSONObject filesJsonObject = jsonObject.getJSONObject("files");
        List<PatchSetFile> files = new ArrayList<PatchSetFile>();
        for (Iterator iterator = filesJsonObject.keys(); iterator.hasNext(); ) {
            String file = (String) iterator.next();
            JSONObject meta = filesJsonObject.getJSONObject(file);
            files.add(PatchSetFile.from(file, meta));
        }
        String message = !jsonObject.isNull("message") ? jsonObject.getString("message") : null;
        int numComments = jsonObject.getInt("num_comments");
        return new PatchSet(message, files, numComments);
    }

    public String message() {
        return message;
    }

    public List<PatchSetFile> files() {
        return files;
    }

    public int linesAdded() {
        int result = 0;
        for (PatchSetFile file: files()) {
            result += file.numAdded();
        }
        return result;
    }

    public int linesRemoved() {
        int result = 0;
        for (PatchSetFile file: files()) {
            result += file.numRemoved();
        }
        return result;
    }

    public int numComments() {
        return numComments;
    }
}
