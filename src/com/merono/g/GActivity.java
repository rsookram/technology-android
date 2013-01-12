package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class GActivity extends Activity {
	static int mPageNum = 0;
	static String mBoardName = "g";
	static final String[] links = new String[15]; // holds the thread links

	private static final String baseUrl = "http://boards.4chan.org/";
	private static final String TAG = "GActivity";

	ArrayList<Post> post = new ArrayList<Post>();
	PostAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.gactivity_layout);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		final ArrayList<Post> postsFromBefore = (ArrayList<Post>) getLastNonConfigurationInstance();
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

			post = postsFromBefore;

			adapter = new PostAdapter(this, R.layout.post_item, post, true);
			((ListView) findViewById(R.id.main_list)).setAdapter(adapter);
			setupOnItemClickListener();
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return post;
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh();
			return true;
		case R.id.choose_page:
			choosePage();
			return true;
		case R.id.choose_board:
			chooseBoard();
			return true;
		case R.id.settings:
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void refresh() {
		post.clear();
		if (adapter != null)
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

	class LoadThreads extends AsyncTask<String, Void, ArrayList<Post>> {
		Activity activity;

		LoadThreads(Activity a) {
			activity = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params) {
			String siteJson = Utils.loadSite(params[0]);
			if (siteJson.equals("nofile") || siteJson.equals("error")) {
				links[0] = siteJson;
				return null;
			}

			try {
				JSONObject object;
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
			if (links[0].equals("error")) {
				Toast.makeText(getApplicationContext(),
						"Error loading. (IOException)", Toast.LENGTH_LONG)
						.show();
				setProgressBarIndeterminateVisibility(false);
				return;
			} else if (links[0].equals("nofile")) {
				Toast.makeText(getApplicationContext(), "Page does not exist.",
						Toast.LENGTH_LONG).show();
				setProgressBarIndeterminateVisibility(false);
				return;
			}

			ListView lv = (ListView) findViewById(R.id.main_list);

			adapter = new PostAdapter(activity, R.layout.post_item, threads,
					true);
			lv.setAdapter(adapter);
			setupOnItemClickListener();

			setProgressBarIndeterminateVisibility(false);
		}
	}
}