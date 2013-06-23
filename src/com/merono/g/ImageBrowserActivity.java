package com.merono.g;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class ImageBrowserActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_browser_layout);
		setTitle(Utils.getCurrentBoard(this));

		String[] imgs = getIntent().getStringArrayExtra("com.merono.g.thumbs");
		final String[] fullImgs = getIntent().getStringArrayExtra(
				"com.merono.g.fullImgs");

		GridView grid = (GridView) findViewById(R.id.gridview);
		grid.setAdapter(new GridImageAdapter(this, imgs));

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
}
