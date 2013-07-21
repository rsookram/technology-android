package com.merono.g;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ThreadActivity extends FragmentActivity {
    private static final int FRAGMENT_COUNT = 2;

    private ThreadFragment threadFragment;
    private ImageBrowserFragment imageBrowserFragment;

    private MenuItem refreshItem;

    private static String cachedUrl;
    private static ArrayList<Post> posts = new ArrayList<Post>(1);

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

        // don't reload on configuration change
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
        refreshItem = menu.findItem(R.id.refresh);
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

    private void loadPosts(final String url, final boolean isRefresh) {
        setProgressBarIndeterminateVisibility(true);
        if (refreshItem != null) {
            refreshItem.setVisible(false);
        }

        GApplication appState = (GApplication) getApplication();
        appState.mRequestQueue.add(new JsonObjectRequest(Method.GET, url, null,
                new Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        posts.clear();
                        parseJSON(response);
                        cachedUrl = url;
                        threadFragment.setData(posts);
                        setImageBrowserData();
                        setProgressBarIndeterminateVisibility(false);
                        refreshItem.setVisible(true);
                    }
                },
                new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (isRefresh) {
                            Toast.makeText(ThreadActivity.this,
                                    error.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else if (!loadCachedPosts(url)) {
                            posts.clear();
                            posts.add(new Post(error.getMessage()));
                            threadFragment.setData(posts);
                        }
                        setProgressBarIndeterminateVisibility(false);
                        if (refreshItem != null) {
                            refreshItem.setVisible(true);
                        }
                    }
                }
        ));
        appState.mRequestQueue.start();
    }

    private boolean loadCachedPosts(String url) {
        if (!url.equals(cachedUrl)) {
            return false;
        }

        threadFragment.setData(posts);
        setImageBrowserData();
        return true;
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
