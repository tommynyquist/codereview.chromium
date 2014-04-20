package com.chrome.codereview.utils;

/**
 * Created by sergeyv on 20/4/14.
 */
public class EmailUtils {

    private EmailUtils() {
    }

    public static String retrieveAccountName(String email) {
        int index = email.indexOf('@');
        if (index == -1) {
            return email;
        }
        return email.substring(0, index);
    }

}
