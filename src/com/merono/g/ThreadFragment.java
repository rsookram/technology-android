package com.merono.g;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.util.ArrayList;

public class ThreadFragment extends ListFragment {
    private ArrayList<Post> posts = new ArrayList<Post>(1);
    private PostAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View arg1,
                                           int position, long arg3) {
                Post selected = (Post) av.getItemAtPosition(position);
                if (selected.hasFullImgUrl()) {
                    String imgUrl = selected.getFullImgUrl();
                    ImageWebViewFragment.openImageWebView(getActivity(), imgUrl);
                    return true;
                }
                return false;
            }
        });
    }

    public void setData(ArrayList<Post> data) {
        posts.clear();
        posts.addAll(data);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showQuotes(Post post) {
        if (!post.hasQuotes()) {
            return;
        }

        DialogFragment quoteFragment = QuoteDialogFragment.newInstance(post, posts);
        quoteFragment.show(getFragmentManager(), "choose_board_dialog");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showQuotes((Post) l.getItemAtPosition(position));
    }
}
