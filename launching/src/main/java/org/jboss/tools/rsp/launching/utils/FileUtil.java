/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtil {
	
	private FileUtil() {
	}

	public static String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile));
	}

	public static byte[] getBytesFromFile(File file) throws IOException {

        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
		try (InputStream is = new FileInputStream(file)) {
			while (offset < bytes.length 
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
		}
        return bytes;
    }

	public static Properties loadProperties(File f) {
		Properties p = new Properties();
		try (FileInputStream stream = new FileInputStream(f)){
			p.load(stream);
			return p;
		} catch(IOException ioe) {
			return p;
		}
	}
	
	public static String asPath(String... vals) {
		if (vals == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
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
		if (tld)
			return dir.delete();
		return true;
	}
}
