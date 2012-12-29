package com.merono.g;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PostAdapter extends ArrayAdapter<Post> {
	private int postItemResourceId;
	private LruCache<String, Bitmap> mMemoryCache;
	private boolean loadThumbs;

	public PostAdapter(Context context, int textViewResourceId,
			ArrayList<Post> posts, LruCache<String, Bitmap> imageHolder) {
		super(context, textViewResourceId, posts);
		postItemResourceId = textViewResourceId;
		mMemoryCache = imageHolder;
		loadThumbs = (imageHolder != null);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final View view = getWorkingView(convertView);
		final ViewHolder viewHolder = getViewHolder(view);
		final Post entry = getItem(position);

		viewHolder.nameView.setText(entry.getName());
		viewHolder.timeView.setText(entry.getTime());
		viewHolder.idView.setText(entry.getId());
		viewHolder.bodyView.setText(entry.getText());

		Bitmap b;
		if (!loadThumbs || entry.getImgURL().equals("")) {
			viewHolder.imageView.setVisibility(View.GONE);
		} else if ((b = mMemoryCache.get(entry.getImgURL())) != null) {
			viewHolder.imageView.setImageBitmap(b);
		} else {
			new BitmapWorkerTask(viewHolder.imageView, mMemoryCache)
					.execute(entry.getImgURL());
		}

		return view;
	}

	private View getWorkingView(final View convertView) {
		View workingView = null;

		if (convertView == null) {
			final Context context = getContext();
			final LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			workingView = inflater.inflate(postItemResourceId, null);
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
		public ImageView imageView;
	}

	private ViewHolder getViewHolder(final View workingView) {
		// The viewHolder allows us to avoid re-looking up view references
		// Since views are recycled, these references will never change
		final Object tag = workingView.getTag();
		ViewHolder viewHolder = null;

		if (null == tag || !(tag instanceof ViewHolder)) {
			viewHolder = new ViewHolder();

			viewHolder.nameView = (TextView) workingView
					.findViewById(R.id.post_name);
			viewHolder.timeView = (TextView) workingView
					.findViewById(R.id.post_time);
			viewHolder.idView = (TextView) workingView
					.findViewById(R.id.post_id);
			viewHolder.bodyView = (TextView) workingView
					.findViewById(R.id.post_body);
			viewHolder.imageView = (ImageView) workingView
					.findViewById(R.id.post_img);

			workingView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) tag;
		}

		return viewHolder;
	}
}
