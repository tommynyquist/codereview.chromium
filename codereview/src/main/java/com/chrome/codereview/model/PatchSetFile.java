package com.chrome.codereview.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 21/4/14.
 */
public class PatchSetFile implements Parcelable {

    public enum Status {
        ADDED("A"),
        MODIFIED("M"),
        DELETED("D"),
        MOVED_MODIFIED("A +") {
            @Override
            public String toString() {
                return "A+";
            }
        };

        private final String text;

        Status(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final Parcelable.Creator<PatchSetFile> CREATOR = new Creator<PatchSetFile>() {

        public PatchSetFile createFromParcel(Parcel source) {
            int id = source.readInt();
            Status status = fromString(source.readString());
            String path = source.readString();
            int numAdded = source.readInt();
            int numRemoved = source.readInt();
            List<Comment> comments = new ArrayList<Comment>();
            source.readTypedList(comments, Comment.CREATOR);
            return new PatchSetFile(id, status, path, numAdded, numRemoved, comments);
        }

        @Override
        public PatchSetFile[] newArray(int size) {
            return new PatchSetFile[size];
        }

    };

    private final int id;
    private final Status status;
    private final String path;
    private final int numAdded;
    private final int numRemoved;
    private final List<Comment> comments;
    private final int numberOfDrafts;

    public PatchSetFile(int id, Status status, String path, int numAdded, int numRemoved, List<Comment> comments) {
        this.id = id;
        this.status = status;
        this.path = path;
        this.numAdded = numAdded;
        this.numRemoved = numRemoved;
        this.comments = comments;

        int numberOfDrafts = 0;
        for (Comment comment : comments) {
            numberOfDrafts += comment.isDraft() ? 1 : 0;
        }
        this.numberOfDrafts = numberOfDrafts;
    }

    public int id() {
        return id;
    }

    public Status status() {
        return status;
    }

    public String path() {
        return path;
    }

    public int numAdded() {
        return numAdded;
    }

    public int numRemoved() {
        return numRemoved;
    }

    public int numberOfDrafts() {
        return numberOfDrafts;
    }

    public List<Comment> comments() {
        return this.comments;
    }

    public int numberOfComments() {
        return this.comments.size() - numberOfDrafts;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status.text);
        dest.writeString(path);
        dest.writeInt(numAdded);
        dest.writeInt(numRemoved);
        dest.writeTypedList(comments);
    }

    public static PatchSetFile from(String path, JSONObject metaData) throws JSONException, ParseException {
        int id = metaData.getInt("id");
        int numAdded = metaData.getInt("num_added");
        int numRemoved = metaData.getInt("num_removed");
        Status status = fromString(metaData.getString("status"));
        List<Comment> comments = Comment.from(metaData.getJSONArray("messages"));
        return new PatchSetFile(id, status, path, numAdded, numRemoved, comments);
    }

    private static Status fromString(String statusString) {
        Status status = null;
        for (Status s : Status.values()) {
            if (TextUtils.equals(s.text, statusString)) {
                status = s;
                break;
            }
        }
        if (status == null) {
            throw new IllegalArgumentException("Unknown status: " + statusString);
        }
        return status;
    }
}
