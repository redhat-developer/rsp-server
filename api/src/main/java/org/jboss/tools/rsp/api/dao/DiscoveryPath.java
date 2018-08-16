/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class DiscoveryPath {
	private String filepath;
	public DiscoveryPath() {
		
	}
	public DiscoveryPath(String filepath) {
		super();
		this.setFilepath(filepath);
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	@Override
	public int hashCode() {
		return filepath.hashCode();
	}
	
	public boolean equals(Object other) {
		if( other instanceof DiscoveryPath) {
			String fp = ((DiscoveryPath)other).getFilepath();
			if( this.getFilepath() == null ) 
				return fp == null;
			return this.getFilepath().equals(fp);
		}
		return false;
	}
	
}
