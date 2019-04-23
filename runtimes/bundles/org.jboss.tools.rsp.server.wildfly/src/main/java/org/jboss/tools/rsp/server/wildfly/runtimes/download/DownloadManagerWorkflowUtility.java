/*************************************************************************************
 * Copyright (c) 2014-2019 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.rsp.server.wildfly.runtimes.download;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
/**
 * A utility class for running the remote download-manager header commands to
 * verify if the downloadRuntime is set to be downloaded.
 */
public class DownloadManagerWorkflowUtility {

	public static final int AUTHORIZED = 1;
	public static final int CREDENTIALS_FAILED = 2;
	public static final int WORKFLOW_FAILED = 3;

	private DownloadManagerWorkflowUtility() {
		// inhibit instantiation
	}
	
	/**
	 * Get one of three statuses via a header request on a jboss/redhat download URL
	 * with credentials.
	 * 
	 * @param dr
	 * @param userS
	 * @param passS
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */
	public static int getWorkflowStatus(DownloadRuntime dr, String userS, String passS)
			throws CoreException, IOException {
		int response = headerOnlyStatusCode(dr, userS, passS);
		if (response == 401) {
			// 401 means bad credentials, change nothing
			return CREDENTIALS_FAILED;
		} else if (response == 403
				|| response == 200) {
			// 403 means workflow incomplete / forbidden, need a child page
			return WORKFLOW_FAILED;
		} else if (response == 302) {
			// 302 means all's clear / redirect, no child page needed
			return AUTHORIZED;
		}
		throw new CoreException(
				new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Unknown response code: " + response));

	}

	private static int headerOnlyStatusCode(String url, String userS, String passS)
			throws IOException {
		HttpURLConnection con = getWorkflowConnection(url, userS, passS, "HEAD", true);
		int response = con.getResponseCode();
		con.disconnect();
		return response;
	}

	private static int headerOnlyStatusCode(DownloadRuntime dr, String userS, String passS)
			throws IOException {
		return headerOnlyStatusCode(dr.getUrl(), userS, passS);
	}
	
	// Example curl command:  
	// curl --verbose -L -u user:pass -H "Content-Type: application/xml" -H "Accept: application/xml"  -O https://www.jboss.org/download-manager/jdf/file/jboss-eap-6.3.0.GA.zip
	public static HttpURLConnection getWorkflowConnection(String url, String user, String pass, String requestMethod,
			boolean useXMLHeader) throws IOException {
		return getWorkflowConnection(url, user, pass, requestMethod, useXMLHeader, false, 30000);
	}
	public static HttpURLConnection getWorkflowConnection(String url, String user, String pass, String requestMethod,
			boolean useXMLHeader, boolean followRedirs, int timeout) throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setInstanceFollowRedirects(followRedirs);
		String userCredentials = user + ":" + pass;
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		con.setRequestProperty("Authorization", basicAuth);
		if (useXMLHeader) {
			con.setRequestProperty("Content-Type", "application/xml");
			con.setRequestProperty("Accept", "application/xml");
		}
		con.setRequestMethod(requestMethod);
		con.setReadTimeout(timeout);
		return con;
	}

	private static String findNextStep(String responseContent) {
		if (responseContent != null && !responseContent.isEmpty()) {
			XMLMemento m = XMLMemento.createReadRoot(new ByteArrayInputStream(responseContent.getBytes()));
			if (m != null) {
				IMemento workflow = m.getChild("workflow");
				IMemento step = workflow.getChild("step");
				String nextStep = ((XMLMemento) step).getTextData();
				return nextStep;
			}
		}
		return null;
	}

	/**
	 * Try to acquire the xml response indicating what the next step in the t&c is
	 * for this url. Due to a bug on the server, the current implementation will
	 * check both normal responses, and a gzip'd response. If the normal response is
	 * properly formatted xml, it will be used. If it is not, we will attempt to
	 * read the input stream through a gzip input stream, to see if that one returns
	 * properly formatted xml.
	 *
	 * @param dr
	 * @param userS
	 * @param passS
	 * @return A workflow response if it can be found, or null
	 * @throws IOException
	 */
	public static byte[] readURL(String url, String userS, String passS) throws IOException {
		HttpURLConnection con = getWorkflowConnection(url, userS, passS, "GET", true);

		// We need to get the content of this stream. If the inputstream fails, get the
		// error stream
		InputStream stream = null;
		try {
			stream = con.getInputStream();
		} catch (IOException e) {
			stream = con.getErrorStream();
		}

		// As byte array
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] bytes = buffer.toByteArray();
		con.disconnect();
		return bytes;
	}

	public static String asString(byte[] bytes) throws IOException {
		// Try to read it normally
		return asString(new ByteArrayInputStream(bytes));
	}

	public static String asStringGZip(byte[] bytes) throws IOException {
		return asString(new GZIPInputStream(new ByteArrayInputStream(bytes)));
	}

	private static String asString(InputStream stream) throws IOException {
		StringBuilder result = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ((line = br.readLine()) != null) {
			result.append(line);
			result.append('\n');
		}
		br.close();
		return result.toString();
	}

	public static String getWorkflowResponseContent(DownloadRuntime dr, String userS, String passS) throws IOException {
		return getWorkflowResponseContent(dr.getUrl(), userS, passS);
	}

	public static String getWorkflowResponseContent(String url, String userS, String passS) throws IOException {
		byte[] bytes = readURL(url, userS, passS);
		String result = null;
		String nextStep = null;
		try {
			result = asString(bytes);
			// Parse the response to see the next step in the process
			nextStep = findNextStep(result);
		} catch (IOException ioe) {
			// Do nothing, but try again as gzip
		}

		// If the result was not properly formatted xml, try via gzip
		if (nextStep == null) {
			result = asStringGZip(bytes);
		}
		return result;
	}
}
