package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class ImageWebView extends Activity {
	protected static String URL;

	private static final String PREF_FILE = "WebViewSettings";
	private static final String DOUBLE_TAP_TOAST_COUNT = "double_tap_toast_count";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageweb_layout);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		setTitle("/" + pref.getString("currentBoard", "g") + "/");

		disableDoubleTapToast();

		WebView wv = (WebView) findViewById(R.id.browser);
		wv.getSettings().setLoadWithOverviewMode(true);
		wv.getSettings().setUseWideViewPort(true);
		wv.getSettings().setBuiltInZoomControls(true);

		final Activity activity = this;
		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				activity.setProgress(newProgress * 100);
			}
		});

		wv.loadUrl(getIntent().getStringExtra(URL));
	}

	private void disableDoubleTapToast() {
		SharedPreferences prefs = getSharedPreferences(PREF_FILE,
				Context.MODE_PRIVATE);
		if (prefs.getInt(DOUBLE_TAP_TOAST_COUNT, 1) > 0) {
			prefs.edit().putInt(DOUBLE_TAP_TOAST_COUNT, 0).commit();
		}
	}
}
