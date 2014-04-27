package com.chrome.codereview.utils;

import android.content.Context;

import com.chrome.codereview.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sergeyv on 26/4/14.
 */
public class DateUtils {

    private static final long MINUTE = 60 * 1000;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;
    private static final long MONTH = 30 * DAY;
    private static final long YEAR = 365 * DAY;

    private DateUtils() {
    }

    public static Date getDate(JSONObject jsonObject, String name) throws JSONException, ParseException {
        String lastModifiedString = jsonObject.getString(name);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = format.parse(lastModifiedString.substring(0, lastModifiedString.length() - 3));
        return date;
    }

    public static String createAgoText(Context context, Date lastModified) {
        long time = System.currentTimeMillis() - lastModified.getTime();
        long[] times = new long[] {YEAR, MONTH, WEEK, DAY, HOUR, MINUTE};
        int[] resources = new int[] {R.plurals.year, R.plurals.month, R.plurals.week, R.plurals.day, R.plurals.hour, R.plurals.minute};
        int i = 0;
        while (i < resources.length && time / times[i] == 0) {
            i++;
        }

        long quantity = i < resources.length ? time / times[i] : 0;
        i = i == resources.length ? resources.length - 1 : i;
        return quantity + " " + context.getResources().getQuantityString(resources[i], (int) quantity) + " " + context.getString(R.string.ago);
    }

}
