/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.transport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.FoundationCoreActivator;
import org.jboss.tools.rsp.foundation.core.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class URLTransportCache {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(URLTransportCache.class);

	
	/**
	 * Encoding for this file
	 */
	private static final String ENCODING = "UTF-8";
	
	
	/**
	 * The new index file, to be stored in each cache root folder. 
	 */
	private static final String CACHE_INDEX_FILE = "URLTransportCache.cacheIndex.properties";
	
	
	/**
	 * A collection of all caches currently in use. 
	 * This is to ensure multiple clients don't try using 
	 * two instances with the same basedir, which could lead to corruption of the cache.
	 */
	private static final HashMap<IPath, URLTransportCache> cacheDirToCache = new HashMap<IPath, URLTransportCache>();

	public synchronized static URLTransportCache getCache(IPath root) {
		URLTransportCache c = cacheDirToCache.get(root);
		if( c == null ) {
			c = new URLTransportCache(root);
			cacheDirToCache.put(root, c);
		}
		return c;
	}
	
	
	private HashMap<String, String> cache;
	private IPath cacheRoot;
	protected URLTransportCache(IPath cacheRoot) {
		this.cacheRoot = cacheRoot;
		this.cache = new HashMap<>();
		load();
	}

	/**
	 * Get a cached file for the given url only if it is downloaded and exists.
	 * 
	 * @param url
	 * @return
	 */
	public File getCachedFile(String url) {
		String cacheVal = cache.get(url);
		if (cacheVal == null)
			return null;
		File f = new File(cacheVal);
		if (f.exists())
			return f;
		return null;
	}

	/**
	 * Check whether the cache is outdated
	 * @throws CoreException if the remote url is invalid, or the remote url cannot be reached
	 */
	public boolean isCacheOutdated(String url, IProgressMonitor monitor)
			throws CoreException {
		LOG.trace("Checking if cache is outdated for {}", url);
		File f = getCachedFile(url);
		if (f == null)
			return true;

		URL url2 = null;
		try {
			url2 = new URL(url);
		} catch (MalformedURLException murle) {
			throw new CoreException(new Status(IStatus.ERROR, FoundationCoreActivator.PLUGIN_ID, murle.getMessage(), murle));
		}

		long remoteModified = getLastModified(url2, monitor);

		// If the remoteModified is -1 but we have a local cache, use that (not outdated)
		if (remoteModified == -1 ) {
			if (f.exists())
				return false;
		}
		// !!! urlModified == 0 when querying files from github
		// It means that files from github can not be cached!
		if (f.exists()) {
			long modified = f.lastModified();
			if( remoteModified > modified ) {
				// The remote file has been updated *after* the local file was created, so, outdated
				return true;
			}
			if( remoteModified == 0 ) {
				// File comes from github or some other server not keeping accurate timestamps
				// so, possibly oudated, and must re-fetch 
				return true;
			}
			// Our local copy has a higher timestamp, so was fetched after
			return false;
		}
		// Local file doesn't exist, so, cache is outdated
		return true;
	}

	public File downloadAndCache(String url, String displayName,
			IProgressMonitor monitor) throws CoreException {
		return downloadAndCache(url, displayName, -1, false, monitor);
	}
	
	public File downloadAndCache(String url, String displayName, 
			int timeout, boolean deleteOnExit, IProgressMonitor monitor) throws CoreException {

		LOG.trace("Downloading and caching {}", url);

		File existing = getExistingRemoteFileCacheLocation(url);
		File target = createNewRemoteFileCacheLocation(url);
		try (FileOutputStream os = new FileOutputStream(target)){
			IStatus s = download(displayName, url, os, timeout, monitor);
			if (s.isOK()) {
				// Download completed successfully, add to cache, delete old copy
				if (deleteOnExit)
					target.deleteOnExit();	
				addToCache(url, target);
				if( existing != null && existing.exists())
					existing.delete();
				return target != null && target.exists() ? target : null;
			}
			// Download did not go as planned. Delete the new, return the old
			if( target != null && target.exists()) {
				target.delete();
			}
			return existing;
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
		}
		return null;
	}

	private void addToCache(String url, File target) {
		cache.put(url, target.getAbsolutePath());
		savePreferences();
	}
	
	private void load() {
		File index = cacheRoot.append(CACHE_INDEX_FILE).toFile();
		if( index.exists() && index.isFile()) {
			try {
				String contents = getContents(index);
				loadIndexFromString(contents);
			} catch(IOException ioe) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
		LOG.trace("Loaded {} cache file locations from preferences", cache.size());
	}
	
	private void loadIndexFromString(String val) {
		if( !isEmpty(val)) {
			String[] byLine = val.split("\n");
			for( int i = 0; i < byLine.length; i++ ) {
				if( isEmpty(byLine[i]))
					continue;
				String[] kv = byLine[i].split("=");
				if( kv.length == 2 && !isEmpty(kv[0]) && !isEmpty(kv[1])) {
					try {
						String decodedUrl = URLDecoder.decode(kv[0],ENCODING);
						if( new File(kv[1]).exists() )
							cache.put(decodedUrl,kv[1]);
					} catch(UnsupportedEncodingException uee) {
						// Should not be hit
						LOG.error(uee.getMessage(), uee);
					}
				}
			}
		}
	}
	
	
	private boolean isEmpty(String s) {
		return s == null || "".equals(s);
	}
	
	private void savePreferences() {
		// Saves are now done to an index file in the cache root. 
		File index = cacheRoot.append(CACHE_INDEX_FILE).toFile();
		
		LOG.trace("Saving {} cache file locations to {}", cache.size(), index.getAbsolutePath());

		StringBuilder sb = new StringBuilder();
		Iterator<String> it = cache.keySet().iterator();
		while(it.hasNext()) {
			String k = it.next();
			String v = cache.get(k);
			String encodedURL = null;
			try {
				encodedURL = URLEncoder.encode(k, ENCODING);
			} catch(UnsupportedEncodingException uee) {
				// Should never happen
			}
			if( encodedURL != null )
				sb.append(encodedURL + "=" + v + "\n");
		}

		try {
			setContents(index, sb.toString());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/*
	 * Get the existing cache location for the given url if it exists 
	 * @param url
	 * @return
	 */
	private synchronized File getExistingRemoteFileCacheLocation(String url) {
		// If this url is already cached, use it
		String cachedLoc = cache.get(url);
		if (cachedLoc != null) {
			File f = new File(cache.get(url));
			return f;
		}
		return null;
	}	
	
	private synchronized File createNewRemoteFileCacheLocation(String url) {
		// Otherwise, make a new one
		File root = getLocalCacheFolder().toFile();
		root.mkdirs();
		String tmp;
		try {
			tmp = DigestUtils.sha1(url);
		} catch (IOException O_o) {
		  //That really can't happen
			tmp = url.replaceAll("[\\p{Punct}&&[^_]]", "_");
		}
		
		File cached = null;
		do {
			cached = new File(root, 
					tmp + new SecureRandom().nextLong() + ".tmp");
		} while (cached.exists());

		return cached;
	}
	
	private IPath getLocalCacheFolder() {
		return cacheRoot;
	}
	
	/* 
	 * foundation.core has no IO utility classes. 
	 * If it gets some, this should be saved there. 
	 */
	
	private static String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile), ENCODING);
	}

	private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
	        byte[] bytes = new byte[(int)file.length()];
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	        return bytes;
        } finally {
            is.close();
        }
    }
	
	private static void setContents(File file, String contents) throws IOException {
		try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING))){
			out.append(contents);
		}
	}
	
	public long getLastModified(URL url2, IProgressMonitor monitor) {
		try {
			HttpURLConnection con = (HttpURLConnection) url2.openConnection();
			return getLastModified(con, monitor);
		} catch (IOException e1) {
			LOG.error("Could determine modification for url {}", url2);
		}
		return -1;
	}
	

	public long getLastModified(URL url, String user, String pass, IProgressMonitor monitor) {
		try {
			return getLastModified(getURLConnection(url, user, pass), monitor);
		} catch (IOException e) {
			LOG.error("Could determine modification for url {}", url);
		}
		return -1;
	}

	private long getLastModified(HttpURLConnection con, IProgressMonitor monitor) {
		try (AutoCloseable conc = () -> con.disconnect()) {
			return con.getLastModified();
		} catch (Exception e) {
			LOG.error("Could determine modification for url {}", con.getURL());
		}
		return -1;
	}

	
	private IStatus download(String displayName, String url, 
			FileOutputStream fileOutputStream, int timeout, 
			IProgressMonitor monitor) throws IOException {
		return download(displayName, createStream(url), fileOutputStream, timeout, monitor);
	}

	public IStatus download(String displayName, String url, 
			String user, String pass, 
			FileOutputStream fileOutputStream, int timeout,
			IProgressMonitor monitor) throws IOException {
		return download(displayName, createStream(url, user, pass), 
				fileOutputStream, timeout, monitor);
	}


	public IStatus download(String name, InputStream istream,
			FileOutputStream out, int timeout,
			IProgressMonitor monitor) throws IOException {
		// TODO respect timeout
		ReadableByteChannel readableByteChannel = null;
		FileChannel fileChannel = null;
		try {
			readableByteChannel = Channels.newChannel(istream);
			fileChannel = out.getChannel();
			fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			return Status.OK_STATUS;
		} finally {
	        if (readableByteChannel != null) {
	        	readableByteChannel.close();
	        }
	        if (fileChannel != null) {
	        	fileChannel.close();
	        }
		}
	}

	private InputStream createStream(String url) throws IOException {
		return new URL(url).openStream();
	}

	private InputStream createStream(String webPage, String user, String pass) throws IOException {
		return getURLConnection(webPage, user, pass).getInputStream();
	}
	
	private HttpURLConnection getURLConnection(String webPage, String user, String pass) throws IOException {
		URL url = new URL(webPage);
		return getURLConnection(url, user, pass);
	}

	private HttpURLConnection getURLConnection(URL url, String user, String pass) throws IOException {
		String authString = user + ":" + pass;
		byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
		String authStringEnc = new String(authEncBytes);

		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
		return urlConnection;
	}

}