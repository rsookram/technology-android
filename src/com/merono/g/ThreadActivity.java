package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

public class ThreadActivity extends FragmentActivity {
	public static final String URL = "";

	private ViewPager mPager;

	private ThreadFragment threadFragment;
	private ImageBrowserFragment imageBrowserFragment;

	private ArrayList<Post> posts = new ArrayList<Post>(1);
	private String mBoardName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		mBoardName = Utils.getCurrentBoard(this);
		setTitle(mBoardName);

		setContentView(R.layout.thread_fragment_pager);

		threadFragment = new ThreadFragment();
		imageBrowserFragment = new ImageBrowserFragment();

		mPager = (ViewPager) findViewById(R.id.thread_pager);
		mPager.setAdapter(new ThreadFragmentPagerAdapter(
				getSupportFragmentManager()));

		if (savedInstanceState == null) {
			loadPosts(getIntent().getStringExtra(URL) + ".json");
		}
	}

	private class ThreadFragmentPagerAdapter extends FragmentPagerAdapter {

		public ThreadFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return threadFragment;
			} else {
				return imageBrowserFragment;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

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
			mPager.setCurrentItem(1, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		if (posts != null) {
			posts.clear();
		}
		loadPosts(getIntent().getStringExtra(URL) + ".json");
	}

	private void setImageBrowserData() {
		ArrayList<Post> images = Post.getImagePosts(posts);

		ArrayList<String> thumbUrls = new ArrayList<String>(images.size());
		ArrayList<String> fullImgUrls = new ArrayList<String>(images.size());
		for (Post post : images) {
			thumbUrls.add(post.getImgURL());
			fullImgUrls.add(post.getFullImgUrl());
		}

		imageBrowserFragment.setData(thumbUrls, fullImgUrls);
	}

	private void loadPosts(String url) {
		setProgressBarIndeterminateVisibility(true);

		GApplication appState = (GApplication) getApplication();
		appState.mRequestQueue.add(new JsonObjectRequest(Method.GET, url, null,
				new Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						parseJSON(response);
						threadFragment.setData(posts);
						setImageBrowserData();
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
