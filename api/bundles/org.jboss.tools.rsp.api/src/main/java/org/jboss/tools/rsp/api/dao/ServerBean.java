/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ServerBean {
	public static final String UNKNOWN_STR = "UNKNOWN"; //$NON-NLS-1$
	public static final String EMPTY = ""; //$NON-NLS-1$

	private String location=EMPTY;
	private String typeCategory = UNKNOWN_STR;
	private String specificType = null;
	private String name = EMPTY;
	private String version = EMPTY;
	private String fullVersion = EMPTY;
	private String serverAdapterTypeId = EMPTY;
	public ServerBean() {
		
	}
	public ServerBean(String location, String name, 
			String typeCategory, String specificType,
			String fullVersion, String majorMinor, 
			String serverAdapterTypeId) {
		super();
		this.location = location;
		this.name = name;
		this.typeCategory = typeCategory;
		this.specificType = specificType;
		this.fullVersion = fullVersion;
		this.version = majorMinor;
		this.serverAdapterTypeId = serverAdapterTypeId;
	}
	
	public ServerBean(ServerBean bean) {
		this(bean.getLocation(),bean.getName(), 
				bean.getTypeCategory(), bean.getSpecificType(),
				bean.getFullVersion(), bean.getVersion(), 
				bean.getServerAdapterTypeId());
	}

	
	public String toString() {
		return name + "," + typeCategory + "," + version + "," + location; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(this == obj) return true;
		return this.toString().equals(obj.toString());
	}

	public static String getUnknownStr() {
		return UNKNOWN_STR;
	}

	public static String getEmpty() {
		return EMPTY;
	}

	public String getLocation() {
		return location;
	}

	public String getTypeCategory() {
		return typeCategory;
	}

	public String getSpecificType() {
		return specificType;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getFullVersion() {
		return fullVersion;
	}

	public String getServerAdapterTypeId() {
		return serverAdapterTypeId;
	}
}