package com.merono.g;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;

public class ImageBrowser extends SherlockActivity {
	String[] thumbs;
	private HashMap<String, Bitmap> imageMap = null;
	private static final String TAG = "ImageBrowser";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_browser_layout);
		thumbs = getIntent().getStringArrayExtra("com.merono.g.thumbs");
		final String[] fullImgs = getIntent().getStringArrayExtra(
				"com.merono.g.fullImgs");

		GridView grid = (GridView) findViewById(R.id.gridview);

		grid.setAdapter(new ImageAdapter(this));

		final Intent intent = new Intent(this, ImageWebView.class);
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				String selected = fullImgs[position];
				intent.putExtra(com.merono.g.ImageWebView.URL, selected);
				startActivity(intent);
			}
		});
	}

	// images won't need to be downloaded again when the screen is rotated
	@Override
	public Object onRetainNonConfigurationInstance() {
		return imageMap;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;

			HashMap<String, Bitmap> previousMap = (HashMap<String, Bitmap>) getLastNonConfigurationInstance();
			imageMap = (previousMap == null) ? new HashMap<String, Bitmap>()
					: previousMap;
		}

		public int getCount() {
			return thumbs.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				// if it's not recycled, init some attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(160, 160));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(0, 0, 0, 0);
			} else {
				imageView = (ImageView) convertView;
			}

			if (imageMap.containsKey(thumbs[position])) {
				imageView.setImageBitmap(imageMap.get(thumbs[position]));
				return imageView;
			}

			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(thumbs[position]);

			return imageView;
		}
	}

	public static Bitmap getBitmapFromURL(String src) {
		Bitmap bitmap = null;
		try {
			URLConnection conn = new URL(src).openConnection();
			bitmap = BitmapFactory
					.decodeStream((InputStream) conn.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	class BitmapWorkerTask extends AsyncTask<String, String, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			// Use WeakReference to ensure imageView can be garbage collected
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

}
