package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class CRCServerType extends BaseMinishiftServerType{

	public CRCServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		return new CRCServerDelegate(server);
	}
}
