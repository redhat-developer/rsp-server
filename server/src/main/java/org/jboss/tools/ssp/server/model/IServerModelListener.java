package org.jboss.tools.ssp.server.model;

import org.jboss.tools.ssp.api.beans.ServerHandle;

public interface IServerModelListener {
	void serverAdded(ServerHandle server);
	
	void serverRemoved(ServerHandle server);
	
	void serverAttributesChanged(ServerHandle server);
	
	void serverStateChanged(ServerHandle server, int state);
	
	void serverProcessCreated(ServerHandle server, String processId);
	
	void serverProcessTerminated(ServerHandle server, String processId);
	
	void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text);

}
