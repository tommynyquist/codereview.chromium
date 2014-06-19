package com.chrome.codereview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.EmailUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by sergeyv on 27/4/14.
 */
public class Comment implements Parcelable {

    private final boolean isDraft;
    private final String text;
    private final String authorEmail;
    private final int line;
    private final boolean left;
    private final Date date;
    private final String messageId;

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {

        public Comment createFromParcel(Parcel source) {
            String text = source.readString();
            String authorEmail = source.readString();
            boolean isDraft = source.readInt() == 1;
            int line = source.readInt();
            boolean left = source.readInt() == 1;
            Date date = new Date(source.readLong());
            String messageId = source.readString();
            return new Comment(isDraft, text, authorEmail, line, left, date, messageId);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }

    };

    public Comment(boolean isDraft, String text, String author, int line, boolean left, Date date, String messageId) {
        this.isDraft = isDraft;
        this.text = text;
        this.authorEmail = author;
        this.line = line;
        this.left = left;
        this.date = date;
        this.messageId = messageId;
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

    public boolean left() {
        return left;
    }

    public int line() {
        return line;
    }

    public Date date() {
        return date;
    }

    public String messageId() {
        return messageId;
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
        dest.writeInt(line);
        dest.writeInt(left ? 1 : 0);
        dest.writeLong(date.getTime());
        dest.writeString(messageId);
    }

    public static Comment createDraft(String text, int line, boolean left, String messageId) {
        return new Comment(true, text, "", line, left, new Date(), messageId);
    }

    public static Comment from(JSONObject jsonObject) throws JSONException, ParseException {
        String text = jsonObject.getString("text");
        boolean isDraft = jsonObject.getBoolean("draft");
        String authorEmail = jsonObject.getString("author_email");
        int line = jsonObject.getInt("lineno");
        boolean left = jsonObject.getBoolean("left");
        Date date = DateUtils.getDate(jsonObject, "date");
        String messageId = jsonObject.getString("message_id");
        return new Comment(isDraft, text, authorEmail, line, left, date, messageId);
    }

    public static List<Comment> from(JSONArray jsonArray) throws JSONException, ParseException {
        ArrayList<Comment> comments = new ArrayList<Comment>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            comments.add(from(jsonArray.getJSONObject(i)));
        }
        return comments;
    }

    public static String quote(Comment comment) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String header = "On " + format.format(comment.date()) + ", " + comment.author() + " wrote:\n";
        StringBuilder builder = new StringBuilder(header);
        StringTokenizer tokenizer = new StringTokenizer(comment.text(), "\n");
        while (tokenizer.hasMoreTokens()) {
            builder.append("> ");
            builder.append(tokenizer.nextToken());
            builder.append("\n");
        }
        return builder.toString();
    }
}
