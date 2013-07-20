package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ImageWebViewFragment extends Fragment {
    private Activity activity;

    private static ImageWebViewFragment newInstance(String url) {
        ImageWebViewFragment frag = new ImageWebViewFragment();
        Bundle b = new Bundle();
        b.putString("URL", url);
        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_web_view, container, false);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        WebView wv = (WebView) v.findViewById(R.id.web_view);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

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
                progressBar.setVisibility(View.GONE);
            }
        });

        wv.loadUrl(getArguments().getString("URL"));

        return v;
    }

    private void disableDoubleTapToast() {
        final String PREF_FILE = "WebViewSettings";
        final String DOUBLE_TAP_TOAST_COUNT = "double_tap_toast_count";

        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FILE,
                Context.MODE_PRIVATE);
        if (prefs.getInt(DOUBLE_TAP_TOAST_COUNT, 1) > 0) {
            prefs.edit().putInt(DOUBLE_TAP_TOAST_COUNT, 0).commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.getActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.getActionBar().show();
    }

    public static void openImageWebView(FragmentActivity activity, String url) {
        ImageWebViewFragment frag = newInstance(url);

        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(android.R.id.content, frag).addToBackStack(null).commit();
    }
}
