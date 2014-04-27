package com.chrome.codereview.utils;

import android.view.View;
import android.widget.TextView;

/**
 * Created by sergeyv on 22/4/14.
 */
public class ViewUtils {

    private ViewUtils() {}

    public static void setText(View view, int id, CharSequence text) {
        TextView textView = (TextView) view.findViewById(id);
        textView.setText(text);
    }
}
