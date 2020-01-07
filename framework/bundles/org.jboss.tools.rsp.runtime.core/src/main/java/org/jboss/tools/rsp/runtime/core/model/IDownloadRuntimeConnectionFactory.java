/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.runtime.core.model;

import java.io.InputStream;
import java.net.URL;

public interface IDownloadRuntimeConnectionFactory {
	public InputStream createConnection(URL url, String user, String pass);
	public long getContentLength(URL url, String user, String pass);
}
