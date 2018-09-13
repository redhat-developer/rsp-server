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
import java.net.URL;

public class URLUtils {
	
	private static final String URL_PROTO_FILE = "file";
	
	private URLUtils() {
	}

	/**
	 * Compares two URL for equality, but do not connect to do DNS resolution
	 *
	 * @param url1 a given URL
	 * @param url2 another given URL to compare to url1
	 *
	 * @since 3.5
	 * @return <code>true</code> if the URLs are equal, <code>false</code> otherwise
	 */
	public static boolean sameURL(URL url1, URL url2) {
		if (url1 == url2) {
			return true;
		}
		if (url1 == null ^ url2 == null) {
			return false;
		}
		// check if URL are file: URL as we may have two URL pointing to the same doc location
		// but with different representation - (i.e. file:/C;/ and file:C:/)
		final boolean isFile1 = URL_PROTO_FILE.equalsIgnoreCase(url1.getProtocol());//$NON-NLS-1$
		final boolean isFile2 = URL_PROTO_FILE.equalsIgnoreCase(url2.getProtocol());//$NON-NLS-1$
		if (isFile1 && isFile2) {
			File file1 = new File(url1.getFile());
			File file2 = new File(url2.getFile());
			return file1.equals(file2);
		}
		// URL1 XOR URL2 is a file, return false. (They either both need to be files, or neither)
		if (isFile1 ^ isFile2) {
			return false;
		}
		return getExternalForm(url1).equals(getExternalForm(url2));
	}

	/**
	 * Gets the external form of this URL. In particular, it trims any white space,
	 * removes a trailing slash and creates a lower case string.
	 * 
	 * @param url the URL to get the {@link String} value of
	 * @return the lower-case {@link String} form of the given URL
	 */
	private static String getExternalForm(URL url) {
		String externalForm = url.toExternalForm();
		if (externalForm == null) {
			return ""; //$NON-NLS-1$
		}
		externalForm = externalForm.trim();
		if (externalForm.endsWith("/")) { //$NON-NLS-1$
			// Remove the trailing slash
			externalForm = externalForm.substring(0, externalForm.length() - 1);
		}
		return externalForm.toLowerCase();

	}

}
