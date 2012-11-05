package com.merono.g;

import org.jsoup.nodes.Element;

import android.util.Log;

public class Post {
	private String body;
	private String name;
	private String time;
	private String id;
	private String imgUrl;
	private String fullImgUrl;

	private static final String TAG = "Post";

	Post(Element postAsHtml) {
		Element rawHtml = postAsHtml;
		try {
			Element bodyHtml = rawHtml.select("blockquote").first();
			Element nameHtml = rawHtml.getElementsByClass("name").first();
			Element timeHtml = rawHtml.getElementsByClass("dateTime").first();

			body = bodyHtml.text().replaceAll("br2n ?", "\n");
			name = nameHtml.text();
			String[] timeAndId = timeHtml.text().split("br2n");
			time = timeAndId[0];
			id = timeAndId[1];
		} catch (Exception e) {
			Log.d(TAG, "ぬるぽ");
			body = "Error. Reload page";
			name = "Anonymous";
			id = "ぬるぽ";
		}

		try {
			Element thumbHtml = rawHtml.select("img[src]").first();
			imgUrl = "http:" + thumbHtml.attr("src");
			fullImgUrl = "http:"
					+ rawHtml.getElementsByClass("fileThumb").first()
							.attr("href");
		} catch (Exception e) {
			imgUrl = "";
			fullImgUrl = "";
		}

		if (!fullImgUrl.equals("")) {
			name = name + " <Img>";
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
