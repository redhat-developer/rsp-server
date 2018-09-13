/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java;

import java.util.HashMap;
import java.util.Map;

public class RunningVMSyspropCache {

	private static RunningVMSyspropCache cache = new RunningVMSyspropCache();

	public static RunningVMSyspropCache getDefault() {
		return cache;
	}

	private HashMap<String, String> cachedValues = new HashMap<>();

	private RunningVMSyspropCache() {
	}

	/**
	 * Removes all cached values.
	 */
	public void clear() {
		cachedValues.clear();
	}
	
	/**
	 * Returns all cached values. Returns {@code null} if the cache is empty. 
	 * 
	 * @return
	 */
	public Map<String, String> getCachedValues() {
		if (cachedValues == null
				|| cachedValues.isEmpty()) {
			return null;
		}
		return new HashMap<>(cachedValues);
	}

	/**
	 * Returns cached values for the given keys. If the cache differs in size or
	 * keys {@code null} is returned.
	 * 
	 * @param keys
	 * @return
	 */
	public Map<String, String> getCachedValues(String[] keys) {
		if (cachedValues.size() < keys.length)
			return null;

		Map<String, String> ret = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String v = cachedValues.get(keys[i]);
			// key is not cached
			if (v == null) {
				return null;
			}
			ret.put(keys[i], v);
		}
		return ret;
	}

	/**
	 * Sets the values that are cached in this cache.
	 * 
	 * @param vals
	 */
	public void setCachedValues(Map<String, String> vals) {
		if (vals == null) {
			this.cachedValues = new HashMap<>();
		} else {
			this.cachedValues = new HashMap<>(vals);
		}
	}

}
