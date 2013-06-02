package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

public class ThreadActivity extends Activity {
	private static final String TAG = "ThreadActivity";
	public static final String URL = "";

	ArrayList<Post> posts = null;
	PostAdapter adapter;
	String mBoardName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Starting ThreadActivity");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.thread_layout);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		mBoardName = pref.getString("currentBoard", "g");
		this.setTitle("/" + mBoardName + "/");

		final ArrayList<Post> postsFromBefore = (ArrayList<Post>) getLastNonConfigurationInstance();
		if (postsFromBefore == null) {
			loadPosts(getIntent().getStringExtra(URL) + ".json");
		} else {
			posts = postsFromBefore;
			adapter = new PostAdapter(this, R.layout.post_item, posts);
			((ListView) findViewById(R.id.list)).setAdapter(adapter);
			setupOnClickListener(this);

			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return posts;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.thread_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh();
			return true;
		case R.id.image_browser:
			launchImageBrowser();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		if (posts != null) {
			posts.clear();
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		loadPosts(getIntent().getStringExtra(URL) + ".json");
	}

	private void launchImageBrowser() {
		ArrayList<Post> images = Post.getImagePosts(posts);

		String[] thumbUrls = new String[images.size()];
		String[] fullImgUrls = new String[images.size()];
		for (int i = 0; i < images.size(); i++) {
			thumbUrls[i] = images.get(i).getImgURL();
			fullImgUrls[i] = images.get(i).getFullImgUrl();
		}

		Intent i = new Intent(this, ImageBrowser.class);
		i.putExtra("com.merono.g.thumbs", thumbUrls);
		i.putExtra("com.merono.g.fullImgs", fullImgUrls);
		startActivity(i);
	}

	private void setupOnClickListener(Activity activity) {
		final ListView lv = (ListView) findViewById(R.id.list);
		final Activity a = activity;

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long viewId) {
				Post selectedPost = (Post) lv.getItemAtPosition(position);
				ArrayList<String> quoteIds = selectedPost.quotes;
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

		final Intent intent = new Intent(activity, ImageWebView.class);
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Post selected = (Post) lv.getItemAtPosition(position);
				if (selected.hasFullImgUrl()) {
					intent.putExtra(com.merono.g.ImageWebView.URL,
							selected.getFullImgUrl());
					startActivity(intent);
				}
				return true;
			}
		});
	}

	private void loadPosts(String url) {
		setProgressBarIndeterminateVisibility(true);

		GApplication appState = (GApplication) getApplication();
		appState.mRequestQueue.add(new JsonObjectRequest(Method.GET, url, null,
				new Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						posts = new ArrayList<Post>(1);
						parseJSON(response);
						adapter = new PostAdapter(ThreadActivity.this,
								R.layout.post_item, posts);
						((ListView) findViewById(R.id.list))
								.setAdapter(adapter);
						setupOnClickListener(ThreadActivity.this);

						setProgressBarIndeterminateVisibility(false);
					}
				}, new ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(ThreadActivity.this, error.getMessage(),
								Toast.LENGTH_LONG).show();
						setProgressBarIndeterminateVisibility(false);
					}
				}));
		appState.mRequestQueue.start();
	}

	private void parseJSON(JSONObject json) {
		try {
			JSONArray allPosts = json.getJSONArray("posts");
			for (int i = 0; i < allPosts.length(); i++) {
				posts.add(new Post(allPosts.getJSONObject(i), mBoardName));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
