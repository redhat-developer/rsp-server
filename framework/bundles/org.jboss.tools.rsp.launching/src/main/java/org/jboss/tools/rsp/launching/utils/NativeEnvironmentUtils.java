/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NativeEnvironmentUtils {
	private static final NativeEnvironmentUtils instance = new NativeEnvironmentUtils();
	public static final NativeEnvironmentUtils getDefault() {
		return instance;
	}

	private NativeEnvironmentUtils() {
	}

	/**
	 * The collection of native environment variables on the user's system. Cached
	 * after being computed once as the environment cannot change.
	 */
	private HashMap<String, String> fNativeEnv = null;
	private HashMap<String, String> fNativeEnvCasePreserved = null;

	public synchronized Map<String, String> getNativeEnvironment() {
		if (fNativeEnv == null) {
			Map<String, String> casePreserved = getNativeEnvironmentCasePreserved();
			if (OSUtils.isWindows()) {
				fNativeEnv = new HashMap<>();
				for (Entry<String, String> entry : casePreserved.entrySet()) {
					fNativeEnv.put(entry.getKey().toUpperCase(), entry.getValue());
				}
			} else {
				fNativeEnv = new HashMap<>(casePreserved);
			}
		}
		return new HashMap<>(fNativeEnv);
	}

	public synchronized Map<String, String> getNativeEnvironmentCasePreserved() {
		if (fNativeEnvCasePreserved == null) {
			fNativeEnvCasePreserved = new HashMap<>(System.getenv());
		}
		return new HashMap<>(fNativeEnvCasePreserved);
	}

	public String[] getEnvironment(Map<String, String> configEnv, boolean appendNativeEnv) {
		Map<String, String> env = new HashMap<>();
		if (appendNativeEnv) {
			env.putAll(NativeEnvironmentUtils.getDefault().getNativeEnvironmentCasePreserved());
		}
		if (configEnv != null) {
			env.putAll(configEnv);
		}

		List<String> strings = new ArrayList<>(env.size());
		for (Entry<String, String> entry : env.entrySet()) {
			strings.add(entry.getKey() + '=' + entry.getValue());
		}
		return strings.toArray(new String[strings.size()]);
	}
}
