/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.internal.BinaryInstaller;
import org.jboss.tools.rsp.runtime.core.model.internal.ExtractionRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.internal.JavaJarRuntimeInstaller;

public class RuntimesInstallerModel {

	private static RuntimesInstallerModel manager = null;

	public static RuntimesInstallerModel getDefault() {
		if( manager == null )
			manager = new RuntimesInstallerModel();
		return manager;
	}
	
	private static class RuntimeInstallerWrapper {
		private String id;
		private IRuntimeInstaller installer;
		public RuntimeInstallerWrapper(String id, IRuntimeInstaller installer) {
			this.id = id;
			this.installer = installer;
		}
		public String getId() {
			return id;
		}
		public IRuntimeInstaller getInstaller() {
			return installer;
		}
	}
	
	private List<RuntimeInstallerWrapper> installers;
	private List<RuntimeInstallerWrapper> loadInstallers() {
		List<RuntimeInstallerWrapper> list = new ArrayList<>();
		RuntimeInstallerWrapper archive = new RuntimeInstallerWrapper(
				IRuntimeInstaller.EXTRACT_INSTALLER, new ExtractionRuntimeInstaller());
		RuntimeInstallerWrapper bin = new RuntimeInstallerWrapper(
				IRuntimeInstaller.BINARY_INSTALLER, new BinaryInstaller());
		RuntimeInstallerWrapper installer = new RuntimeInstallerWrapper(
				IRuntimeInstaller.JAVA_JAR_INSTALLER, new JavaJarRuntimeInstaller());
		list.add(archive);
		list.add(bin);
		list.add(installer);
		return list;
	}
	
	/**
	 * Get a runtime installer by the given id. 
	 * 
	 * @param id
	 * @return
	 */
	public IRuntimeInstaller getRuntimeInstaller(String id) {
		if( installers == null ) {
			installers = loadInstallers();
		}
		Iterator<RuntimeInstallerWrapper> it = installers.iterator();
		while(it.hasNext()) {
			RuntimeInstallerWrapper w = it.next();
			if(id.equals(w.getId())) {
				return w.getInstaller();
			}
		}
		return null;
	}
	
}
