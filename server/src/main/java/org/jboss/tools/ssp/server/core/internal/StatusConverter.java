package org.jboss.tools.ssp.server.core.internal;

public class StatusConverter {
	public static org.jboss.tools.ssp.api.beans.Status convert(
			org.eclipse.core.runtime.IStatus status) {
		int sev = status.getSeverity();
		String plugin = status.getPlugin();
		String msg = status.getMessage();
		Throwable t = status.getException();
		org.jboss.tools.ssp.api.beans.Status ret = 
				new org.jboss.tools.ssp.api.beans.Status(sev, plugin, msg, t);
		return ret;
	}
}
