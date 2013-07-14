package com.merono.g;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

public class GActivity extends Activity {
    private static final String API_URL = "https://api.4chan.org%scatalog.json";

    private static String mBoardName;
    private static final ArrayList<String> threadLinks = new ArrayList<String>(15);

    private MenuItem refreshItem;

    private ArrayList<Post> posts = new ArrayList<Post>(15);
    private PostAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.thread_layout);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);

        ArrayList<Post> previousPosts = (ArrayList<Post>) getLastNonConfigurationInstance();
        if (previousPosts == null) {
            mBoardName = pref.getString("board", "/g/");
            pref.edit().putString("currentBoard", mBoardName).commit();

            loadThreads(String.format(API_URL, mBoardName));
        } else {
            mBoardName = pref.getString("currentBoard", "/g/");
            posts = new ArrayList<Post>(previousPosts);
        }

        adapter = new PostAdapter(this, R.layout.post_item, posts);
        ((ListView) findViewById(R.id.list)).setAdapter(adapter);
        setupOnItemClickListeners();

        getActionBar().setDisplayShowHomeEnabled(false);
        setTitle(mBoardName);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return posts;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        refreshItem = menu.findItem(R.id.refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refresh();
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

    public void refresh() {
        posts.clear();
        threadLinks.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        setTitle(mBoardName);

        loadThreads(String.format(API_URL, mBoardName));
    }

    private void chooseBoard() {
        DialogFragment boardFragment = new ChooseBoardDialogFragment();
        boardFragment.show(getFragmentManager(), "choose_board_dialog");
    }

    public void switchBoard(String board) {
        mBoardName = Utils.cleanBoardName(board);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        pref.edit().putString("currentBoard", mBoardName).commit();
        refresh();
    }

    private void setupOnItemClickListeners() {
        ListView lv = (ListView) findViewById(R.id.list);

        final Intent i = new Intent(this, ThreadActivity.class);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {
                if (threadLinks.get(position) != null) {
                    i.putExtra("URL", threadLinks.get(position));
                    startActivity(i);
                }
            }
        });

        final Intent intent = new Intent(this, ImageWebView.class);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View arg1,
                    int position, long arg3) {
                Post selected = (Post) av.getItemAtPosition(position);
                if (selected.hasFullImgUrl()) {
                    intent.putExtra("URL", selected.getFullImgUrl());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadThreads(String url) {
        setProgressBarIndeterminateVisibility(true);
        if (refreshItem != null) {
            refreshItem.setVisible(false);
        }

        GApplication appState = (GApplication) getApplication();
        appState.mRequestQueue.add(new JsonArrayRequest(url,
                new Listener<JSONArray>() {
                    public void onResponse(JSONArray response) {
                        parseJSON(response);
                        adapter.notifyDataSetChanged();

                        setProgressBarIndeterminateVisibility(false);
                        refreshItem.setVisible(true);
                    }
                }, new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        posts.add(new Post(error.getMessage()));
                        threadLinks.add(null);
                        adapter.notifyDataSetChanged();
                        setProgressBarIndeterminateVisibility(false);
                        refreshItem.setVisible(true);
                    }
                }));
        appState.mRequestQueue.start();
    }

    private void parseJSON(JSONArray json) {
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                JSONArray threads = obj.getJSONArray("threads");
                for (int j = 0; j < threads.length(); j++) {
                    JSONObject post = threads.getJSONObject(j);
                    posts.add(new Post(post, mBoardName));
                    threadLinks.add("https://boards.4chan.org" + mBoardName +
                            "res/" + post.getInt("no"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}