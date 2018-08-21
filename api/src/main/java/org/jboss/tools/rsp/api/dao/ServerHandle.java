/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ServerHandle {
	private String id;
	private ServerType type;
	public ServerHandle() {
		
	}
	public ServerHandle(String id, ServerType type) {
		this.id = id;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}

	public ServerType getType() {
		return type;
	}
	public String toString() {
		return type.toString() + ":" + id;
	}
}
