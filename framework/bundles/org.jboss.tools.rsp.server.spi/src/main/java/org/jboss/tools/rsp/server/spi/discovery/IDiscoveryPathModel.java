/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.discovery;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;

public interface IDiscoveryPathModel {

	public void addListener(IDiscoveryPathListener l);

	public void removeListener(IDiscoveryPathListener l);

	public List<DiscoveryPath> getPaths();
	
	/**
	 * Return whether the path was added.
	 * @param path
	 * @return
	 */
	public boolean addPath(DiscoveryPath path);
	
	/**
	 * Return whether the path was removed
	 * @param path
	 * @return
	 */
	public boolean removePath(DiscoveryPath path);
	
	public void loadDiscoveryPaths(File data) throws IOException;
	
	public void saveDiscoveryPaths(File data) throws IOException;
}
