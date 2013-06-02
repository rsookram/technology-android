package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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

			loadThreads(BASE_API_Url + mBoardName + "/" + mPageNum + ".json");
		} else {
			mBoardName = pref.getString("currentBoard", "g");
			
			post = postsFromBefore;
			adapter = new PostAdapter(this, R.layout.post_item, post);
			((ListView) findViewById(R.id.list)).setAdapter(adapter);
			setupOnItemClickListener();

			setProgressBarIndeterminateVisibility(false);
		}

		this.setTitle("/" + mBoardName + "/" + " - page " + mPageNum);
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

		loadThreads(BASE_API_Url + mBoardName + "/" + mPageNum + ".json");
	}

	private void choosePage() {
		final NumberPicker pageNumber = new NumberPicker(this);
		pageNumber.setMinValue(0);
		pageNumber.setMaxValue(10);
		pageNumber.setValue(mPageNum);

		new AlertDialog.Builder(this)
				.setTitle("Choose Page")
				.setView(pageNumber)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int button) {
								mPageNum = pageNumber.getValue();
								refresh();
							}
						}).show();
	}

	private void chooseBoard() {
		DialogFragment boardFragment = ChooseBoardDialogFragment
				.newInstance("Choose Board");
		boardFragment.show(getFragmentManager(), "choose_board_dialog");
	}

	public void switchBoard(String board) {
		mBoardName = board;
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		pref.edit().putString("currentBoard", mBoardName).commit();
		mPageNum = 0;
		refresh();
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

	private void loadThreads(String url) {
		setProgressBarIndeterminateVisibility(true);

		GApplication appState = (GApplication) getApplication();
		appState.mRequestQueue.add(new JsonObjectRequest(Method.GET, url, null,
				new Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						parseJSON(response);
						adapter = new PostAdapter(GActivity.this,
								R.layout.post_item, post);
						((ListView) findViewById(R.id.list))
								.setAdapter(adapter);
						setupOnItemClickListener();

						setProgressBarIndeterminateVisibility(false);
					}
				}, new ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(GActivity.this, error.getMessage(),
								Toast.LENGTH_LONG).show();
						setProgressBarIndeterminateVisibility(false);
					}
				}));
		appState.mRequestQueue.start();
	}

	private void parseJSON(JSONObject json) {
		try {
			JSONArray threads = json.getJSONArray("threads");
			for (int i = 0; i < NUM_THREADS; i++) {
				JSONArray posts = threads.getJSONObject(i)
						.getJSONArray("posts");
				post.add(new Post(posts.getJSONObject(0), mBoardName));
				threadLinks[i] = "https://boards.4chan.org/" + mBoardName
						+ "/res/" + posts.getJSONObject(0).getInt("no");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}