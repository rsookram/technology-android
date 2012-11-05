package com.merono.g;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	ArrayList<Post> post = null;
	PostAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Starting ThreadActivity");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.thread_layout);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String boardName = pref.getString("currentBoard", "g");
		this.setTitle("/" + boardName + "/");

		final ArrayList<Post> postsFromBefore = (ArrayList<Post>) getLastNonConfigurationInstance();
		if (postsFromBefore == null) {
			new LoadPosts(this).execute(getIntent().getStringExtra(URL));
		} else {
			post = postsFromBefore;
			final ListView lv = (ListView) findViewById(R.id.list);
			adapter = new PostAdapter(this, R.layout.post_item, post, null);
			lv.setAdapter(adapter);
			setupOnClickListener(this);

			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return post;
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
		post.clear();
		adapter.notifyDataSetChanged();

		new LoadPosts(this).execute(getIntent().getStringExtra(URL));
	}

	void launchImageBrowser() {
		ArrayList<Post> images = new ArrayList<Post>();
		for (Post p : post) {
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
			post = new ArrayList<Post>();
			String entirePage = Utils.loadSite(params[0]).replaceAll(
					"(?i)<br[^>]*>", "br2n");
			if (entirePage.equals("error") || entirePage.equals("nofile")) {
				return null;
			}

			Document doc = Jsoup.parse(entirePage);
			Elements allPosts = doc.getElementsByClass("postContainer");

			for (Element singlePost : allPosts) {
				Post postAsPost = new Post(singlePost);
				post.add(postAsPost);
			}

			Log.d(TAG, "finished parsing");
			return post;
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
