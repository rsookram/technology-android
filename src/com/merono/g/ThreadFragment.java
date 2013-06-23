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

	private void setupOnClickListeners(final ListView lv) {
		final Activity a = getActivity();

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long viewId) {
				Post selectedPost = (Post) lv.getItemAtPosition(position);
				ArrayList<String> quoteIds = selectedPost.getQuoteIds();
				if (quoteIds == null || quoteIds.size() == 0) {
					return;
				}

				ArrayList<Post> quotedPosts = Post.getQuotedPosts(quoteIds,
						posts);
				quotedPosts.add(selectedPost);

				ListView quoteList = new ListView(a);
				quoteList.setAdapter(new PostAdapter(a, R.layout.post_item,
						quotedPosts));

				new AlertDialog.Builder(a).setView(quoteList).show();
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
				}
				return true;
			}
		});
	}
}
