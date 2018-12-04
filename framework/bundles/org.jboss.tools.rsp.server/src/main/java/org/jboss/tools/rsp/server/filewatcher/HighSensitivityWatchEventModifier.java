/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.filewatcher;

import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchService;

import org.jboss.tools.rsp.launching.utils.OSUtils;

/**
 * A class that allows testing and grabbing SensitivityWatchEventModifier#HIGH
 * reflectively. This allows the code to run in jdks where it does not exists
 * (ex. IBM, etc.). SensitivityWatchEventModifier#HIGH is required for the
 * {@link WatchService} to work ~reliably on MacOS where no native hooks but
 * polling is used.
 * 
 * @see https://bugs.openjdk.java.net/browse/JDK-7133447
 * @see https://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else
 * 
 * @author Andre Dietisheim
 * 
 * @see SenisitivityWatchEventModifier#HIGH
 */
public class HighSensitivityWatchEventModifier {

	private static final String HIGH_SENSITIVITY = "HIGH";
	private static final String QUALIFIED_CLASSNAME = "com.sun.nio.file.SensitivityWatchEventModifier";

	public boolean isRequired() {
		return isMac();
	}

	/** for testing purposes **/
	protected boolean isMac() {
		return OSUtils.isMac();
	}

	public boolean exists() {
		return get() != null;
	}

	/**
	 * Returns the SensitivityWatchEventModifier#HIGH if it exists (on sun jvms or
	 * openjdk), {@code null} otherwise.
	 * 
	 * @return
	 */
	public WatchEvent.Modifier get() {
		try {
			Class<Modifier> sensitivityClass = getSensitivityWatchEventModifierClass();
			return getEnumConstant(HIGH_SENSITIVITY, sensitivityClass.getEnumConstants());
		} catch (ClassNotFoundException | SecurityException e) {
			return null;
		}
	}

	/** for testing purposes **/
	protected Class<Modifier> getSensitivityWatchEventModifierClass() throws ClassNotFoundException {
		return (Class<Modifier>) Class.forName(QUALIFIED_CLASSNAME);
	}

	private Modifier getEnumConstant(String name, Modifier[] modifiers) {
		for (Modifier modifier : modifiers) {
			if (name.equals(modifier.name())) {
				return modifier;
			}
		}
		return null;
	}

}
