/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.extract.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.extract.IOverwrite;

public class UntarUtility implements IExtractUtility {
	private static final String SEPARATOR = "/"; //$NON-NLS-1$

	protected File file;
	private String discoveredRoot = null;
	private boolean rootEntryImpossible = false;

	public UntarUtility(File file) {
		this.file = file;
	}

	public IStatus extract(File dest, IOverwrite overwriteQuery, IProgressMonitor monitor) throws CoreException {
		String possibleRoot = null;
		try {
			dest.mkdir();
			TarArchiveInputStream tarIn = getTarArchiveInputStream(file);
			TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
			while (tarEntry != null) {
				String name = tarEntry.getName();
				File destPath = new File(dest, name);
				if (tarEntry.isDirectory()) {
					destPath.mkdirs();
				} else {
					if( !destPath.createNewFile() ) {
						String msg = NLS.bind("Error extracting runtime: Could not create file {0}", destPath.toString());
						throw new CoreException(
								new Status(IStatus.ERROR, 
										RuntimeCoreActivator.PLUGIN_ID, 0, msg, null));
					}
					byte[] btoRead = new byte[1024];
					try (BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath))) {
						int length = 0;
						while((length = tarIn.read(btoRead)) != -1) {
							bout.write(btoRead,0,length);
						}
					}
				}

				// Lets check for a possible root, to avoid scanning the archive again later
				possibleRoot = checkForPossibleRootEntry(possibleRoot, name);

				tarEntry = tarIn.getNextTarEntry();
			}
			tarIn.close();
		} catch(IOException ioe) {
			throw new CoreException(
					new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, 0, NLS.bind("Error extracting runtime {0}", ioe.getLocalizedMessage()), ioe)); //$NON-NLS-1$
		}
		this.discoveredRoot = possibleRoot;
		return Status.OK_STATUS;
	}

	protected String checkForPossibleRootEntry(String possibleRoot, String name) {
		if (!rootEntryImpossible && discoveredRoot == null) {
			// Check for a root
			if (name == null || name.isEmpty() || name.startsWith(SEPARATOR) || name.indexOf(SEPARATOR) == -1) {
				rootEntryImpossible = true;
				return null;
			} else {
				String directory = name.substring(0, name.indexOf(SEPARATOR));
				if (possibleRoot == null) {
					return directory;
				} else if (!directory.equals(possibleRoot)) {
					rootEntryImpossible = true;
					return null;
				}
			}
		}
		return possibleRoot;
	}

	protected TarArchiveInputStream getTarArchiveInputStream(File file) throws IOException {
		return new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
	}

	public String getRoot(IProgressMonitor monitor) throws CoreException {
		// IF we found a root during the extract, use that.
		if( discoveredRoot != null ) 
			return discoveredRoot;
		if( rootEntryImpossible)
			return null;
		// We don't have the .tar file anymore, so can't analyze it. 
		return null;
	}
}
