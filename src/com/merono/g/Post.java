package com.merono.g;

import org.json.JSONException;
import org.json.JSONObject;

public class Post {
	private String body;
	private String name;
	private String time;
	private String id;
	private String imgUrl;
	private String fullImgUrl;

	Post(JSONObject postJSON, String boardName) {
		try {
			body = postJSON.getString("com").replaceAll("<br>", "\n");
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
			id = "No." + postJSON.getInt("no");
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

	@Override
	public String toString() {
		return body;
	}
}
