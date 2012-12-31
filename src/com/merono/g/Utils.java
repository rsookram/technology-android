package com.merono.g;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";

	public static Bitmap getBitmapFromURL(String src) {
		Bitmap bitmap = null;
		try {
			URLConnection conn = new URL(src).openConnection();
			bitmap = BitmapFactory
					.decodeStream((InputStream) conn.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	public static String loadSite(String urlToLoad) {
		try {
			URL url = new URL(urlToLoad);
			URLConnection conn = url.openConnection();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()), 8 * 1024);
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			return sb.toString();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "FileNotFoundException: " + urlToLoad);
			return "nofile";
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "IOException");
			return "error";
		}
	}

	public static String replaceEntities(String s) {
		return s.replaceAll("&quot;", "\"").replaceAll("&gt;", ">")
				.replaceAll("&lt;", "<").replaceAll("&amp;", "&");
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

	public static String makeLinkQuote(String toChange) {
		String linkQuoteRegex = "<a .*?quotelink.*?</a>";
		Pattern pLinkQuote = Pattern.compile(linkQuoteRegex);
		Matcher mLinkQuote = pLinkQuote.matcher(toChange);

		int quoteCount = 0;
		while (mLinkQuote.find()) {
			quoteCount++;
		}

		mLinkQuote = pLinkQuote.matcher(toChange);

		for (int i = 0; i < quoteCount; i++) {
			if (mLinkQuote.find()) {
				String tmp = mLinkQuote.group();
				tmp = tmp.replaceAll("</a>", "");
				tmp = tmp.replaceAll("<a.+?\">", "");
				toChange = toChange.replaceFirst(linkQuoteRegex, tmp);
				mLinkQuote = pLinkQuote.matcher(toChange);
			} else {
				break;
			}
		}
		return toChange;
	}
}