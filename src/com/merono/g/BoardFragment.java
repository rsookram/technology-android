package com.merono.g;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class BoardFragment extends ListFragment {
    private ArrayList<String> threadLinks = new ArrayList<String>(15);
    private ArrayList<Post> posts = new ArrayList<Post>(15);
    private PostAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View arg1,
                                           int position, long arg3) {
                Post selected = (Post) av.getItemAtPosition(position);
                if (selected.hasFullImgUrl()) {
                    String imgUrl = selected.getFullImgUrl();
                    ImageWebView.openImageWebView(getActivity(), imgUrl);
                    return true;
                }
                return false;
            }
        });
    }

    public void setData(ArrayList<Post> data, ArrayList<String> links) {
        posts.clear();
        posts.addAll(data);

        threadLinks.clear();
        threadLinks.addAll(links);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (threadLinks.get(position) != null) {
            Intent i = new Intent(getActivity(), ThreadActivity.class);
            i.putExtra("URL", threadLinks.get(position));
            startActivity(i);
        }
    }
}
