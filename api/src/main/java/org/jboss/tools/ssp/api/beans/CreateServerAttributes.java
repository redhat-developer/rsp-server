/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api.beans;

import java.util.HashMap;
import java.util.Map;

public class CreateServerAttributes {
	private HashMap<String, CreateServerAttribute> attributes;
	
	public CreateServerAttributes() {
		attributes = new HashMap<>();
	}
	public CreateServerAttributes(Map<String, CreateServerAttribute> a) {
		attributes = new HashMap<>(a);
	}

	public HashMap<String, CreateServerAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, CreateServerAttribute> attrs) {
		this.attributes = attrs;
	}
}
