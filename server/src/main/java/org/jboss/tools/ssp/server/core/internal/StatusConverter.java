/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.core.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StatusConverter {
	public static org.jboss.tools.ssp.api.beans.Status convert(
			org.jboss.tools.ssp.eclipse.core.runtime.IStatus status) {
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
		org.jboss.tools.ssp.api.beans.Status ret = 
				new org.jboss.tools.ssp.api.beans.Status(sev, plugin, msg, trace);
		return ret;
	}
}
