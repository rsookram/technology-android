package com.merono.g;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

import java.util.ArrayList;

public class QuoteDialogFragment extends DialogFragment implements GestureDetector.OnGestureListener {
    private static ArrayList<Post> posts;
    private GestureDetector mDetector;

    public static QuoteDialogFragment newInstance(Post post, ArrayList<Post> allPosts) {
        ArrayList<String> quoteIds = post.getQuoteIds();
        posts = Post.selectPostsByIds(quoteIds, allPosts);
        posts.add(post);

        return new QuoteDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetector = new GestureDetector(getActivity(), this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        ListView lv = new ListView(activity) {
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                mDetector.onTouchEvent(ev);
                return super.onTouchEvent(ev);
            }
        };

        lv.setAdapter(new PostAdapter(activity, R.layout.post_item, posts, null));

        return new AlertDialog.Builder(activity).setView(lv).create();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {
        if (Math.abs(velocityX) > Math.abs(velocityY) + 1000) {
            getDialog().dismiss();
        }
        return false;
    }
}
