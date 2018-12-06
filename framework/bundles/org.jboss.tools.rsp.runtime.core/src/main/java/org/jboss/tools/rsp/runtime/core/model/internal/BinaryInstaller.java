/*************************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.util.internal.DownloadRuntimeOperationUtility;

public class BinaryInstaller implements IRuntimeInstaller {

	@Override
	public IStatus installRuntime(DownloadRuntime downloadRuntime, String unzipDirectory, String downloadDirectory,
			boolean deleteOnExit, TaskModel taskModel, IProgressMonitor monitor) {
		String user = (String)taskModel.getObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY);
		String pass = (String)taskModel.getObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY);
		
		monitor.beginTask("Install Runtime '" + downloadRuntime.getName() + "' ...", 100);//$NON-NLS-1$ //$NON-NLS-2$
		monitor.worked(1);
		try {
			File f = new DownloadRuntimeOperationUtility().download(unzipDirectory, downloadDirectory, 
					getDownloadUrl(downloadRuntime, taskModel), deleteOnExit, user, pass, new SubProgressMonitor(monitor, 80));
			File dest = new File(unzipDirectory, f.getName());
			boolean renamed = f.renameTo(dest);
			if( !renamed ) {
				try {
					Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch(IOException ioe) {
					throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, ioe.getMessage(), ioe));
				}
			}
			if (!dest.setExecutable(true)) {
				throw new CoreException(new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "Can't set executable bit to " + dest.getAbsolutePath()));
			}
			taskModel.putObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_HOME_DIRECTORY, unzipDirectory);
			taskModel.putObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_BIN, dest.getAbsolutePath());
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	private String getDownloadUrl(DownloadRuntime downloadRuntime, TaskModel taskModel) {
		if (downloadRuntime != null) {
			String dlUrl = downloadRuntime.getUrl();
			if (dlUrl == null) {
				return (String) taskModel.getObject(IDownloadRuntimeWorkflowConstants.DL_RUNTIME_URL);
			}
			return dlUrl;
		}
		return null;
	}

}
