package com.merono.g;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;

public class GActivity extends SherlockActivity {
	static int mPageNum = 0;
	static String mBoardName = "g";
	static final String[] links = new String[15]; // holds the thread links
	private HashMap<String, Bitmap> imageMap = null;

	private static final String baseUrl = "http://boards.4chan.org/";
	private static final String TAG = "GActivity";

	ArrayList<Post> post = new ArrayList<Post>();
	PostAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.gactivity_layout);

		ListView lv = (ListView) findViewById(R.id.main_list);
		registerForContextMenu(lv);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		final ThreadPosts postsFromBefore = (ThreadPosts) getLastNonConfigurationInstance();
		if (postsFromBefore == null) {
			mBoardName = pref.getString("board", "g");
			pref.edit().putString("currentBoard", mBoardName).commit();
			this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

			String urlStr = "http://api.4chan.org/" + mBoardName + "/"
					+ mPageNum + ".json";
			new LoadThreads(this).execute(urlStr);
		} else {
			mBoardName = pref.getString("currentBoard", "g");
			this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

			imageMap = postsFromBefore.images;
			post = postsFromBefore.posts;
			adapter = new PostAdapter(this, R.layout.post_item, post, imageMap);
			lv.setAdapter(adapter);
			setupOnItemClickListener();
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ThreadPosts restoreValue = new ThreadPosts();
		restoreValue.posts = post;
		restoreValue.images = imageMap;
		return restoreValue;
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
		sub.add("Choose Page").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						choosePage();
						return true;
					}
				});
		sub.add("Choose Board").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						chooseBoard();
						return true;
					}
				});
		sub.add("Settings").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						startActivity(new Intent(getApplicationContext(),
								PrefsActivity.class));
						return true;
					}
				});
		sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	void refresh() {
		post.clear();
		adapter.notifyDataSetChanged();

		this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

		String urlStr = "http://api.4chan.org/" + mBoardName + "/" + mPageNum
				+ ".json";
		new LoadThreads(this).execute(urlStr);
	}

	void choosePage() {
		AlertDialog.Builder pageAlert = new AlertDialog.Builder(this);
		pageAlert.setTitle("Choose Page");

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		pageAlert.setView(input);

		pageAlert.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						try {
							mPageNum = Integer.parseInt(value);
							refresh();
						} catch (Exception e) {
							Toast.makeText(getBaseContext(),
									"Invalid page number", Toast.LENGTH_SHORT)
									.show();
						}
					}
				});

		pageAlert.show();
	}

	void chooseBoard() {
		AlertDialog.Builder boardAlert = new AlertDialog.Builder(this);
		boardAlert.setTitle("Choose Board");

		final EditText boardText = new EditText(this);
		// forces one-line text input
		boardText.setInputType(InputType.TYPE_CLASS_TEXT);
		boardAlert.setView(boardText);

		boardAlert.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = boardText.getText().toString();
						mBoardName = value;
						SharedPreferences pref = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						pref.edit().putString("currentBoard", mBoardName)
								.commit();
						mPageNum = 0;
						refresh();
					}
				});

		boardAlert.show();
	}

	void setupOnItemClickListener() {
		ListView lv = (ListView) findViewById(R.id.main_list);
		final Activity a = this;

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				final Intent i = new Intent(a, ThreadActivity.class);
				i.putExtra(com.merono.g.ThreadActivity.URL, links[position]);
				startActivity(i);
			}
		});

		final Intent intent = new Intent(this, ImageWebView.class);
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Post selected = post.get(position);
				intent.putExtra(com.merono.g.ImageWebView.URL,
						selected.getFullImgUrl());
				startActivity(intent);
				return true;
			}

		});
	}

	class ThreadPosts {
		ArrayList<Post> posts;
		HashMap<String, Bitmap> images;
	}

	class LoadThreads extends AsyncTask<String, Void, ArrayList<Post>> {
		Activity activity;

		LoadThreads(Activity a) {
			activity = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params) {
			String siteJson = Utils.loadSite(params[0]);

			JSONObject object;
			try {
				object = (JSONObject) new JSONTokener(siteJson).nextValue();
				JSONArray threads = object.getJSONArray("threads");
				for (int i = 0; i < 15; i++) {
					JSONArray posts = threads.getJSONObject(i).getJSONArray(
							"posts");
					post.add(new Post(posts.getJSONObject(0), mBoardName));
					links[i] = baseUrl + mBoardName + "/res/"
							+ posts.getJSONObject(0).getInt("no");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "end parsing");
			return post;
		}

		@Override
		protected void onPostExecute(ArrayList<Post> threads) {
			super.onPostExecute(threads);

			/*
			if (links[0].equals("error")) {
				Toast.makeText(getApplicationContext(),
						"Error loading. (IOException)", Toast.LENGTH_LONG)
						.show();
			} else if (links[0].equals("nofile")) {
				Toast.makeText(getApplicationContext(), "Page does not exist.",
						Toast.LENGTH_LONG).show();
			}
			*/

			ListView lv = (ListView) findViewById(R.id.main_list);
			imageMap = new HashMap<String, Bitmap>();
			adapter = new PostAdapter(activity, R.layout.post_item, threads,
					imageMap);
			lv.setAdapter(adapter);
			setupOnItemClickListener();

			setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}