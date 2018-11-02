/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

public class DeployableReference {
	private String id;
	private String path;
	
	public DeployableReference() {
		this(null,null);
	}

	public DeployableReference(String id, String path) {
		this.id = id;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public boolean equals(Object other2) {
		if( !(other2 instanceof DeployableReference)) 
			return false;
		DeployableReference other = (DeployableReference)other2;
		return EqualsUtility.areEqual(getPath(), other.getPath()) && 
				EqualsUtility.areEqual(getId(), other.getId());
	}

	@Override
	public int hashCode() {
		return (getId() + "::" + getPath()).hashCode();
	}
}
