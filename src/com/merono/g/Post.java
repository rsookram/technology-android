package com.merono.g;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class Post {
	private String body;
	private String name;
	private String time;
	private String id;
	private String imgUrl;
	private String fullImgUrl;

	ArrayList<String> quotes = null;

	Post(JSONObject postJSON, String boardName) {
		try {
			body = postJSON.getString("com").replaceAll("<br>", "\n");

			// extract IDs of quotes
			quotes = new ArrayList<String>();
			Pattern quoteIDPattern = Pattern.compile("&gt;&gt;(.*?)<");
			Matcher m = quoteIDPattern.matcher(body);
			while (m.find()) {
				quotes.add(m.group(1));
			}

			body = body.replaceAll("</?.*?>", "");
			body = Utils.replaceEntities(body);
		} catch (JSONException e) {
			e.printStackTrace();
			body = "";
		}

		try {
			name = Utils.replaceEntities(postJSON.getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
			name = "no name";
		}

		try {
			time = postJSON.getString("now").replaceFirst("\\(.*?\\)", "  ");
		} catch (JSONException e) {
			e.printStackTrace();
			time = "now";
		}

		try {
			id = String.valueOf(postJSON.getInt("no"));
		} catch (JSONException e) {
			e.printStackTrace();
			id = "id error";
		}

		boolean hasImage = true;
		try {
			imgUrl = "https://thumbs.4chan.org/" + boardName + "/thumb/"
					+ postJSON.getLong("tim") + "s.jpg";
			fullImgUrl = "https://images.4chan.org/" + boardName + "/src/"
					+ postJSON.getLong("tim") + postJSON.getString("ext");
		} catch (JSONException e) {
			imgUrl = "";
			fullImgUrl = "";
			hasImage = false;
		}

		if (hasImage) {
			name = name + " <img>";
		}
	}

	public String getText() {
		return body;
	}

	public String getName() {
		return name;
	}

	public String getTime() {
		return time;
	}

	public String getId() {
		return id;
	}

	public String getImgURL() {
		return imgUrl;
	}

	public String getFullImgUrl() {
		return fullImgUrl;
	}

	public boolean hasImgUrl() {
		return !imgUrl.equals("");
	}

	public boolean hasFullImgUrl() {
		return !fullImgUrl.equals("");
	}

	@Override
	public String toString() {
		return body;
	}
}
