/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Objects;
import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

public class PublishServerRequest {
	private ServerHandle server;
	private int kind;

	public PublishServerRequest() {

	}

	public PublishServerRequest(ServerHandle server, int type) {
		this.server = server;
		this.kind = type;
	}

	public ServerHandle getServer() {
		return server;
	}

	public void setServer(ServerHandle server) {
		this.server = server;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}
	

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof PublishServerRequest)) {
			return false;
		}
		PublishServerRequest temp = (PublishServerRequest) o;
		return EqualsUtility.areEqual(this.server, temp.server) && EqualsUtility.areEqual(this.kind, temp.kind);
	}

	@Override
	public int hashCode() {
		return Objects.hash(server, kind);
	}
}
