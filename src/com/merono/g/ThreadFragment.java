package com.merono.g;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ThreadFragment extends ListFragment {
    private ArrayList<Post> posts = new ArrayList<Post>(1);
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

        final Intent intent = new Intent(getActivity(), ImageWebView.class);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View arg1,
                                           int position, long arg3) {
                Post selected = (Post) av.getItemAtPosition(position);
                if (selected.hasFullImgUrl()) {
                    intent.putExtra("URL", selected.getFullImgUrl());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showQuotes((Post) l.getItemAtPosition(position));
    }
}
