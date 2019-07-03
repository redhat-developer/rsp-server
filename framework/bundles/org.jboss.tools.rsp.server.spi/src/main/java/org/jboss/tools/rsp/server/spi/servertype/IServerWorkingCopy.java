package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

public interface IServerWorkingCopy {
	public void setAttribute(String attributeName, int value);

	public void setAttribute(String attributeName, boolean value);

	public void setAttribute(String attributeName, String value);

	public void setAttribute(String attributeName, List<String> value);

	public void setAttribute(String attributeName, Map<?,?> value);
	
	void save(IProgressMonitor monitor) throws CoreException;
}
