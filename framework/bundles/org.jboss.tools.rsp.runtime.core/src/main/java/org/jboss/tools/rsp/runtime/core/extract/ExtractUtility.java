/*************************************************************************************
 * Copyright (c) 2014-2019 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.extract;

import java.io.File;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.extract.internal.IExtractUtility;
import org.jboss.tools.rsp.runtime.core.extract.internal.UntarGZUtility;
import org.jboss.tools.rsp.runtime.core.extract.internal.UntarUtility;
import org.jboss.tools.rsp.runtime.core.extract.internal.UntarXZUtility;
import org.jboss.tools.rsp.runtime.core.extract.internal.UnzipUtility;

public class ExtractUtility {

	private static final String ZIP_SUFFIX = "zip"; //$NON-NLS-1$
	private static final String TAR_SUFFIX = "tar"; //$NON-NLS-1$
	private static final String TAR_GZ_SUFFIX = ".gz"; //$NON-NLS-1$
	private static final String TGZ_SUFFIX = ".tgz"; //$NON-NLS-1$
	private static final String TAR_XZ_SUFFIX = ".xz";
	
	public static final int FORMAT_UNKNOWN = -1;
	public static final int FORMAT_ZIP = 1;
	public static final int FORMAT_TAR = 2;
	public static final int FORMAT_TGZ = 3;
	public static final int FORMAT_XZ = 4;
	
	
	private final File file;
	private final IExtractUtility util;

	public ExtractUtility(File file) {
		this(file, FORMAT_UNKNOWN);
	}

	public ExtractUtility(File file, int format) {
		this.file = file;
		if (FORMAT_UNKNOWN == format) {
			this.util = getUtil(file);
		} else {
			this.util = getUtil(file, format);
		}
	}

	private IExtractUtility getUtil(File file, int format) {
		switch(format) {
			case FORMAT_ZIP:
				return new UnzipUtility(file);
			case FORMAT_TAR:
				return new UntarUtility(file);
			case FORMAT_TGZ:
				return new UntarGZUtility(file);
			case FORMAT_XZ:
				return new UntarXZUtility(file);
			default:
				return null;
		}
	}

	private IExtractUtility getUtil(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(ZIP_SUFFIX)) {
			return new UnzipUtility(file);
		} else if (name.endsWith(TAR_SUFFIX)) {
			return new UntarUtility(file);
		} else if (name.endsWith(TAR_GZ_SUFFIX) || name.endsWith(TGZ_SUFFIX)) {
			return new UntarGZUtility(file);
		} else if (name.endsWith(TAR_XZ_SUFFIX)) {
			return new UntarXZUtility(file);
		} else {
			return null;
		}
	}

	public File getOriginalFile() {
		return file;
	}
	
	public IStatus extract(File destination, IOverwrite overwriteQuery, IProgressMonitor monitor) {
		if (util != null) {
			try {
				return util.extract(destination, overwriteQuery, monitor);
			} catch(CoreException ce) {
				return new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "Error extracting file " + file.getAbsolutePath(), ce);
			}
		}
		return new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "Unable to discover how to extract file " + file.getAbsolutePath());
	}
	
	public String getExtractedRootFolder(IProgressMonitor monitor) throws CoreException {
		if (util != null)
			return util.getRoot(monitor);
		return null;
	}
	
}
