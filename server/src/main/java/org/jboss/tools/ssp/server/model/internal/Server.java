package org.jboss.tools.ssp.server.model.internal;

import java.io.File;

import org.jboss.tools.ssp.server.core.internal.Base;
import org.jboss.tools.ssp.server.core.internal.IMemento;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class Server extends Base implements IServer {
	private static final String TYPE_ID = "org.jboss.tools.ssp.server.typeId";
	private IServerDelegate delegate;
	public Server(File file, String typeId) {
		super(file);
		setAttribute(TYPE_ID, typeId);
	}

	@Override
	protected String getXMLRoot() {
		return "server";
	}

	@Override
	protected void saveState(IMemento memento) {
		// Intentionally empty, may be removed
	}

	@Override
	protected void loadState(IMemento memento) {
		// Intentionally empty, may be removed
	}

	@Override
	public String getTypeId() {
		return getAttribute(TYPE_ID, (String)null);
	}

	public void setDelegate(IServerDelegate del) {
		delegate = del;
	}

	@Override
	public IServerDelegate getDelegate() {
		return delegate;
	}

}
