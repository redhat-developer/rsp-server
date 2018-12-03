/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

public class Attributes {
	private Map<String, Attribute> attributes;

	public Attributes() {
		this.attributes = new LinkedHashMap<>();
	}

	public Attributes(Map<String, Attribute> a) {
		this.attributes = new LinkedHashMap<>(a);
	}

	public Map<String, Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Attribute> attrs) {
		this.attributes = new LinkedHashMap<>(attrs);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof Attributes)) {
			return false;
		}
		return EqualsUtility.areEqual(this.attributes, ((Attributes) o).attributes);
	}

	@Override
	public int hashCode() {
		return attributes.hashCode();
	}
}
