package com.chrome.codereview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chrome.codereview.utils.EmailUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 27/4/14.
 */
public class Comment implements Parcelable {

    private final boolean isDraft;
    private final String text;
    private final String authorEmail;

    public static final Parcelable.Creator<Comment> CREATOR =
            new Parcelable.Creator<Comment>() {

                public Comment createFromParcel(Parcel source) {
                    String text = source.readString();
                    String authorEmail = source.readString();
                    boolean isDraft = source.readInt() == 1;
                    return new Comment(false, text, authorEmail);
                }

                @Override
                public Comment[] newArray(int size) {
                    return new Comment[size];
                }


            };

    public Comment(boolean isDraft, String text, String author) {
        this.isDraft = isDraft;
        this.text = text;
        this.authorEmail = author;
    }

    public String text() {
        return text;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public String author() {
        return EmailUtils.retrieveAccountName(authorEmail);
    }

    public static Comment from(JSONObject jsonObject) throws JSONException {
        String text = jsonObject.getString("text");
        boolean isDraft = jsonObject.getBoolean("draft");
        String authorEmail = jsonObject.getString("author_email");
        return new Comment(isDraft, text, authorEmail);
    }

    public static List<Comment> from(JSONArray jsonArray) throws JSONException {
        ArrayList<Comment> comments = new ArrayList<Comment>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            comments.add(from(jsonArray.getJSONObject(i)));
        }
        return comments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(authorEmail);
        dest.writeInt(isDraft ? 1 : 0);
    }

}
