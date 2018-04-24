package org.jboss.tools.ssp.server.spi.servertype;

import org.jboss.tools.ssp.api.beans.SSPAttributes;

public interface IServerType {
	public String getServerTypeId();
	public IServerDelegate createServerDelegate(IServer server);
	public SSPAttributes getRequiredAttributes();
	public SSPAttributes getOptionalAttributes();
}
