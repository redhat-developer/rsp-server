/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusConverter {
	public static org.jboss.tools.rsp.eclipse.core.runtime.IStatus convert(
			org.jboss.tools.rsp.api.dao.Status status) {
		int sev = status.getSeverity();
		String plugin = status.getPlugin();
		String msg = status.getMessage();
		return new org.jboss.tools.rsp.eclipse.core.runtime.Status(sev, plugin, msg);
	}
	
	public static org.jboss.tools.rsp.api.dao.Status convert(
			org.jboss.tools.rsp.eclipse.core.runtime.IStatus status) {
		int sev = status.getSeverity();
		String plugin = status.getPlugin();
		String msg = status.getMessage();
		Throwable t = status.getException();
		String trace = null;
		if( t != null ) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			trace = sw.toString(); // stack trace as a string
		}
		org.jboss.tools.rsp.api.dao.Status ret = 
				new org.jboss.tools.rsp.api.dao.Status(sev, plugin, msg, trace);
		return ret;
	}
}
