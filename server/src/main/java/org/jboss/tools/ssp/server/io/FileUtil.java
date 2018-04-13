package org.jboss.tools.ssp.server.io;

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
}
