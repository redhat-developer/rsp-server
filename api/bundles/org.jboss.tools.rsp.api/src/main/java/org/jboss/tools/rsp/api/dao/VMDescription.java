/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class VMDescription {
	private String id;
	private String installLocation;
	private String version;
	public VMDescription() {
		
	}
	public VMDescription(String id, String il, String v) {
		this.id = id;
		this.installLocation = il;
		this.version = v;
	}

	public String getId() {
		return id;
	}

	public String getInstallLocation() {
		return installLocation;
	}

	public String getVersion() {
		return version;
	}
}
