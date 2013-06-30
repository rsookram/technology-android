package com.merono.g;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class ImageBrowserFragment extends Fragment {
    private ArrayList<String> thumbImgUrls = null;
    private ArrayList<String> fullImgUrls = null;

    private GridView grid;
    private GridImageAdapter gridImageAdapter;

    public ImageBrowserFragment() {
        setRetainInstance(true);
    }

    public void setData(ArrayList<String> thumbImgUrls,
            ArrayList<String> fullImgUrls) {
        if (this.thumbImgUrls != null && this.fullImgUrls != null) {
            this.thumbImgUrls.clear();
            this.fullImgUrls.clear();
            this.thumbImgUrls.addAll(thumbImgUrls);
            this.fullImgUrls.addAll(fullImgUrls);
            gridImageAdapter.notifyDataSetChanged();
        } else {
            this.thumbImgUrls = new ArrayList<String>(thumbImgUrls);
            this.fullImgUrls = new ArrayList<String>(fullImgUrls);
        }
        gridImageAdapter = new GridImageAdapter(getActivity(), thumbImgUrls);
        if (grid != null) {
            grid.setAdapter(gridImageAdapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        grid = (GridView) inflater.inflate(R.layout.image_browser_layout,
                container, false);

        gridImageAdapter = new GridImageAdapter(getActivity(), thumbImgUrls);
        grid.setAdapter(gridImageAdapter);

        final Intent intent = new Intent(getActivity(), ImageWebView.class);
        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                String selected = fullImgUrls.get(position);
                intent.putExtra("URL", selected);
                startActivity(intent);
            }
        });
        return grid;
    }
}
