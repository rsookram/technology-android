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

	private ArrayList<String> quoteIds;

	public Post(JSONObject postJSON, String boardName) {
		try {
			body = postJSON.getString("com").replaceAll("<br>", "\n");

			quoteIds = extractQuoteIds(body);

			body = body.replaceAll("</?.*?>", "");
			body = Utils.replaceEntities(body);
		} catch (JSONException e) {
			body = "";
			quoteIds = null;
		}

		try {
			name = Utils.replaceEntities(postJSON.getString("name"));
		} catch (JSONException e) {
			name = "";
		}

		try {
			// remove day of the week
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
			imgUrl = "https://thumbs.4chan.org" + boardName + "thumb/"
					+ postJSON.getLong("tim") + "s.jpg";
			fullImgUrl = "https://images.4chan.org" + boardName + "src/"
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

	public ArrayList<String> getQuoteIds() {
		return quoteIds;
	}

	public boolean hasImgUrl() {
		return !imgUrl.isEmpty();
	}

	public boolean hasFullImgUrl() {
		return !fullImgUrl.isEmpty();
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

	public static ArrayList<Post> getImagePosts(ArrayList<Post> posts) {
		ArrayList<Post> imagePosts = new ArrayList<Post>();
		for (Post post : posts) {
			if (post.hasImgUrl()) {
				imagePosts.add(post);
			}
		}
		return imagePosts;
	}

	// extract IDs of quotes
	private static ArrayList<String> extractQuoteIds(String body) {
		ArrayList<String> quotes = new ArrayList<String>();

		Pattern quoteIDPattern = Pattern.compile("&gt;&gt;(.*?)<");
		Matcher m = quoteIDPattern.matcher(body);
		while (m.find()) {
			quotes.add(m.group(1));
		}

		return quotes;
	}
}
