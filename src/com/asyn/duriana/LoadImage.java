package com.asyn.duriana;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.asyn.duriana.cache.FileCache;
import com.asyn.duriana.cache.MemoryCache;
import com.asyn.duriana.utilities.Utils;
import com.asyn.durianacache.R;

public class LoadImage {
	
	private static final int N_THREADS = 5;
	private static final int DEFAULT_IMG = R.drawable.ic_launcher;
	
	private static final int REQUIRED_SIZE = 1000;
	
	private static final int CONNECTION_TIME = 30 * 1000;

	private MemoryCache cache;
	private FileCache fileCache;

	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	
	private ExecutorService executorService;
	
	private int required_size;
	
	public LoadImage(Context context) {
		cache = new MemoryCache();
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(N_THREADS);
	}
	
	public void displayImage(ImageView imageview, String url) {
		imageViews.put(imageview, url);
		Bitmap bitmap = cache.get(url);
		if(bitmap != null)
			imageview.setImageBitmap(bitmap);
		else {
			queuePhoto(imageview, url);
			imageview.setImageResource(DEFAULT_IMG);
		}
	}
	
	private void queuePhoto(ImageView imageView, String url) {
		PhotoToLoad photo = new PhotoToLoad(imageView, url);
		executorService.submit(new PhotoLoader(photo));
	}
	
	private Bitmap getBitmap(String url) {
		File file = fileCache.getFile(url);
		
		Bitmap b = decodeFile(file);
		if(b != null)
			return b;
		try {
			Bitmap bitmap = null;
			
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(CONNECTION_TIME);
            conn.setReadTimeout(CONNECTION_TIME);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(file);
            Utils.copyStream(is, os);
            os.close();
            
            bitmap = decodeFile(file);
            return bitmap;
		} catch (Throwable t) {
			if(t instanceof OutOfMemoryError)
				cache.clear();
			return null;
		}
	}
	
	private Bitmap decodeFile(File file) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file), null, options);
			
			int width = options.outWidth;
			int height = options.outHeight;
			int scale = 1;
//			while(true) {
//				if(width/2 < REQUIRED_SIZE || height/2 < REQUIRED_SIZE)
//					break;
//				
//				width /= 2;
//				height /= 2;
//				scale *= 2;
//			}
			
			BitmapFactory.Options options2 = new BitmapFactory.Options();
			options2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(file), null, options2);
		} catch (Exception e) { }
		
		return null;
	}
	
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		
		public PhotoToLoad(ImageView iv, String u) {
			imageView = iv;
			url = u;
		}
	}
	
	private class PhotoLoader implements Runnable {
		PhotoToLoad photo;
		
		public PhotoLoader(PhotoToLoad photo) {
			this.photo = photo;
		}
		
		@Override
		public void run() {
			if(imageViewReused(photo))
				return;
			Bitmap bmp = getBitmap(photo.url);
			cache.put(photo.url, bmp);
			if(imageViewReused(photo))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photo);
			Activity activity = (Activity) photo.imageView.getContext();
			activity.runOnUiThread(bd);
		}		
	}
	
	private boolean imageViewReused(PhotoToLoad photo) {
		String tag = imageViews.get(photo.imageView);
		if(tag == null || !tag.equals(photo.url))
			return true;
		return false;
	}
	
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photo;
		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photo = p;
		}
		
		@Override
		public void run() {
			if(imageViewReused(photo))
				return;
			if(bitmap != null)
				photo.imageView.setImageBitmap(bitmap);
			else
				photo.imageView.setImageResource(DEFAULT_IMG);
		}
	}
	
	public void clearCache() {
		cache.clear();
		fileCache.clear();
	}
}
