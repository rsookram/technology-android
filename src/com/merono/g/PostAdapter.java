package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

public class PostAdapter extends ArrayAdapter<Post> {
    private int postItemResourceId;
    private Activity mActivity;
    private View.OnTouchListener mTouchListener;
    private LayoutInflater mInflater;

    private final int greenTextColour;

    public PostAdapter(Activity activity, int resourceId, ArrayList<Post> posts, View.OnTouchListener listener) {
        super(activity, resourceId, posts);
        postItemResourceId = resourceId;
        mActivity = activity;
        mTouchListener = listener;
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        greenTextColour = activity.getResources().getColor(R.color.green_text);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        final View view = getWorkingView(convertView, parent);
        final ViewHolder viewHolder = getViewHolder(view);
        final Post entry = getItem(position);

        if (mTouchListener != null) {
            view.setOnTouchListener(mTouchListener);
        }

        String idLabel = mActivity.getString(R.string.id_label, entry.getId());

        viewHolder.nameView.setText(entry.getName());
        viewHolder.timeView.setText(entry.getTime());
        viewHolder.idView.setText(idLabel);
        makeGreenText(viewHolder.bodyView, entry.getText());

        if (!entry.hasImgUrl()) {
            viewHolder.imageFrameLayout.setVisibility(View.GONE);
        } else {
            viewHolder.imageFrameLayout.setVisibility(View.VISIBLE);
            GApplication appState = (GApplication) mActivity.getApplication();
            viewHolder.imageView.setImageUrl(entry.getImgURL(),
                    appState.mImageLoader);
        }

        return view;
    }

    private View getWorkingView(final View convertView, ViewGroup parent) {
        if (convertView == null) {
            return mInflater.inflate(postItemResourceId, parent, false);
        } else {
            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView nameView;
        public TextView timeView;
        public TextView idView;
        public TextView bodyView;
        public FrameLayout imageFrameLayout;
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
            viewHolder.imageFrameLayout = (FrameLayout) workingView
                    .findViewById(R.id.post_img_frame);
            viewHolder.imageView = (NetworkImageView) workingView
                    .findViewById(R.id.post_img);

            workingView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) tag;
        }

        return viewHolder;
    }

    private void makeGreenText(TextView tv, String text) {
        int spanStart;
        int spanEnd = 0;

        final String targetStart = "\n>";
        final String targetEnd = "\n";
        Spannable spanRange = new SpannableString(text);

        // case where first line is green text
        if (text.indexOf(">") == 0) {
            spanEnd = text.indexOf(targetEnd);
            if (spanEnd < 0) {
                spanEnd = text.length();
            }
            ForegroundColorSpan foreColour = new ForegroundColorSpan(greenTextColour);
            spanRange.setSpan(foreColour, 0, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        while (true) {
            spanStart = text.indexOf(targetStart, spanEnd);
            // Need a new span object every loop, else it just moves the span
            ForegroundColorSpan foreColour = new ForegroundColorSpan(greenTextColour);
            if (spanStart < 0) {
                break;
            }

            // +1 so that the '\n' in target isn't found
            spanEnd = text.indexOf(targetEnd, spanStart + 1);
            if (spanEnd < 0) {
                spanEnd = text.length();
            }
            spanRange.setSpan(foreColour, spanStart, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv.setText(spanRange);
    }
}
