/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class Attribute {
	private String type;
	private String description;
	private Object defaultVal;
	
	public Attribute() {
		
	}
	
	public Attribute(String type, String desc, Object def) {
		this.type = type;
		this.description = desc;
		this.defaultVal = def;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(Object defaultVal) {
		this.defaultVal = defaultVal;
	}

}
