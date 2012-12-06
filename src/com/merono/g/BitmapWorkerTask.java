package com.merono.g;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<String, String, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	private HashMap<String, Bitmap> imageMap = null;

	public BitmapWorkerTask(ImageView imageView, HashMap<String, Bitmap> image) {
		// Use WeakReference to ensure imageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		imageMap = image;
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
			imageMap.put(params[0], Utils.getBitmapFromURL(params[0]));
		}

		return imageMap.get(params[0]);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		// Once complete, see if ImageView is still around and set bitmap.
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}