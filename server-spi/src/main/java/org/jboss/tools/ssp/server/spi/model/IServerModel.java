package org.jboss.tools.ssp.server.spi.model;

import java.util.Map;

import org.jboss.tools.ssp.api.dao.Attributes;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;

public interface IServerModel {

	String[] getServerTypes();

	IServer getServer(String id);

	ServerHandle[] getServerHandles();

	Attributes getRequiredAttributes(String id);

	Attributes getOptionalAttributes(String id);

	Attributes getRequiredLaunchAttributes(String id);

	Attributes getOptionalLaunchAttributes(String id);

	IStatus createServer(String serverType, String id, Map<String, Object> attributes);

	void removeServer(String id);

	void fireServerStateChanged(IServer server, int state);

	void fireServerProcessTerminated(IServer server, String processId);

	void fireServerProcessCreated(IServer server, String processId);

	void fireServerStreamAppended(IServer server2, String processId, int streamType, String text);

	void addServerModelListener(IServerModelListener listener);

	void addServerType(IServerType serverType);
	void removeServerType(IServerType serverType);
	

}
