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
	private static final String TAG = "ImageWebView";
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
		this.setTitle("/" + pref.getString("currentBoard", "g") + "/");

		// this disables the toast that shows the hint to double-tap
		SharedPreferences prefs = getSharedPreferences(PREF_FILE,
				Context.MODE_PRIVATE);
		if (prefs.getInt(DOUBLE_TAP_TOAST_COUNT, 1) > 0) {
			prefs.edit().putInt(DOUBLE_TAP_TOAST_COUNT, 0).commit();
		}

		WebView img = (WebView) findViewById(R.id.browser);
		img.getSettings().setLoadWithOverviewMode(true);
		img.getSettings().setUseWideViewPort(true);
		img.getSettings().setBuiltInZoomControls(true);

		final Activity activity = this;
		img.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				activity.setProgress(newProgress * 100);
			}
		});

		img.loadUrl(getIntent().getStringExtra(URL));
	}
}
