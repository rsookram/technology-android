package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

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
			new LoadPostsTask(this).execute(getIntent().getStringExtra(URL));
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
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
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

	void refresh() {
		posts.clear();
		adapter.notifyDataSetChanged();

		new LoadPostsTask(this).execute(getIntent().getStringExtra(URL));
	}

	void launchImageBrowser() {
		ArrayList<Post> images = new ArrayList<Post>();
		for (Post post : posts) {
			if (post.hasImgUrl()) {
				images.add(post);
			}
		}

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

	void setupOnClickListener(Activity activity) {
		final ListView lv = (ListView) findViewById(R.id.list);

		final Activity a = activity;
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long viewId) {
				ArrayList<String> quoteIds = ((Post) lv
						.getItemAtPosition(position)).quotes;
				if (quoteIds == null || quoteIds.size() == 0) {
					return;
				}

				ArrayList<Post> quotePosts = Post.getQuotedPosts(quoteIds,
						posts);
				quotePosts.add((Post) lv.getItemAtPosition(position));

				View quoteList = new ListView(a);
				((ListView) quoteList).setAdapter(new PostAdapter(a,
						R.layout.post_item, quotePosts));

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

	class LoadPostsTask extends AsyncTask<String, Void, ArrayList<Post>> {
		private static final String TAG = "ThreadActivity LoadPosts";
		Activity activity;

		public LoadPostsTask(Activity a) {
			this.activity = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params) {
			String siteJson = Utils.loadSite(params[0] + ".json");
			posts = new ArrayList<Post>();

			try {
				JSONObject object = (JSONObject) new JSONTokener(siteJson)
						.nextValue();
				JSONArray allPosts = object.getJSONArray("posts");
				for (int i = 0; i < allPosts.length(); i++) {
					posts.add(new Post(allPosts.getJSONObject(i), mBoardName));
				}

				Log.d(TAG, "finished parsing posts");
				return posts;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			super.onPostExecute(result);

			if (result != null) {
				final ListView lv = (ListView) findViewById(R.id.list);
				adapter = new PostAdapter(activity, R.layout.post_item, result);
				lv.setAdapter(adapter);
				setupOnClickListener(activity);
			} else {
				Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show();
			}
			setProgressBarIndeterminateVisibility(false);
		}
	}
}
