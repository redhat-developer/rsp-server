/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Http requests utility class.
 * @author odockal
 *
 */
public class HttpUtility {

	private static Logger log = Logger.getLogger(HttpUtility.class.getName());
	
    public static int getHttpStatusCode(String uri) throws IOException {
    	log.info("URL to check: " + uri);
    	HttpURLConnection connection = constructHttpRequest(uri, "GET");
		int responseCode = connection.getResponseCode();
		connection.disconnect();
		return responseCode;
    }
    
    public static void waitForUrlEndpoint(URL url, int statusCode, int attempts) throws IOException, InterruptedException {
    	int tries = 0;
    	int actualCode = 0;
    	while(tries++ < attempts) {
    		actualCode = getHttpStatusCode(url.toString());
    		if (actualCode == statusCode) {
    			return;
    		}
    		Thread.sleep(1000);
    	}
    	fail("Failed to get expected endpoint on url " + url.toString()
    			+ " to be available with status code " + statusCode + ", code was " + actualCode);
    }
    
    public static HttpURLConnection constructHttpRequest(String uri, String method) throws IOException {
    	URL url = null;
    	try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			fail("Malformed URL: " + uri);
		}
    	return setupHttpURLConnection(url, method);
    }
    
    public static HttpURLConnection setupHttpURLConnection(URL url, String method) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		HttpURLConnection.setFollowRedirects(true);
		connection.setRequestProperty("Content-Type", "application/xml");
		connection.setRequestProperty("Accept", "application/xml");
		connection.setReadTimeout(30000);
		return connection;
    }
}
