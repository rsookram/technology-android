package com.merono.g;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class GApplication extends Application {
	private static final String TAG = "GApplication";
	private LruCache<String, Bitmap> mMemoryCache = null;

	@Override
	public void onCreate() {
		super.onCreate();
		initCache();
	}

	private void initCache() {
		int memClass = ((ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSize = 1024 * 1024 * memClass / 8;

		Log.d(TAG, "Initializing cache to:" + cacheSize + " bytes");
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount(); // cache size measured in bytes
			}
		};
	}

	public void loadBitmap(String key, ImageView iv) {
		Bitmap b = mMemoryCache.get(key);
		if (b != null) {
			iv.setImageBitmap(b);
		} else {
			new BitmapWorkerTask(iv, mMemoryCache).execute(key);
		}
	}
}
