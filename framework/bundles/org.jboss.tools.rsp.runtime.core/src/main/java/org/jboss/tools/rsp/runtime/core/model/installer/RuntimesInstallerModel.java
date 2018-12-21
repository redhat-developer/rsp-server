/*************************************************************************************
 * Copyright (c) 2013-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.installer;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.internal.BinaryInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.internal.ExtractionRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.internal.JavaJarRuntimeInstaller;

public class RuntimesInstallerModel {

	private static RuntimesInstallerModel manager = null;

	public static RuntimesInstallerModel getDefault() {
		if( manager == null )
			manager = new RuntimesInstallerModel();
		return manager;
	}
	
	/** default for testing purposes **/
	RuntimesInstallerModel() {
	}

	/** default for testing purposes **/
	static class RuntimeInstallerWrapper {

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

	/** protected for testing purposes **/
	protected List<RuntimeInstallerWrapper> loadInstallers() {
		List<RuntimeInstallerWrapper> list = new ArrayList<>();
		list.add(new RuntimeInstallerWrapper(
				IRuntimeInstaller.EXTRACT_INSTALLER, new ExtractionRuntimeInstaller()));
		list.add(new RuntimeInstallerWrapper(
				IRuntimeInstaller.BINARY_INSTALLER, new BinaryInstaller()));
		list.add(new RuntimeInstallerWrapper(
				IRuntimeInstaller.JAVA_JAR_INSTALLER, new JavaJarRuntimeInstaller()));
		return list;
	}
	
	/**
	 * Get a runtime installer by the given id. 
	 * 
	 * @param id
	 * @return
	 */
	public IRuntimeInstaller getRuntimeInstaller(String id) {
		if (installers == null) {
			installers = loadInstallers();
		}
		return installers.stream()
			.filter(wrapper -> wrapper.getId().equals(id))
			.findFirst()
			.map(RuntimeInstallerWrapper::getInstaller)
			.orElse(null);
	}
	
}
