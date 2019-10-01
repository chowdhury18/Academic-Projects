package com.g10.chit_chat.chatapp.utils;

import android.text.TextUtils;
import android.widget.TextView;

public abstract class StringUtils {
    public static void setUsername(TextView textView, String username) {
        textView.setText(capitalize(username));
    }

    public static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
