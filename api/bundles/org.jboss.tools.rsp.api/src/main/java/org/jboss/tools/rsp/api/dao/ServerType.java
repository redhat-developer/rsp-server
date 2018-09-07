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

public class ServerType {
	private String id;
	private String visibleName;
	private String description;

	public ServerType() {

	}

	public ServerType(String id, String name, String description) {
		this.id = id;
		this.visibleName = name;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVisibleName() {
		return visibleName;
	}

	public void setVisibleName(String visibleName) {
		this.visibleName = visibleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ServerType)) {
                return false;
            }
            ServerType temp = (ServerType) o;
            return EqualsUtility.areEqual(this.id, temp.id) 
                    && EqualsUtility.areEqual(this.description, temp.description) 
                    && EqualsUtility.areEqual(this.visibleName, temp.visibleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, description, visibleName);
        } 
}
