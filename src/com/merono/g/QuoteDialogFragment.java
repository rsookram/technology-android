package com.merono.g;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ListView;

import java.util.ArrayList;

public class QuoteDialogFragment extends DialogFragment {
    private static ArrayList<Post> posts;

    public static QuoteDialogFragment newInstance(Post post, ArrayList<Post> allPosts) {
        ArrayList<String> quoteIds = post.getQuoteIds();
        posts = Post.selectPostsByIds(quoteIds, allPosts);
        posts.add(post);

        return new QuoteDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        ListView lv = new ListView(activity);
        lv.setAdapter(new PostAdapter(activity, R.layout.post_item, posts));

        return new AlertDialog.Builder(activity).setView(lv).create();
    }
}
