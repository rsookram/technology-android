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
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

public class ThreadActivity extends FragmentActivity {
    private static final int FRAGMENT_COUNT = 2;

    private ThreadFragment threadFragment;
    private ImageBrowserFragment imageBrowserFragment;

    private ArrayList<Post> posts = new ArrayList<Post>(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        getActionBar().setDisplayShowHomeEnabled(false);
        setTitle(Utils.getCurrentBoard(this));

        setContentView(R.layout.thread_fragment_pager);

        threadFragment = new ThreadFragment();
        imageBrowserFragment = new ImageBrowserFragment();

        FragmentManager fm = getSupportFragmentManager();
        ViewPager mPager = (ViewPager) findViewById(R.id.thread_pager);
        mPager.setAdapter(new ThreadFragmentPagerAdapter(fm));

        mPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / 10);
        mPager.setPageMarginDrawable(android.R.color.black);

        // Only load when first created
        if (savedInstanceState == null) {
            loadPosts(getIntent().getStringExtra("URL") + ".json", false);
        }
    }

    private class ThreadFragmentPagerAdapter extends FragmentPagerAdapter {
        public ThreadFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return (position == 0) ? threadFragment : imageBrowserFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (position == 0) ? "Thread" : "Image Viewer";
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            loadPosts(getIntent().getStringExtra("URL") + ".json", true);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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

    private void loadPosts(String url, final boolean isRefresh) {
        setProgressBarIndeterminateVisibility(true);

        GApplication appState = (GApplication) getApplication();
        appState.mRequestQueue.add(new JsonObjectRequest(Method.GET, url, null,
                new Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        posts.clear();
                        parseJSON(response);
                        threadFragment.setData(posts);
                        setImageBrowserData();
                        setProgressBarIndeterminateVisibility(false);
                    }
                }, new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (isRefresh) {
                            Toast.makeText(ThreadActivity.this,
                                    error.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            posts.add(new Post(error.getMessage()));
                            threadFragment.setData(posts);
                        }
                        setProgressBarIndeterminateVisibility(false);
                    }
                }));
        appState.mRequestQueue.start();
    }

    private void parseJSON(JSONObject json) {
        try {
            String boardName = Utils.getCurrentBoard(this);
            JSONArray allPosts = json.getJSONArray("posts");
            for (int i = 0; i < allPosts.length(); i++) {
                posts.add(new Post(allPosts.getJSONObject(i), boardName));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
