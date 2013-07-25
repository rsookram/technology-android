package com.merono.g;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class ThreadFragment extends ListFragment implements View.OnTouchListener {
    private ArrayList<Post> posts = new ArrayList<Post>(1);
    private PostAdapter adapter;

    private boolean mIsImagePress;
    private FrameLayout imageFrameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);
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
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsImagePress = BoardFragment.isImagePress(v, event);
                if (mIsImagePress) {
                    imageFrameLayout = (FrameLayout) v.findViewById(R.id.post_img_frame);
                    imageFrameLayout.setForeground(getResources().getDrawable(R.color.image_foreground));
                } else {
                    v.setBackgroundResource(android.R.color.holo_blue_dark);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsImagePress) {
                    imageFrameLayout.setForeground(null);
                } else {
                    v.setBackgroundResource(0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsImagePress) {
                    imageFrameLayout.setForeground(null);
                } else {
                    v.setBackgroundResource(0);
                }

                int position = getListView().getPositionForView(v);
                Post selected = posts.get(position);
                if (mIsImagePress && selected.hasFullImgUrl()) {
                    String imgUrl = selected.getFullImgUrl();
                    ImageWebViewFragment.openImageWebView(getActivity(), imgUrl);
                } else {
                    showQuotes(selected);
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
