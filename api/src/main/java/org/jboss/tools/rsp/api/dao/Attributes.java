/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.HashMap;
import java.util.Map;

public class Attributes {
	private Map<String, Attribute> attributes;
	
	public Attributes() {
		attributes = new HashMap<>();
	}
	public Attributes(Map<String, Attribute> a) {
		attributes = new HashMap<>(a);
	}

	public Map<String, Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Attribute> attrs) {
		this.attributes = attrs;
	}
}
