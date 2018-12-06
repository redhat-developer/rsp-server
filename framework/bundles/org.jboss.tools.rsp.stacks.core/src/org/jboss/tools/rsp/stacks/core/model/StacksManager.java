/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.stacks.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.jdf.stacks.client.DefaultStacksClientConfiguration;
import org.jboss.jdf.stacks.client.StacksClient;
import org.jboss.jdf.stacks.client.StacksClientConfiguration;
import org.jboss.jdf.stacks.client.messages.StacksMessages;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.jdf.stacks.parser.Parser;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.foundation.core.transport.URLTransportCache;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A StacksManager is in charge of retrieving a file from a URL or standard
 * location and returning a jdf.stacks model object generated via the stacks
 * client.
 */
public class StacksManager {

	private static final Logger LOG = LoggerFactory.getLogger(StacksManager.class);

	@Deprecated
	private static final String STACKS_URL_PROPERTY = "org.jboss.examples.stacks.url";

	private static final String URL_PROPERTY_STACKS = "org.jboss.tools.stacks.url_stacks";
	private static final String URL_PROPERTY_PRESTACKS = "org.jboss.tools.stacks.url_prestacks";

	private static final String STACKS_URL;
	private static final String PRESTACKS_URL;

	// Declare the types of stacks available for fetch
	public enum StacksType {
		STACKS_TYPE, PRESTACKS_TYPE
	}

	// Load the default stacks url and prestacks url from a sysprop or jar
	static {
		STACKS_URL = System.getProperty(URL_PROPERTY_STACKS, System.getProperty(STACKS_URL_PROPERTY,
				System.getProperty(StacksClientConfiguration.REPO_PROPERTY, getStacksDefaultUrlFromJar())));
		PRESTACKS_URL = System.getProperty(URL_PROPERTY_PRESTACKS,
				System.getProperty("jdf.prestacks.client.repo", getPreStacksDefaultUrlFromJar()));
	}

	/**
	 * Fetch the default stacks model.
	 * 
	 * @param monitor
	 * @return
	 */
	public Stacks getStacks(IProgressMonitor monitor) {
		Stacks[] all = getStacks("Fetching JBoss Stacks", monitor, StacksType.STACKS_TYPE);
		if (all != null && all.length > 0)
			return all[0];
		return null;
	}

	/**
	 * Fetch an array of stacks models where each element represents one of the
	 * StacksType urls
	 * 
	 * @param jobName
	 * @param monitor
	 * @param types
	 * @return
	 */
	public Stacks[] getStacks(String jobName, IProgressMonitor monitor, StacksType... types) {
		if (types == null)
			return new Stacks[0];
		LOG.trace("Request received for {} stacks types.", types.length);
		List<Stacks> ret = new ArrayList<>(types.length);
		monitor.beginTask(jobName, types.length * 100);
		for (int i = 0; i < types.length; i++) {
			switch (types[i]) {
			case STACKS_TYPE:
				LOG.trace("Loading Stacks Model from {}", STACKS_URL);
				Stacks s = getStacks(STACKS_URL, jobName, new SubProgressMonitor(monitor, 50));
				if (s == null && !monitor.isCanceled()) {
					LOG.warn("Stacks from {} can not be read, using client mechanism instead", STACKS_URL );
					s = getDefaultStacksFromClient(new SubProgressMonitor(monitor, 50));
				}
				if (s != null)
					ret.add(s);
				break;
			case PRESTACKS_TYPE:
				// Pre-stacks has no fall-back mechanism at this time
				LOG.trace("Loading Stacks Model from {}", PRESTACKS_URL);
				Stacks s2 = getStacks(PRESTACKS_URL, jobName, new SubProgressMonitor(monitor, 100));
				if (s2 != null)
					ret.add(s2);
				break;
			default:
				break;
			}
		}
		monitor.done();
		return ret.toArray(new Stacks[ret.size()]);
	}

	/**
	 * Fetch the stacks model representing a given arbitrary url. The remote file
	 * will be cached only until the system exits.
	 * 
	 * @param url
	 * @param monitor
	 * @return
	 */
	public Stacks getStacks(String url, IProgressMonitor monitor) {
		return getStacksFromURL(url, url, monitor);
	}

