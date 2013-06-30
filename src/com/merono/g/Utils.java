package com.merono.g;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String makeGreenText(String toChange) {
        String greenTextRegex = "<span class=\"quote\">.*?</span>";
        Pattern pGreenText = Pattern.compile(greenTextRegex);
        Matcher mGreenText = pGreenText.matcher(toChange);

        int quoteCount = 0;
        while (mGreenText.find()) {
            quoteCount++;
        }

        mGreenText = pGreenText.matcher(toChange);

        for (int i = 0; i < quoteCount; i++) {
            if (mGreenText.find()) {
                String tmp = mGreenText.group();
                tmp = tmp.replaceAll("</?span( class=\"quote\")?>", "");
                tmp = tmp.replaceAll("&gt;", ">");
                toChange = toChange.replaceFirst(greenTextRegex, tmp);
                mGreenText = pGreenText.matcher(toChange);
            } else {
                break;
            }
        }
        return toChange;
    }
}