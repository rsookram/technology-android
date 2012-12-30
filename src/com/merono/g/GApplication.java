package com.merono.g;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

public class GApplication extends Application {
	private LruCache<String, Bitmap> mMemoryCache = null;

	private void initCache() {
		int memClass = ((ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSize = 1024 * 1024 * memClass / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// cache size measured in bytes
				return bitmap.getByteCount();
			}
		};
	}

	public void loadBitmap(String key, ImageView iv) {
		if (mMemoryCache == null) {
			initCache();
		}

		Bitmap b = mMemoryCache.get(key);
		if (b != null) {
			iv.setImageBitmap(b);
		} else {
			new BitmapWorkerTask(iv, mMemoryCache).execute(key);
		}
	}
}
