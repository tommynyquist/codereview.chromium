package com.chrome.codereview.model;

import android.content.Context;

import com.chrome.codereview.R;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.EmailUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sergeyv on 18/4/14.
 */
public class Message {

    public enum Decoration {
        LGTM(R.color.scheme_green) {
            @Override
            public boolean appliesTo(Message message) {
                return message.approval;
            }
        },
        NOT_LGTM(R.color.scheme_red) {
            @Override
            public boolean appliesTo(Message message) {
                return message.disapproval;
            }
        },
        COMMITTED(R.color.scheme_blue) {

            private static final String COMMIT_BY_BOT = "Change committed as ";
            private static final String COMMIT_MANUALLY = "Committed patchset";

            @Override
            public boolean appliesTo(Message message) {
                return message.text().startsWith(COMMIT_BY_BOT) || message.text().startsWith(COMMIT_MANUALLY);
            }
        };

        private final String prefix;
        private final int color;

        Decoration(int color) {
            this(null, color);
        }

        Decoration(String prefix, int color) {
            this.prefix = prefix;
            this.color = color;
        }

        public int color(Context context) {
            return context.getResources().getColor(color);
        }

        public boolean appliesTo(Message message) {
            return prefix != null && message.text().startsWith(prefix);
        }
    }

    private final String text;
    private final String senderEmail;
    private final String sender;
    private final Date date;
    private final boolean approval;
    private final boolean disapproval;

    public Message(String text, String senderEmail, Date date, boolean approval, boolean disapproval) {
        this.text = text;
        this.senderEmail = senderEmail;
        this.date = date;
        this.approval = approval;
        this.disapproval = disapproval;
        this.sender = EmailUtils.retrieveAccountName(senderEmail);
    }

    public String sender() {
        return this.sender;
    }

    public String senderEmail() {
        return senderEmail;
    }

    public String text() {
        return text;
    }

    public Date date() {
        return date;
    }

    public Decoration decoration() {
        for (Decoration decoration : Decoration.values()) {
            if (decoration.appliesTo(this)) {
                return decoration;
            }
        }
        return null;
    }

    public static Message from(JSONObject jsonObject) throws JSONException, ParseException {
        String sender = jsonObject.getString("sender");
        String text = jsonObject.getString("text");
        Date date = DateUtils.getDate(jsonObject, "date");
        boolean approval = jsonObject.getBoolean("approval");
        boolean disapproval = jsonObject.getBoolean("disapproval");
        return new Message(text, sender, date, approval, disapproval);
    }

    public static List<Message> from(JSONArray jsonArray) {
        ArrayList<Message> result = new ArrayList<Message>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                result.add(from(jsonArray.getJSONObject(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
