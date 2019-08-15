/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.discovery;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathListener;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;

public class DiscoveryPathModel implements IDiscoveryPathModel {
	private List<DiscoveryPath> paths;
	private List<IDiscoveryPathListener> listeners;

	public DiscoveryPathModel() {
		paths = new ArrayList<DiscoveryPath>();
		listeners = new ArrayList<IDiscoveryPathListener>();
	}
	
	public void addListener(IDiscoveryPathListener l) {
		listeners.add(l);
	}

	public void removeListener(IDiscoveryPathListener l) {
		listeners.remove(l);
	}

	public List<DiscoveryPath> getPaths() {
		return Collections.unmodifiableList(paths);
	}
	
	public boolean addPath(DiscoveryPath path) {
		if( !paths.contains(path)) {
			paths.add(path);
			for(IDiscoveryPathListener l : listeners ) {
				l.discoveryPathAdded(path);
			}
			return true;
		}
		return false;
	}
	
	public boolean removePath(DiscoveryPath path) {
		if( paths.contains(path)) {
			paths.remove(path);
			for(IDiscoveryPathListener l : listeners ) {
				l.discoveryPathRemoved(path);
			}
			return true;
		}
		return false;
	}

	public void loadDiscoveryPaths(File discoveryPathFile) throws IOException {
		if (!discoveryPathFile.exists()) {
			return;
		}
		Scanner scanner = new Scanner(discoveryPathFile);
		while (scanner.hasNextLine()) {
			String discoveryPathString = scanner.nextLine();
			if( discoveryPathFile == null || discoveryPathFile.length() == 0 ) {
				continue;
			}
			addPath(new DiscoveryPath(discoveryPathString));
		}
		scanner.close();
	}

	public void saveDiscoveryPaths(File discoveryPathFile) throws IOException {
		if (!discoveryPathFile.exists()) {
			if( !discoveryPathFile.createNewFile()) {
				throw new IOException();
			}
		}
		PrintWriter pw = new PrintWriter(discoveryPathFile);
		getPaths().forEach(path -> pw.println(path.getFilepath()));
		pw.close();
	}
}
