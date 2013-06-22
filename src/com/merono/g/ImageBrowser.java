package com.merono.g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.android.volley.toolbox.NetworkImageView;

public class ImageBrowser extends Activity {
	String[] thumbs;

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

	public class ImageAdapter extends BaseAdapter {
		private Activity mActivity;

		public ImageAdapter(Activity a) {
			mActivity = a;
		}

		public int getCount() {
			return thumbs.length;
		}

		public String getItem(int position) {
			return thumbs[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			FrameLayout fl = (FrameLayout) convertView;
			NetworkImageView niv;
			if (fl == null) {
				LayoutInflater inflater = (LayoutInflater) mActivity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				fl = (FrameLayout) inflater.inflate(R.layout.grid_image, null);
				niv = (NetworkImageView) fl.findViewById(R.id.niv_grid);

				niv.setDefaultImageResId(R.drawable.ic_icon);
				niv.setErrorImageResId(android.R.drawable.ic_dialog_alert);
			}
			niv = (NetworkImageView) fl.findViewById(R.id.niv_grid);

			GApplication appState = (GApplication) mActivity.getApplication();
			niv.setImageUrl(getItem(position), appState.mImageLoader);
			return fl;
		}
	}
}
