/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

public interface IServerWorkingCopy extends IServerAttributes {
	public void setAttribute(String attributeName, int value);

	public void setAttribute(String attributeName, boolean value);

	public void setAttribute(String attributeName, String value);

	public void setAttribute(String attributeName, List<String> value);

	public void setAttribute(String attributeName, Map<?,?> value);
	
	void save(IProgressMonitor monitor) throws CoreException;
}
