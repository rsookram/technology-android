package com.merono.g;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PostAdapter extends ArrayAdapter<Post> {
	private int postItemResourceId;
	private HashMap<String, Bitmap> imageMap = null;
	private Boolean loadThumbs;

	public PostAdapter(Context context, int textViewResourceId,
			ArrayList<Post> posts, HashMap<String, Bitmap> imageholder) {
		super(context, textViewResourceId, posts);
		postItemResourceId = textViewResourceId;
		imageMap = imageholder;
		if (imageholder == null)
			loadThumbs = false;
		else
			loadThumbs = true;
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
		if (!loadThumbs) {
			viewHolder.imageView.setVisibility(View.GONE);
			return view;
		}
		if (entry.getImgURL().equals("")) {
			viewHolder.imageView.setVisibility(View.GONE);
		} else if (imageMap.containsKey(entry.getImgURL())) {
			viewHolder.imageView
					.setImageBitmap(imageMap.get(entry.getImgURL()));
		} else {
			BitmapWorkerTask task = new BitmapWorkerTask(viewHolder.imageView);
			task.execute(entry.getImgURL());

		}
		return view;
	}

	private View getWorkingView(final View convertView) {
		View workingView = null;

		if (null == convertView) {
			final Context context = getContext();
			final LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			workingView = inflater.inflate(postItemResourceId, null);
		} else {
			workingView = convertView;
		}

		return workingView;
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

	private static class ViewHolder {
		public TextView nameView;
		public TextView timeView;
		public TextView idView;
		public TextView bodyView;
		public ImageView imageView;
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	class BitmapWorkerTask extends AsyncTask<String, String, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			// Use WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			final ImageView imageView = imageViewReference.get();
			imageView.setImageResource(R.drawable.ic_icon);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			if (!imageMap.containsKey(params[0])) {
				imageMap.put(params[0], getBitmapFromURL(params[0]));
			}

			return imageMap.get(params[0]);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}
