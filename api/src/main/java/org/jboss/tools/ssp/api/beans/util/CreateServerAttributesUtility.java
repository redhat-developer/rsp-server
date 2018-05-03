/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api.beans.util;

import java.util.HashMap;
import java.util.Set;

import org.jboss.tools.ssp.api.beans.CreateServerAttribute;
import org.jboss.tools.ssp.api.beans.CreateServerAttributes;

public class CreateServerAttributesUtility {
	private HashMap<String, CreateServerAttribute> attrs;
	
	public CreateServerAttributesUtility() {
		attrs = new HashMap<>();
	}
	
	public CreateServerAttributesUtility(CreateServerAttributes original) {
		attrs = new HashMap<>(original.getAttributes());
	}

	public Set<String> listAttributes() {
		return attrs.keySet();
	}
	
	public String getAttributeType(String key) {
		CreateServerAttribute a = attrs.get(key);
		if( a != null )
			return a.getType();
		return null;
	}
	
	public String getAttributeDescription(String key) {
		CreateServerAttribute a = attrs.get(key);
		if( a != null )
			return a.getDescription();
		return null;
	}
	
	public Object getAttributeDefaultValue(String key) {
		CreateServerAttribute a = attrs.get(key);
		if( a != null )
			return a.getDefaultVal();
		return null;
	}
	
	public void addAttribute(String key, String type, String d, Object defaultVal) {
		CreateServerAttribute a = new CreateServerAttribute(type, d, defaultVal);
		attrs.put(key,  a);
	}

	public CreateServerAttributes toPojo() {
		return new CreateServerAttributes(attrs);
	}
}