	/**
	 * Fetch the stacks model for a given url. Cache the remote file with a duration
	 * representing forever, or, until the remote file is newer.
	 * 
	 * @param url     The url
	 * @param jobName Job name for display purposes
	 * @param monitor
	 * @return
	 */
	protected Stacks getStacks(String url, String jobName, IProgressMonitor monitor) {
		return getStacksFromURL(url, jobName, monitor);
	}

	protected Stacks getStacksFromURL(String url, String jobName, IProgressMonitor monitor) {

		Stacks stacks = null;
		try {
			LOG.trace("Locating or downloading file for {}", url);
			File f = getCachedFileForURL(url, jobName, monitor);
			return getStacksFromFile(f);
		} catch (Exception e) {
			LOG.error("Can't access or parse  " + url, e); //$NON-NLS-1$
		}
		return stacks;
	}

	protected Stacks getStacksFromFile(File f) throws IOException {
		if (f != null && f.exists()) {
			LOG.trace("Local file for url exists");
			try(FileInputStream fis = new FileInputStream(f)) {
				Parser p = new Parser();
				return p.parse(fis);
			}
		}
		return null;
	}

	private Stacks getDefaultStacksFromClient(IProgressMonitor monitor) {
		if (!monitor.isCanceled()) {
			final StacksClient client = new StacksClient(new DefaultStacksClientConfiguration(),
					new JBTStacksMessages());
			return runWithTimeout(5000, client::getStacks);
		}
		return null;
	}

	public static <R> R runWithTimeout(long millisTimeout, Callable<R> callable) {
		ExecutorService singleThreadExecutor = Executors.newFixedThreadPool(1);
		Future<R> future = singleThreadExecutor.submit(callable);
		try {
			return future.get(millisTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
		} finally {
			singleThreadExecutor.shutdown();
		}
		return null;
	}

	/**
	 * Fetch a local cache of the remote file. If the remote file is newer than the
	 * local, update it.
	 * 
	 * @param url     A url to fetch the stacks model from
	 * @param jobName A job name passed into the downloader for display purposes
	 * @param monitor A file representign the model
	 * @return
	 */
	protected File getCachedFileForURL(String url, String jobName, IProgressMonitor monitor) throws CoreException {
		if( getCache().isCacheOutdated(url, monitor)) {
			return cache.downloadAndCache(url,jobName, 10000, true, monitor);
		} else {
			// Else use the local cache
			return cache.getCachedFile(url);
		}
	}
	
	private URLTransportCache cache;
	private URLTransportCache getCache() {
		if( cache == null ) {
			File data = LaunchingCore.getDataLocation();
			File stacks = new File(data, "stacks");
			cache = URLTransportCache.getCache(new Path(stacks.getAbsolutePath()));
		}
		return cache;
	}

	/*
	 * Read the stacks.yaml location from inside our client jar
	 */
	private static String getStacksDefaultUrlFromJar() {
		return getUrlFromJar(StacksClientConfiguration.REPO_PROPERTY);
	}

	private static String getPreStacksDefaultUrlFromJar() {
		return getUrlFromJar(StacksClientConfiguration.PRESTACKS_REPO_PROPERTY);
	}

	private static String getUrlFromJar(String prop) {
		InputStream is = null;
		try {
			is = StacksManager.class.getResourceAsStream("/org/jboss/jdf/stacks/client/config.properties"); //$NON-NLS-1$
			Properties p = new Properties();
			p.load(is);
			return p.getProperty(prop);
		} catch (Exception e) {
			LOG.warn("Can't read stacks url from the stacks-client.jar", e); //$NON-NLS-1$
		} finally {
			close(is);
		}
		return null;
	}

	private static class JBTStacksMessages implements StacksMessages {
		public void showDebugMessage(String arg0) {
			LOG.trace(arg0);
		}

		public void showInfoMessage(String arg0) {
			LOG.info(arg0);
		}

		public void showErrorMessage(String arg0) {
			LOG.error(arg0);
		}

		public void showErrorMessageWithCause(String arg0, Throwable t) {
			LOG.error(arg0, t);
		}

		public void showWarnMessage(String arg0) {
			LOG.warn(arg0);
		}

	}

	/*
	 * Close an inputstream
	 */
	private static void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException ie) {
				// IGNORE
			}
		}
	}

}
