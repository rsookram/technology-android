package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ImageWebView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        setTitle(pref.getString("currentBoard", "/g/"));
        setContentView(R.layout.image_web_view);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        WebView wv = (WebView) findViewById(R.id.web_view);

        WebSettings ws = wv.getSettings();
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(pref.getBoolean("zoom_imageviewer", false));

        disableDoubleTapToast();

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ImageWebView.this.getActionBar().hide();
                progressBar.setVisibility(View.GONE);
            }
        });

        wv.loadUrl(getIntent().getStringExtra("URL"));
    }

    private void disableDoubleTapToast() {
        final String PREF_FILE = "WebViewSettings";
        final String DOUBLE_TAP_TOAST_COUNT = "double_tap_toast_count";

        SharedPreferences prefs = getSharedPreferences(PREF_FILE,
                Context.MODE_PRIVATE);
        if (prefs.getInt(DOUBLE_TAP_TOAST_COUNT, 1) > 0) {
            prefs.edit().putInt(DOUBLE_TAP_TOAST_COUNT, 0).commit();
        }
    }

    public static void openImageWebView(Context context, String url) {
        Intent intent = new Intent(context, ImageWebView.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }
}
