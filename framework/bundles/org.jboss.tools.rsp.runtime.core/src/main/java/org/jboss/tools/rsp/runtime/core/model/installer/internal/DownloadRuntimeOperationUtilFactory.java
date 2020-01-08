/*************************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.installer.internal;

import java.io.InputStream;
import java.net.URL;

import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeConnectionFactory;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.util.internal.DownloadRuntimeOperationUtility;

public class DownloadRuntimeOperationUtilFactory {

	public static DownloadRuntimeOperationUtility createDownloadRuntimeOperationUtility(
			TaskModel tm, IDownloadRuntimesModel downloadRuntimesModel) {
		IDownloadRuntimeConnectionFactory fact = (IDownloadRuntimeConnectionFactory)tm.getObject(
				IDownloadRuntimeWorkflowConstants.CONNECTION_FACTORY);
		if (fact == null) {
			return new DownloadRuntimeOperationUtility(downloadRuntimesModel) {

				@Override
				protected long getContentLength(URL url, String user, String pass) {
					Long expectedSize = (Long)tm.getObject(IDownloadRuntimeWorkflowConstants.DL_RUNTIME_SIZE);
					if( expectedSize != null ) {
						return expectedSize.longValue();
					}
					return -1;
				}
			};
		} else {
			return new DownloadRuntimeOperationUtility(downloadRuntimesModel) {
				@Override
				protected InputStream createDownloadInputStream(URL url, String user, String pass) {
					return fact.createConnection(url, user, pass);
				}

				@Override
				protected long getContentLength(URL url, String user, String pass) {
					long ret = fact.getContentLength(url, user, pass);
					if( ret == -1 ) {
						Long expectedSize = (Long)tm.getObject(IDownloadRuntimeWorkflowConstants.DL_RUNTIME_SIZE);
						if( expectedSize != null ) {
							return expectedSize.longValue();
						}
					}
					return ret;
				}
			};
		}
	}
	
}
