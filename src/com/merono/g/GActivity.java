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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

public class GActivity extends Activity {
	private static final String TAG = "GActivity";
	private static final String BASE_API_Url = "https://api.4chan.org/";
	private static final int NUM_THREADS = 15; // number of threads per page

	static int mPageNum = 0;
	static String mBoardName = "g";
	static final String[] threadLinks = new String[NUM_THREADS];

	ArrayList<Post> post = new ArrayList<Post>(NUM_THREADS);
	PostAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.thread_layout);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		final ArrayList<Post> postsFromBefore = (ArrayList<Post>) getLastNonConfigurationInstance();
		if (postsFromBefore == null) {
			mBoardName = pref.getString("board", "g");
			pref.edit().putString("currentBoard", mBoardName).commit();
			this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

			String urlStr = BASE_API_Url + mBoardName + "/" + mPageNum + ".json";
			new LoadThreadsTask(this).execute(urlStr);
		} else {
			mBoardName = pref.getString("currentBoard", "g");
			this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

			post = postsFromBefore;
			adapter = new PostAdapter(this, R.layout.post_item, post);
			((ListView) findViewById(R.id.list)).setAdapter(adapter);
			setupOnItemClickListener();

			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return post;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		post.clear();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}

		this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);

		String urlStr = BASE_API_Url + mBoardName + "/" + mPageNum + ".json";
		new LoadThreadsTask(this).execute(urlStr);
	}

	private void choosePage() {
		final NumberPicker pageNumber = new NumberPicker(this);
		pageNumber.setMinValue(0);
		pageNumber.setMaxValue(10);
		pageNumber.setValue(mPageNum);

		new AlertDialog.Builder(this).setTitle("Choose Page")
				.setView(pageNumber)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mPageNum = pageNumber.getValue();
						refresh();
					}
				}).show();
	}

	private void chooseBoard() {
		//request focus and finsih when pressing done on keyboard
		final EditText boardText = new EditText(this);
		// forces one-line text input
		boardText.setInputType(InputType.TYPE_CLASS_TEXT);

		new AlertDialog.Builder(this).setTitle("Choose Board")
				.setView(boardText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mBoardName = boardText.getText().toString();
						SharedPreferences pref = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						pref.edit().putString("currentBoard", mBoardName)
								.commit();
						mPageNum = 0;
						refresh();
					}
				}).show();
	}

	private void setupOnItemClickListener() {
		ListView lv = (ListView) findViewById(R.id.list);
		final Activity a = this;

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				final Intent i = new Intent(a, ThreadActivity.class);
				i.putExtra(com.merono.g.ThreadActivity.URL,
						threadLinks[position]);
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

	private class LoadThreadsTask extends AsyncTask<String, Void, ArrayList<Post>> {
		Activity mActivity;

		public LoadThreadsTask(Activity a) {
			mActivity = a;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params) {
			String siteJSON = Utils.loadSite(params[0]);
			if (siteJSON.equals("nofile") || siteJSON.equals("error")) {
				threadLinks[0] = siteJSON;
				return null;
			}

			try {
				JSONObject object;
				object = (JSONObject) new JSONTokener(siteJSON).nextValue();
				JSONArray threads = object.getJSONArray("threads");

				for (int i = 0; i < NUM_THREADS; i++) {
					JSONArray posts = threads.getJSONObject(i).getJSONArray(
							"posts");
					post.add(new Post(posts.getJSONObject(0), mBoardName));
					threadLinks[i] = "https://boards.4chan.org/" + mBoardName
							+ "/res/" + posts.getJSONObject(0).getInt("no");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "end threads parsing");
			return post;
		}

		@Override
		protected void onPostExecute(ArrayList<Post> threads) {
			super.onPostExecute(threads);

			if (threadLinks[0].equals("error")) {
				Toast.makeText(mActivity, "Error loading. (IOException)",
						Toast.LENGTH_LONG).show();
				setProgressBarIndeterminateVisibility(false);
				return;
			} else if (threadLinks[0].equals("nofile")) {
				Toast.makeText(mActivity, "Page does not exist.",
						Toast.LENGTH_LONG).show();
				setProgressBarIndeterminateVisibility(false);
				return;
			}

			adapter = new PostAdapter(mActivity, R.layout.post_item, threads);
			((ListView) findViewById(R.id.list)).setAdapter(adapter);
			setupOnItemClickListener();

			setProgressBarIndeterminateVisibility(false);
		}
	}
}