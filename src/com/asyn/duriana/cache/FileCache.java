package com.asyn.duriana.cache;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileCache {
	
	private static final String IMAGES_CACHE = "Duriana Task Cache";
	
	private File cacheDir;
	
	public FileCache(Context context) {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			cacheDir = new File(Environment.getExternalStorageDirectory(), IMAGES_CACHE);
		else
			cacheDir = context.getCacheDir();
		
		if(!cacheDir.exists())
			cacheDir.mkdirs();
	}
	
	public File getFile(String url) {
		String fileName = String.valueOf(url.hashCode());
		// TODO
		// String fileName = URLEncoder.encode(url)
		return new File(cacheDir, fileName);
	}
	
	public void clear() {
		File[] files = cacheDir.listFiles();
		if(files == null)
			return;
		
		for (File file : files)
			file.delete();
	}	
	
}
