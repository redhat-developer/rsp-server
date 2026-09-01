/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerDescriptorUtil {

	public static final String VARIABLE_PREFIX = "${rsp_import_export/";
	public static final String VARIABLE_SUFFIX = "}";
	public static final String FORMAT_VERSION_KEY = "rsp.descriptor.format.version";
	public static final String FORMAT_VERSION = "1.0";
	public static final String TYPE_ID_KEY = "org.jboss.tools.rsp.server.typeId";
	private static final Set<String> EXCLUDED_EXPORT_KEYS = new HashSet<>(
			Arrays.asList("id", "deployables"));

	private static final Pattern VARIABLE_PATTERN = Pattern.compile(
			"\\$\\{rsp_import_export/([^}]+)\\}");

	private ServerDescriptorUtil() {
	}

	public static boolean isFilesystemPath(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		String trimmed = value.trim();

		if (trimmed.startsWith("file://")) {
			return true;
		}

		if (trimmed.startsWith("/")) {
			String[] segments = trimmed.split("/");
			long nonEmpty = Arrays.stream(segments).filter(s -> !s.isEmpty()).count();
			return nonEmpty >= 2;
		}

		if (trimmed.length() >= 3
				&& Character.isLetter(trimmed.charAt(0))
				&& trimmed.charAt(1) == ':'
				&& (trimmed.charAt(2) == '\\' || trimmed.charAt(2) == '/')) {
			return true;
		}

		return false;
	}

	public static Map<String, String> detectPathAttributes(Map<String, Object> attrs) {
		Map<String, String> pathAttrs = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : attrs.entrySet()) {
			String key = entry.getKey();
			if (EXCLUDED_EXPORT_KEYS.contains(key)
					|| FORMAT_VERSION_KEY.equals(key)
					|| TYPE_ID_KEY.equals(key)) {
				continue;
			}
			Object val = entry.getValue();
			if (val instanceof String && isFilesystemPath((String) val)) {
				pathAttrs.put(key, (String) val);
			}
		}
		return pathAttrs;
	}

	public static Map<String, Object> substitutePathsForExport(Map<String, Object> attrs) {
		Map<String, String> pathAttrs = detectPathAttributes(attrs);

		List<Map.Entry<String, String>> sortedPaths = new ArrayList<>(pathAttrs.entrySet());
		sortedPaths.sort((a, b) -> b.getValue().length() - a.getValue().length());

		Map<String, Object> result = new LinkedHashMap<>();
		result.put(FORMAT_VERSION_KEY, FORMAT_VERSION);

		for (Map.Entry<String, Object> entry : attrs.entrySet()) {
			if (EXCLUDED_EXPORT_KEYS.contains(entry.getKey())) {
				continue;
			}

			Object val = entry.getValue();
			if (!(val instanceof String)) {
				result.put(entry.getKey(), val);
				continue;
			}

			String newValue = (String) val;
			for (Map.Entry<String, String> pathEntry : sortedPaths) {
				String variable = VARIABLE_PREFIX + pathEntry.getKey() + VARIABLE_SUFFIX;
				while (newValue.contains(pathEntry.getValue())) {
					newValue = newValue.replace(pathEntry.getValue(), variable);
				}
			}
			result.put(entry.getKey(), newValue);
		}

		return result;
	}

	public static List<String> findVariables(Map<String, Object> attrs) {
		Set<String> variables = new LinkedHashSet<>();
		for (Object val : attrs.values()) {
			if (!(val instanceof String)) {
				continue;
			}
			Matcher matcher = VARIABLE_PATTERN.matcher((String) val);
			while (matcher.find()) {
				variables.add(matcher.group(1));
			}
		}
		return new ArrayList<>(variables);
	}

	public static Map<String, Object> resolveVariables(
			Map<String, Object> attrs, Map<String, String> values) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : attrs.entrySet()) {
			if (FORMAT_VERSION_KEY.equals(entry.getKey())) {
				continue;
			}

			Object val = entry.getValue();
			if (!(val instanceof String)) {
				result.put(entry.getKey(), val);
				continue;
			}

			String newValue = (String) val;
			for (Map.Entry<String, String> varEntry : values.entrySet()) {
				String variable = VARIABLE_PREFIX + varEntry.getKey() + VARIABLE_SUFFIX;
				while (newValue.contains(variable)) {
					newValue = newValue.replace(variable, varEntry.getValue());
				}
			}
			result.put(entry.getKey(), newValue);
		}
		return result;
	}

}
