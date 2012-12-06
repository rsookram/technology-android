package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;

public class ThreadActivity extends SherlockActivity {
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
			new LoadPosts(this).execute(getIntent().getStringExtra(URL));
		} else {
			posts = postsFromBefore;
			ListView lv = (ListView) findViewById(R.id.list);
			adapter = new PostAdapter(this, R.layout.post_item, posts, null);
			lv.setAdapter(adapter);
			setupOnClickListener(this);

			setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return posts;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Refresh").setIcon(R.drawable.ic_action_refresh)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						refresh();
						return true;
					}
				}).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu sub = menu.addSubMenu("Extra").setIcon(
				R.drawable.ic_action_extra);

		sub.add("Image Viewer").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						launchImageBrowser();
						return true;
					}
				});

		sub.add("Open in Browser").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						String urlToLaunch = getIntent().getStringExtra(URL);
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(urlToLaunch));
						startActivity(intent);
						return true;
					}
				});

		sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	void refresh() {
		posts.clear();
		adapter.notifyDataSetChanged();

		new LoadPosts(this).execute(getIntent().getStringExtra(URL));
	}

	void launchImageBrowser() {
		ArrayList<Post> images = new ArrayList<Post>();
		for (Post p : posts) {
			if (!p.getImgURL().equals(""))
				images.add(p);
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

		final Intent intent = new Intent(activity, ImageWebView.class);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Post selected = (Post) lv.getItemAtPosition(position);
				if (!selected.getFullImgUrl().equals("")) {
					intent.putExtra(com.merono.g.ImageWebView.URL,
							selected.getFullImgUrl());
					startActivity(intent);
				}
			}
		});
	}

	class LoadPosts extends AsyncTask<String, Void, ArrayList<Post>> {
		private static final String TAG = "ThreadActivity LoadPosts";
		Activity activity;

		public LoadPosts(Activity a) {
			activity = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
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
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

			Log.d(TAG, "finished parsing");
			return posts;
		}

		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			super.onPostExecute(result);

			if (result != null) {
				final ListView lv = (ListView) findViewById(R.id.list);
				adapter = new PostAdapter(activity, R.layout.post_item, result,
						null);
				lv.setAdapter(adapter);
				setupOnClickListener(activity);
			} else {
				Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show();
			}
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}
