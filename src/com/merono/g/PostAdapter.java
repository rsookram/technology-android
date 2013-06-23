package com.merono.g;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class PostAdapter extends ArrayAdapter<Post> {
	private int postItemResourceId;
	private Activity mActivity;

	public PostAdapter(Activity a, int textViewResourceId, ArrayList<Post> posts) {
		super(a, textViewResourceId, posts);
		postItemResourceId = textViewResourceId;
		mActivity = a;
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		final View view = getWorkingView(convertView, parent);
		final ViewHolder viewHolder = getViewHolder(view);
		final Post entry = getItem(position);

		viewHolder.nameView.setText(entry.getName());
		viewHolder.timeView.setText(entry.getTime());
		viewHolder.idView.setText("No." + entry.getId());
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
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			workingView = inflater.inflate(postItemResourceId, parent, false);
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
