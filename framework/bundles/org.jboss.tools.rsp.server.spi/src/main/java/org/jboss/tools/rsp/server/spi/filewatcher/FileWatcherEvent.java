/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.filewatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FileWatcherEvent {
	private Path path;
	private WatchEvent.Kind<?> kind;

	public FileWatcherEvent(Path path, WatchEvent.Kind<?> kind) {
		this.path = path;
		this.kind = kind;
	}

	public Path getPath() {
		return path;
	}

	public WatchEvent.Kind<?> getKind() {
		return kind;
	}
}
