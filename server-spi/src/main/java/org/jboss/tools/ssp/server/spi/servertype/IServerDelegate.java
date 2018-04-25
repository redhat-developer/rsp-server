package org.jboss.tools.ssp.server.spi.servertype;

import org.eclipse.core.runtime.IStatus;

public interface IServerDelegate {
	public IStatus validate();
}
