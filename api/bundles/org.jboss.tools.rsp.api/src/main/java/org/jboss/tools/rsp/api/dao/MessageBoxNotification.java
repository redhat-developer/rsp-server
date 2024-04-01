/*******************************************************************************
 * Copyright (c) 2018, 2024 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class MessageBoxNotification {
	public static final int SYSTEM_UNKNOWN = 0x1000;
	public static final int SYSTEM_STARTUP = 0x2000;
	public static final int SYSTEM_SHUTDOWN = 0x4000;
	public static final int SYSTEM_PUBLISH = 0x8000;
	public static final int SYSTEM_ACTIONS = 0x10000;
	

	
	private int code;
	private int severity;
	private String message;
	private Map<String, Object> properties;
	
	public MessageBoxNotification(String message) {
		this(message, Status.INFO, 0);
	}
	
	public MessageBoxNotification(String message, int code) {
		this(message, Status.INFO, code);
		this.message = message;
		this.code = code;
	}
	public MessageBoxNotification(String message, int severity, int code) {
		this.message = message;
		this.severity = severity;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

}
