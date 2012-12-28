package com.merono.g;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageBrowser extends Activity {
	String[] thumbs;
	private HashMap<String, Bitmap> imageMap = null;
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
				imageView.setLayoutParams(new GridView.LayoutParams(240, 240));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView = (ImageView) convertView;
			}

			if (imageMap.containsKey(thumbs[position])) {
				imageView.setImageBitmap(imageMap.get(thumbs[position]));
				return imageView;
			}

			BitmapWorkerTask task = new BitmapWorkerTask(imageView, imageMap);
			task.execute(thumbs[position]);

			return imageView;
		}
	}
}
