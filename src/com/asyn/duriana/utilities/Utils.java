package com.asyn.duriana.utilities;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	
	public static final int BUFFER_SIZE = 1024;
	
	/**
	 * 
	 * @param is
	 * @param os
	 */
	public static void copyStream(InputStream is, OutputStream os) {
		try {
			byte[] bytes = new byte[BUFFER_SIZE];
			while(true) {
				int count = is.read(bytes, 0, BUFFER_SIZE);
				if(count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception e) { }
	}
	
}
