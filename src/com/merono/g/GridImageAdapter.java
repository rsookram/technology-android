package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

public class GridImageAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<String> thumbs;

    public GridImageAdapter(Activity a, ArrayList<String> imgs) {
        mActivity = a;
        thumbs = imgs;
    }

    public int getCount() {
        return (thumbs == null) ? 0 : thumbs.size();
    }

    public String getItem(int position) {
        return thumbs.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout fl = (FrameLayout) convertView;
        NetworkImageView niv;
        if (fl == null) {
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fl = (FrameLayout) inflater.inflate(R.layout.grid_image, parent,
                    false);
            niv = (NetworkImageView) fl.findViewById(R.id.niv_grid);

            niv.setDefaultImageResId(R.drawable.ic_icon);
            niv.setErrorImageResId(android.R.drawable.ic_dialog_alert);
        }
        niv = (NetworkImageView) fl.findViewById(R.id.niv_grid);

        GApplication appState = (GApplication) mActivity.getApplication();
        niv.setImageUrl(getItem(position), appState.mImageLoader);
        return fl;
    }
}