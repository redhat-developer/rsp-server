package org.jboss.tools.rsp.server.spi.model;

import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;

public class ServerModelListenerAdapter implements IServerModelListener {

	@Override
	public void serverAdded(ServerHandle server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverRemoved(ServerHandle server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverAttributesChanged(ServerHandle server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverStateChanged(ServerHandle server, ServerState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverProcessCreated(ServerHandle server, String processId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverProcessTerminated(ServerHandle server, String processId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text) {
		// TODO Auto-generated method stub
		
	}

}
