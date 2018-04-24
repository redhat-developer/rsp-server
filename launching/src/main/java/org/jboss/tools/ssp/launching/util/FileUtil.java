package org.jboss.tools.ssp.launching.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtil {
	public static String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile));
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
	

	public static Properties loadProperties(File f) {
		Properties p = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(f); 
			p.load(stream);
			return p;
		} catch(IOException ioe) {
			return p;
		} finally {
			if( stream != null ) {
				try {
					stream.close();
				} catch(IOException ioe) {
					// Do nothing
				}
			}
		}
	}

	

	
	public static String asPath(String... vals) {
		StringBuffer sb = new StringBuffer();
		for ( String v : vals ) {
			sb.append(v);
			sb.append(File.separatorChar);
		}
		String s = sb.toString();
		s = s.substring(0, s.length() - 1);
		return s;
	}
	
	public static boolean deleteDirectory(File dir, boolean tld) { 
		if (dir.isDirectory()) { 
			File[] children = dir.listFiles(); 
			for (int i = 0; i < children.length; i++) { 
				boolean success = deleteDirectory(children[i], true); 
				if (!success) { 
					return false; 
				} 
			} 
		}
		if( tld )
			return dir.delete();
		return true;
	}
}
