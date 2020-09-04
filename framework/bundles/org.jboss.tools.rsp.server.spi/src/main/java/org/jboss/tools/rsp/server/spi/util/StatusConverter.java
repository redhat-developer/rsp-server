/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;

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
		if (status == null) {
			return null;
		}
		StringBuffer fullMessage = new StringBuffer();

		int sev = status.getSeverity();
		String plugin = status.getPlugin();
		String msg = status.getMessage();
		Throwable t = status.getException();
		String trace = getTrace(t);
		fullMessage.append(trace);
		
		if( status instanceof MultiStatus ) {
			IStatus[] kids = ((MultiStatus)status).getChildren();
			if( kids != null ) {
				for( int i = 0; i < kids.length; i++ ) {
					String msg2 = status.getMessage();
					Throwable t2 = status.getException();
					String trace2 = getTrace(t2);
					fullMessage.append("\n\n");
					fullMessage.append(msg2);
					fullMessage.append("\n");
					fullMessage.append(trace2);
				}
			}
		}
		
		org.jboss.tools.rsp.api.dao.Status ret = 
				new org.jboss.tools.rsp.api.dao.Status(sev, plugin, msg, fullMessage.toString());
		return ret;
	}
	
	private static String getTrace(Throwable t) {
		String trace = null;
		if( t != null ) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			trace = sw.toString(); // stack trace as a string
		}
		return trace;

	}
}
