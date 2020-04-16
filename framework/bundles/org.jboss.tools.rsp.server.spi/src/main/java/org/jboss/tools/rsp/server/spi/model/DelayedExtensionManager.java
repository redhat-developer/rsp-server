/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model;

import java.util.ArrayList;

public class DelayedExtensionManager {
	private static DelayedExtensionManager INSTANCE = new DelayedExtensionManager();
	public static DelayedExtensionManager getDefault() {
		return INSTANCE;
	}
	
	private ArrayList<IDelayedExtension> list = new ArrayList<>();
	
	public interface IDelayedExtension {
		public void addExtensionsToModel();
	}
	
	public void addDelayedExtension(IDelayedExtension e) {
		list.add(e);
	}
	
	public IDelayedExtension[] getDelayedExtensions() {
		return (IDelayedExtension[]) list.toArray(new IDelayedExtension[list.size()]);
	}
	
}
