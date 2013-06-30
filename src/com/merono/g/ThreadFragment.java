package com.merono.g;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ThreadFragment extends Fragment {
    private ArrayList<Post> posts = new ArrayList<Post>(1);
    private PostAdapter adapter;

    public ThreadFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.thread_layout, container, false);

        ListView lv = (ListView) view.findViewById(R.id.list);
        lv.setAdapter(adapter);
        setupOnClickListeners(lv);

        return view;
    }

    public void setData(ArrayList<Post> data) {
        posts.clear();
        posts.addAll(data);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showQuotes(Post post) {
        if (!post.hasQuotes()) {
            return;
        }

        ArrayList<String> quoteIds = post.getQuoteIds();
        ArrayList<Post> quotedPosts = Post.getQuotedPosts(quoteIds, posts);
        quotedPosts.add(post);

        Activity activity = getActivity();
        ListView quoteList = new ListView(activity);
        quoteList.setAdapter(new PostAdapter(activity, R.layout.post_item,
                quotedPosts));

        new AlertDialog.Builder(activity).setView(quoteList).show();
    }

    private void setupOnClickListeners(final ListView lv) {
        final Activity a = getActivity();

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long viewId) {
                showQuotes((Post) lv.getItemAtPosition(position));
            }
        });

        final Intent intent = new Intent(a, ImageWebView.class);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {
                Post selected = (Post) lv.getItemAtPosition(position);
                if (selected.hasFullImgUrl()) {
                    intent.putExtra("URL", selected.getFullImgUrl());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}
