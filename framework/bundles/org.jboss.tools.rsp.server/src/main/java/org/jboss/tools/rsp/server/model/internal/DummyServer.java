package org.jboss.tools.rsp.server.model.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.server.ServerCoreActivator;

public class DummyServer extends Server {

	public static DummyServer createDummyServer(String json) throws CoreException {
		DummyServer ds = new DummyServer();
		ds.loadFromJson(json);
		return ds;
	}
	
	public DummyServer() {
		super(null, null, null);
	}
	public void loadFromJson(String json) throws CoreException {
		try(InputStream in = new ByteArrayInputStream(json.getBytes())) {
			IMemento memento = loadMemento(in); 
			if( memento == null ) {
				throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 0, 
						NLS.bind("Could not load server from provided string: {0}", json), null));
			}
			load(memento);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 0, 
					NLS.bind("Could not load server from provided string: {0}", json), e));
		}
	}
	
	public Map<String, Object> getMap() {
		return map;
	}
}
