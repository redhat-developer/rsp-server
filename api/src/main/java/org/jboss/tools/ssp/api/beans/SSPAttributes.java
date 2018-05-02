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
import java.util.Set;

public class SSPAttributes {
	private HashMap<String, String> types;
	private HashMap<String, String> descriptions;
	private HashMap<String, Object> defaultVals;
	
	public SSPAttributes() {
		types = new HashMap<>();
		descriptions = new HashMap<>();
		defaultVals = new HashMap<>();
	}
	
	public Set<String> listAttributes() {
		return types.keySet();
	}
	
	public String getAttributeType(String key) {
		return types.get(key);
	}
	
	public String getAttributeDescription(String key) {
		return descriptions.get(key);
	}
	
	public Object getAttributeDefaultValue(String key) {
		return defaultVals.get(key);
	}
	
	public void addAttribute(String key, String type, String d, Object defaultVal) {
		types.put(key,  type);
		if( d != null ) {
			descriptions.put(key,  d);
		}
		if( defaultVal != null ) {
			defaultVals.put(key, defaultVal);
		}
	}
}
