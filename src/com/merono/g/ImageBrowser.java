package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageBrowser extends Activity {
	String[] thumbs;
	private LruCache<String, Bitmap> mMemoryCache;
	private static final String TAG = "ImageBrowser";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_browser_layout);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		this.setTitle("/" + pref.getString("currentBoard", "g") + "/");

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
		return mMemoryCache;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;

			LruCache<String, Bitmap> previousMap = (LruCache<String, Bitmap>) getLastNonConfigurationInstance();

			if (previousMap == null) {
				mMemoryCache = new LruCache<String, Bitmap>(
						Utils.getCacheSize(mContext)) {
					@Override
					protected int sizeOf(String key, Bitmap bitmap) {
						// cache size measured in bytes
						return bitmap.getByteCount();
					}
				};
			} else {
				mMemoryCache = previousMap;
			}
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
				imageView.setLayoutParams(new GridView.LayoutParams(240, 240));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView = (ImageView) convertView;
			}

			Bitmap b = mMemoryCache.get(thumbs[position]);
			if (b != null) {
				imageView.setImageBitmap(b);
			} else {
				new BitmapWorkerTask(imageView, mMemoryCache)
						.execute(thumbs[position]);
			}

			return imageView;
		}
	}
}
