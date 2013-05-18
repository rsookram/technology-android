package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ImageWebView extends Activity {
	protected static String URL;

	private static final String PREF_FILE = "WebViewSettings";
	private static final String DOUBLE_TAP_TOAST_COUNT = "double_tap_toast_count";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.ActionBarOverlay);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		WebView wv = new WebView(this);
		setContentView(wv);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		setTitle("/" + pref.getString("currentBoard", "g") + "/");

		disableDoubleTapToast();

		WebSettings ws = wv.getSettings();
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
		ws.setBuiltInZoomControls(true);
		ws.setDisplayZoomControls(pref.getBoolean("zoom_imageviewer", false));

		final Activity activity = this;
		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				activity.setProgress(newProgress * 100);
			}
		});
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				activity.getActionBar().hide();
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
