/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.filewatcher;

import java.nio.file.Path;

public interface IFileWatcherService {

	public void start() throws IllegalStateException;
	
	public void stop();
	
	public void addFileWatcherListener(Path path, 
			IFileWatcherEventListener listener, boolean recursive);
	
	public void removeFileWatcherListener(Path path, IFileWatcherEventListener listener);
}
