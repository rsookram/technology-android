package com.merono.g;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

public class GApplication extends Application {
    public RequestQueue mRequestQueue;
    public ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);

        int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int maxSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(
                maxSize));
    }

    public class LruBitmapCache extends LruCache<String, Bitmap> implements
            ImageCache {

        public LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        public Bitmap getBitmap(String url) {
            return get(url);
        }

        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }
    }
}
