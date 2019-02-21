/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Objects;
import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

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

	public void setId(String id) {
		this.id = id;
	}

	public void setType(ServerType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ServerHandle)) {
			return false;
		}
		ServerHandle temp = (ServerHandle) o;
		return EqualsUtility.areEqual(this.id, temp.id) && EqualsUtility.areEqual(this.type, temp.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}
}
