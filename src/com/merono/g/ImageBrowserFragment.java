package com.merono.g;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import java.util.ArrayList;

public class ImageBrowserFragment extends Fragment {
    private ArrayList<String> thumbImgUrls;
    private ArrayList<String> fullImgUrls;

    private GridView grid;
    private GridImageAdapter gridImageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        grid = (GridView) inflater.inflate(R.layout.image_browser_layout,
                container, false);

        gridImageAdapter = new GridImageAdapter(getActivity(), thumbImgUrls);
        grid.setAdapter(gridImageAdapter);

        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                String imgUrl = fullImgUrls.get(position);
                ImageWebView.openImageWebView(getActivity(), imgUrl);
            }
        });
        return grid;
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
}
