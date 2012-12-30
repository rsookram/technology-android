package com.merono.g;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<String, String, Bitmap> {
	ImageView iv;
	private LruCache<String, Bitmap> mMemoryCache;

	public BitmapWorkerTask(ImageView imageView, LruCache<String, Bitmap> cache) {
		iv = imageView;
		mMemoryCache = cache;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		iv.setImageResource(R.drawable.ic_icon);
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		final Bitmap bitmap = Utils.getBitmapFromURL(params[0]);
		mMemoryCache.put(params[0], Utils.getBitmapFromURL(params[0]));

		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		iv.setImageBitmap(bitmap);
	}
}