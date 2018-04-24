package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import org.jboss.tools.ssp.api.beans.SSPAttributes;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;
import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;

public class JBossServerTypeFactory implements IServerType{
	private SSPAttributes required = null;
	private SSPAttributes optional = null;
	
	@Override
	public String getServerTypeId() {
		return IServerConstants.SERVER_WILDFLY_120;
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		// TODO Auto-generated method stub
		return new IServerDelegate() {};
	}

	@Override
	public SSPAttributes getRequiredAttributes() {
		if( required == null ) {
			SSPAttributes attrs = new SSPAttributes();
			attrs.addAttribute("TEST1", Integer.class, "a test", new Integer(5));
			attrs.addAttribute("TEST2", String.class, "a test 2", null);
			// TODO add some
			required = attrs;
		}
		return required;
	}

	@Override
	public SSPAttributes getOptionalAttributes() {
		if( optional == null ) {
			SSPAttributes attrs = new SSPAttributes();
			// TODO add some
			optional = attrs;
		}
		return optional;
	}

}
