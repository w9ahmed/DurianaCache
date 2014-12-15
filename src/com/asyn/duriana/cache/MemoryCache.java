package com.asyn.duriana.cache;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

public class MemoryCache {

	// TODO
	// private static final String TAG = MemoryCache.class.getSimpleName();

	private Map<String, Bitmap> cache = Collections
			.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));

	private long size = 0L;
	private long limit = 1000000L;
	
	public MemoryCache() {
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}
	
	private void setLimit(long limit) {
		this.limit = limit;
	}
	
	public Bitmap get(String id) {
		try {
			if(!cache.containsKey(id))
				return null;
			return cache.get(id);
		} catch (Exception e) {
			return null;
		}
	}
	
	public void put(String id, Bitmap bitmap) {
		try {
			if(cache.containsKey(id))
				size -= getSizeInButes(cache.get(id));
			cache.put(id, bitmap);
			size += getSizeInButes(bitmap);
			checkSize();
		} catch (Exception e) { }
	}
	
	public void clear() {
		try {
			cache.clear();
			size = 0;
		} catch (Exception e) { }
	}
	
	private void checkSize() {
		if(size > limit) {
			Iterator<Entry<String, Bitmap>> iterator = cache.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Bitmap> entry = iterator.next();
				size -= getSizeInButes(entry.getValue());
				iterator.remove();
				if(size <= limit)
					break;
			}
		}
	}
	
	private long getSizeInButes(Bitmap bitmap) {
		if(bitmap == null)
			return 0;
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
	
}
