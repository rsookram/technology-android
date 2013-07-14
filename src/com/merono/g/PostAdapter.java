package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

public class PostAdapter extends ArrayAdapter<Post> {
    private int postItemResourceId;
    private Activity mActivity;
    private LayoutInflater mInflater;

    public PostAdapter(Activity activity, int resourceId, ArrayList<Post> posts) {
        super(activity, resourceId, posts);
        postItemResourceId = resourceId;
        mActivity = activity;
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        final View view = getWorkingView(convertView, parent);
        final ViewHolder viewHolder = getViewHolder(view);
        final Post entry = getItem(position);

        String idLabel = mActivity.getString(R.string.id_label, entry.getId());

        viewHolder.nameView.setText(entry.getName());
        viewHolder.timeView.setText(entry.getTime());
        viewHolder.idView.setText(idLabel);
        viewHolder.bodyView.setText(entry.getText());

        if (!entry.hasImgUrl()) {
            viewHolder.imageView.setVisibility(View.GONE);
        } else {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            GApplication appState = (GApplication) mActivity.getApplication();
            viewHolder.imageView.setImageUrl(entry.getImgURL(),
                    appState.mImageLoader);
        }

        return view;
    }

    private View getWorkingView(final View convertView, ViewGroup parent) {
        View workingView;

        if (convertView == null) {
            workingView = mInflater.inflate(postItemResourceId, parent, false);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    private static class ViewHolder {
        public TextView nameView;
        public TextView timeView;
        public TextView idView;
        public TextView bodyView;
        public NetworkImageView imageView;
    }

    private ViewHolder getViewHolder(final View workingView) {
        // The viewHolder allows us to avoid re-looking up view references
        // Since views are recycled, these references will never change
        final Object tag = workingView.getTag();
        ViewHolder viewHolder;

        if (tag == null || !(tag instanceof ViewHolder)) {
            viewHolder = new ViewHolder();

            viewHolder.nameView = (TextView) workingView
                    .findViewById(R.id.post_name);
            viewHolder.timeView = (TextView) workingView
                    .findViewById(R.id.post_time);
            viewHolder.idView = (TextView) workingView
                    .findViewById(R.id.post_id);
            viewHolder.bodyView = (TextView) workingView
                    .findViewById(R.id.post_body);
            viewHolder.imageView = (NetworkImageView) workingView
                    .findViewById(R.id.post_img);

            workingView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) tag;
        }

        return viewHolder;
    }
}
