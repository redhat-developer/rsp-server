/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;

public class CreateServerAttributesUtility {
	private Map<String, Attribute> attrs;
	
	public CreateServerAttributesUtility() {
		this.attrs = new LinkedHashMap<>();
	}
	
	public CreateServerAttributesUtility(Attributes original) {
		this.attrs = new LinkedHashMap<>(original.getAttributes());
	}

	public Set<String> listAttributes() {
		return attrs.keySet();
	}
	
	public String getAttributeType(String key) {
		Attribute a = attrs.get(key);
		if( a != null )
			return a.getType();
		return null;
	}
	
	public String getAttributeDescription(String key) {
		Attribute a = attrs.get(key);
		if( a != null )
			return a.getDescription();
		return null;
	}
	
	public boolean isAttributeSecret(String key) {
		Attribute a = attrs.get(key);
		if( a != null )
			return a.isSecret();
		return false;
	}
	
	public Object getAttributeDefaultValue(String key) {
		Attribute a = attrs.get(key);
		if( a != null )
			return a.getDefaultVal();
		return null;
	}
	
	public void addAttribute(String key, String type, String d, Object defaultVal) {
		Attribute a = new Attribute(type, d, defaultVal);
		attrs.put(key,  a);
	}

	public void addAttribute(String key, String type, String d, Object defaultVal, boolean secret) {
		Attribute a = new Attribute(type, d, defaultVal, secret);
		attrs.put(key,  a);
	}

	public void removeAttribute(String key) {
		attrs.remove(key);
	}

	public Attributes toPojo() {
		return new Attributes(attrs);
	}
}
