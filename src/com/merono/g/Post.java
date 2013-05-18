package com.merono.g;

import java.util.ArrayList;

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

			quotes = Utils.extractQuotes(body);

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
			name = "";
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

		try {
			imgUrl = "https://thumbs.4chan.org/" + boardName + "/thumb/"
					+ postJSON.getLong("tim") + "s.jpg";
			fullImgUrl = "https://images.4chan.org/" + boardName + "/src/"
					+ postJSON.getLong("tim") + postJSON.getString("ext");
		} catch (JSONException e) {
			imgUrl = "";
			fullImgUrl = "";
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

	public static ArrayList<Post> getQuotedPosts(ArrayList<String> ids,
			ArrayList<Post> posts) {
		ArrayList<Post> quotedPosts = new ArrayList<Post>();
		for (String quoteId : ids) {
			for (Post post : posts) {
				if (post.getId().equals(quoteId)) {
					quotedPosts.add(post);
					break;
				}
			}
		}

		return quotedPosts;
	}

	@Override
	public String toString() {
		return body;
	}
}
