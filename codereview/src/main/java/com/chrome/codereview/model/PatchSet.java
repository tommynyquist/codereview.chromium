package com.chrome.codereview.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sergeyv on 21/4/14.
 */
public class PatchSet {

    private final String message;
    private final List<PatchSetFile> files;
    private final int numComments;
    private final int patchSetId;
    private final List<TryBotResult> tryBotResults;

    public PatchSet(String message, List<PatchSetFile> files, int numComments, int patchSetId, List<TryBotResult> tryBotResults) {
        this.numComments = numComments;
        this.patchSetId = patchSetId;
        this.tryBotResults = Collections.unmodifiableList(tryBotResults);
        this.message = message != null ? message : "";
        this.files = files;
    }

    public static PatchSet from(int patchSetId, JSONObject jsonObject) throws JSONException, ParseException {
        JSONObject filesJsonObject = jsonObject.getJSONObject("files");
        List<PatchSetFile> files = new ArrayList<PatchSetFile>();
        for (Iterator iterator = filesJsonObject.keys(); iterator.hasNext(); ) {
            String file = (String) iterator.next();
            JSONObject meta = filesJsonObject.getJSONObject(file);
            files.add(PatchSetFile.from(file, meta));
        }
        String message = !jsonObject.isNull("message") ? jsonObject.getString("message") : null;
        int numComments = jsonObject.getInt("num_comments");
        List<TryBotResult> tryJobResults = TryBotResult.from(jsonObject.getJSONArray("try_job_results"));
        return new PatchSet(message, files, numComments, patchSetId, tryJobResults);
    }

    public String message() {
        return message;
    }

    public List<PatchSetFile> files() {
        return files;
    }

    public int linesAdded() {
        int result = 0;
        for (PatchSetFile file : files()) {
            result += file.numAdded();
        }
        return result;
    }

    public int linesRemoved() {
        int result = 0;
        for (PatchSetFile file : files()) {
            result += file.numRemoved();
        }
        return result;
    }

    public int numComments() {
        return numComments;
    }

    public int numDrafts() {
        int drafts = 0;
        for (PatchSetFile file: files()) {
            drafts += file.numberOfDrafts();
        }
        return drafts;
    }

    public int id() {
        return patchSetId;
    }

    public List<TryBotResult> tryBotResults() {
        return tryBotResults;
    }

    public boolean hasTryBotsResults() {
        return !tryBotResults.isEmpty();
    }
}
