/*******************************************************************************
 * Copyright (c) 2014 Red Hat 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat
 *******************************************************************************/
package org.jboss.tools.rsp.runtime.core.util.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.foundation.core.transport.URLTransportCache;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.extract.ExtractUtility;
import org.jboss.tools.rsp.runtime.core.extract.IOverwrite;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;

/**
 * Mixed class of core+ui to initiate the download, unzipping, 
 * and runtime creation for a downloaded runtime. 
 */
public class DownloadRuntimeOperationUtility {

	protected File getNextUnusedFilename(File destination, String name) {
		String nameWithoutSuffix = null;
		if( name.indexOf('.') == -1 ) {
			nameWithoutSuffix = name;
		} else if (name.endsWith(".tar.gz")) {
			nameWithoutSuffix = name.substring(0, name.length() - ".tar.gz".length());
		} else { 
			nameWithoutSuffix = name.substring(0, name.lastIndexOf('.'));
		}
		String suffix = name.substring(nameWithoutSuffix.length());
		int i = 1;
		String tmpName = null;
		File file = new File (destination, name);
		while (file.exists()) {
			tmpName = nameWithoutSuffix + "(" + i++ + ")" + suffix; //$NON-NLS-1$ //$NON-NLS-2$
			file = new File(destination, tmpName); 
		}
		return file;
	}
	
	
	/**
	 * 
	 * @param downloadDestinationPath   The path to put the downloaded zip
	 * @param urlString					The remote url
	 * @param deleteOnExit				Whether to delete on exit or not
	 * @return
	 */
	private File getDestinationFile(String downloadDestinationPath, String urlString, boolean deleteOnExit) throws CoreException {
		File ret = null;
		try {
			URL url = new URL(urlString);
			String name = url.getPath();
			int slashIdx = name.lastIndexOf('/');
			if (slashIdx >= 0)
				name = name.substring(slashIdx + 1);

			File destination = new File(downloadDestinationPath);
			destination.mkdirs();
			ret = new File (destination, name);
			if (deleteOnExit) {
				ret = getNextUnusedFilename(destination, name);
			}
			if( deleteOnExit )
				ret.deleteOnExit();
			return ret;
		} catch (IOException e) {
			cancel(ret);
			IStatus s = new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, e.getMessage(), e);
			throw new CoreException(s);
		}
	}
	
	private boolean cacheOutdated(File local, boolean deleteOnExit) {
		boolean download = true;
		long urlModified = 0;
		if (!deleteOnExit) {
			long cacheModified = local.lastModified();
			download = cacheModified <= 0 || cacheModified != urlModified;
		}
		return download;
	}
	
	
	private long getRemoteURLModified(String urlString, String user, String pass, IProgressMonitor monitor) throws CoreException, IOException {
		monitor.beginTask("Checking remote timestamp", 100);
		long l = getCache().getLastModified(new URL(urlString), user, pass, monitor);
		monitor.worked(100);
		monitor.done();
		return l;
	}
	
	private void validateInputs(String downloadDirectoryPath, String unzipDirectoryPath) throws CoreException {
		File downloadDirectory = new File(downloadDirectoryPath);
		downloadDirectory.mkdirs();
		if (!downloadDirectory.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "The '" + downloadDirectory + "' is not a directory.")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		File unzipDirectory = new File(unzipDirectoryPath);
		unzipDirectory.mkdirs();
		if (!unzipDirectory.isDirectory()) {
			throw new CoreException( new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "The '" + unzipDirectory + "' is not a directory.")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	

	public File download(String unzipDirectoryPath, String downloadDirectoryPath, 
			String urlString, boolean deleteOnExit, String user, String pass, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Download runtime from url " + urlString, 500);
		try {
			validateInputs(downloadDirectoryPath, unzipDirectoryPath);
			File downloadedFile = downloadRemoteRuntime(downloadDirectoryPath, 
					urlString, deleteOnExit, user, pass, new SubProgressMonitor(monitor, 500));
			return downloadedFile;
		} finally {
			monitor.done();
		}
	}

	

	public IStatus downloadAndUnzip(String unzipDirectoryPath, String downloadDirectoryPath, 
			String urlString, boolean deleteOnExit, String user, String pass, TaskModel tm, IProgressMonitor monitor) {
		monitor.beginTask("Configuring runtime from url " + urlString, 500);
		try {
			validateInputs(downloadDirectoryPath, unzipDirectoryPath);
			File downloadedFile = downloadRemoteRuntime(downloadDirectoryPath, urlString, deleteOnExit, user, pass, new SubProgressMonitor(monitor, 450));
			ExtractUtility extractUtil = new ExtractUtility(downloadedFile);
			IOverwrite ow = (IOverwrite)tm.getObject(IDownloadRuntimeWorkflowConstants.OVERWRITE);
			if( ow == null ) {
				ow = createOverwriteFileQuery();
			}
			unzip(extractUtil, downloadedFile, unzipDirectoryPath, ow, new SubProgressMonitor(monitor, 30));
			String updatedRuntimeRoot = getUpdatedUnzipPath(extractUtil, unzipDirectoryPath, new SubProgressMonitor(monitor, 10));
			tm.putObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_HOME_DIRECTORY, updatedRuntimeRoot);
		} catch(CoreException ce) {
			return new Status(ce.getStatus().getSeverity(), RuntimeCoreActivator.PLUGIN_ID, NLS.bind("Error while retrieving runtime from {0}", urlString), ce);  //$NON-NLS-1$
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	
	private File downloadRemoteRuntime(String destinationDirectory, 
			String urlString, boolean deleteOnExit, String user, String pass, IProgressMonitor monitor) throws CoreException  {
		monitor.beginTask("Downloading " + urlString, 1000);
		File file = null;
		try {
			file = getDestinationFile(destinationDirectory, urlString, deleteOnExit);
			
			long urlModified = 0;
			if( !deleteOnExit ) {
				try {
					urlModified = getRemoteURLModified(urlString, user, pass, new SubProgressMonitor(monitor, 100));
				} catch(IOException ioe) {
					// Ignore error on checking timestamp, may be fluke
				} catch(CoreException ce) {
					// Ignore error on checking timestamp, may be fluke
				}
			}
			boolean download = cacheOutdated(file, deleteOnExit);

			IStatus result = Status.OK_STATUS;
			if (download) {
				result = downloadFileFromRemoteUrl(file, new URL(urlString), urlModified, user, pass, new SubProgressMonitor(monitor, 900));
			}
			if( !result.isOK())
				throw new CoreException(result);
			if (monitor.isCanceled())
				throw new CoreException(cancel(file));
			
			return file;
		} catch (IOException  e) {
			cancel(file);
			throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}
	
	private void unzip(ExtractUtility util, File downloadedFile, String unzipDirectoryPath, IOverwrite overwriteQuery, IProgressMonitor monitor) throws CoreException  {
		monitor.beginTask("Unzipping " + downloadedFile.getAbsolutePath(), 1000);
		if (monitor.isCanceled())
			throw new CoreException(cancel(downloadedFile));

		final IStatus status = util.extract(new File(unzipDirectoryPath), overwriteQuery, new SubProgressMonitor(monitor, 1000));
		if (monitor.isCanceled())
			throw new CoreException( cancel(downloadedFile));
		if( !status.isOK())
			throw new CoreException(status);
	}
	
	private IStatus cancel(File f) {
		if( f != null ) {
			f.deleteOnExit();
			f.delete();
		}
		return Status.CANCEL_STATUS;
	}
	
	private IOverwrite createOverwriteFileQuery() {
		return (File file) -> IOverwrite.YES;
	}
	
	private String getUpdatedUnzipPath(ExtractUtility util, String unzipDirectoryPath, IProgressMonitor monitor) throws CoreException {
		try {
			String root = util.getExtractedRootFolder( new SubProgressMonitor(monitor, 10));
			if (root != null) {
				File rootFile = new File(unzipDirectoryPath, root);
				if (rootFile.exists()) {
					unzipDirectoryPath = rootFile.getAbsolutePath();
				}
			}
			return unzipDirectoryPath;
		} catch(CoreException ce) {
			cancel(util.getOriginalFile());
			throw ce;
		} finally {
			monitor.done();
		}
	}
	
	private IStatus downloadFileFromRemoteUrl(File toFile, URL url, long remoteUrlModified, String user, String pass, IProgressMonitor monitor) throws IOException {
		try (FileOutputStream out = new FileOutputStream(toFile)) {
			IStatus result = getCache().download(
					toFile.getName(), url.toExternalForm(), user, pass, out, -1, monitor);
			out.flush();
			if (remoteUrlModified > 0) {
				toFile.setLastModified(remoteUrlModified);
			}
			return result;
		}
	}

	
	private URLTransportCache cache;

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
	
	private URLTransportCache getCache() {
		if( cache == null ) {
			File data = LaunchingCore.getDataLocation();
			File stacks = new File(data, "downloadruntimes");
			cache = URLTransportCache.getCache(new Path(stacks.getAbsolutePath()));
		}
		return cache;
	}
}
