package com.merono.g;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {
    public static String getCurrentBoard(Context context) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return pref.getString("currentBoard", "/g/");
    }

    public static String replaceEntities(String s) {
        return s.replaceAll("&quot;", "\"").replaceAll("&gt;", ">")
                .replaceAll("&lt;", "<").replaceAll("&amp;", "&")
                .replaceAll("&#039;", "'");
    }

    public static String cleanBoardName(String board) {
        if (board.startsWith("/") && board.endsWith("/")) {
            return board;
        } else {
            return "/" + board + "/";
        }
    }
}