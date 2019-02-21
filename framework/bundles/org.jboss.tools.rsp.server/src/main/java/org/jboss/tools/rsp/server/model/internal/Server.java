/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.server.core.internal.SecuredBase;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class Server extends SecuredBase implements IServer {

	public static final String TYPE_ID = "org.jboss.tools.rsp.server.typeId";

	private static final String MEMENTO_DEPLOYABLES = "deployables";
	private static final String MEMENTO_DEPLOYABLE = "deployable";
	private static final String MEMENTO_DEPLOYABLE_LABEL = "label";
	private static final String MEMENTO_DEPLOYABLE_PATH = "path";

	private IServerDelegate delegate;
	private IServerType serverType;
	private IServerManagementModel managementModel;
	
	private List<DeployableReference> deployableInitialization;
	
	public Server(File file, IServerManagementModel managementModel) {
		super(file, managementModel.getSecureStorageProvider());
		this.managementModel = managementModel;
	}
	
	public Server(File file, IServerType type, String id, Map<String, Object> attributes, IServerManagementModel managementModel) {
		super(file, id, managementModel.getSecureStorageProvider());
		this.serverType = type;
		this.managementModel = managementModel;
		setAttributes(attributes);
		if( this.serverType != null ) {
			setAttribute(TYPE_ID, type.getId());
			this.delegate = this.serverType.createServerDelegate(this);
		}
	}

	@Override
	public String getName() {
		return getId();
	}

	@Override
	protected String getXMLRoot() {
		return "server";
	}

	@Override
	protected void saveState(IMemento memento) {
		if( this.delegate != null ) {
			// Do not persist 'state' information for server or deployable;
			// just the existence of that deployable itself. 
			List<DeployableState> deployableState = null;
			IServerPublishModel pubMod = delegate.getServerPublishModel();
			deployableState = pubMod == null ? new ArrayList<>() : pubMod.getDeployableStates();
			if( deployableState == null || deployableState.isEmpty() ) {
				return;
			}
			IMemento deployables = memento.createChild(MEMENTO_DEPLOYABLES);
			Iterator<DeployableState> dsIt = deployableState.iterator();
			while(dsIt.hasNext()) {
				IMemento deployable = deployables.createChild(MEMENTO_DEPLOYABLE);
				DeployableState oneState = dsIt.next();
				deployable.putString(MEMENTO_DEPLOYABLE_LABEL, oneState.getReference().getLabel());
				deployable.putString(MEMENTO_DEPLOYABLE_PATH, oneState.getReference().getPath());
			}
		}
	}
	
	@Override
	protected void loadState(IMemento memento) {
		List<DeployableReference> references = new ArrayList<>();
		IMemento deployables = memento.getChild(MEMENTO_DEPLOYABLES);
		if( deployables != null ) {
			IMemento[] deployableArray = deployables.getChildren(MEMENTO_DEPLOYABLE);
			if( deployableArray != null) {
				for( int i = 0; i < deployableArray.length; i++ ) {
					String path = deployableArray[i].getString(MEMENTO_DEPLOYABLE_PATH);
					String label = deployableArray[i].getString(MEMENTO_DEPLOYABLE_LABEL);
					references.add(new DeployableReference(label, path));
				}
			}
		}
		deployableInitialization = references;
	}
	
	@Override
	public String getTypeId() {
		return getAttribute(TYPE_ID, (String)null);
	}

	@Override
	public IServerType getServerType() {
		return serverType;
	}

	public void setServerType(IServerType type) {
		this.serverType = type;
		if( type != null ) 
			setAttribute(TYPE_ID, type.getId());
	}

	public void setDelegate(IServerDelegate del) {
		delegate = del;
		if( delegate != null && delegate.getServerPublishModel() != null )
			delegate.getServerPublishModel().initialize(deployableInitialization);
	}

	@Override
	public IServerDelegate getDelegate() {
		return delegate;
	}

	@Override
	public IServerManagementModel getServerManagementModel() {
		return managementModel;
	}

	@Override
	public void save(IProgressMonitor monitor) throws CoreException {
		super.saveToFile(monitor);
	}

	@Override
	public void load(IProgressMonitor monitor) throws CoreException {
		super.loadFromFile(monitor);
	}

	private void setAttributes(Map<String, Object> attributes) {
		Set<String> keys = attributes.keySet();
		for( String k : keys) {
			setAttribute(k, attributes.get(k));
		}
	}

	protected void setAttribute(String k, Object val) {
		if( val instanceof Integer) {
			setAttribute(k, ((Integer)val).intValue());
		} else if( val instanceof Boolean) {
			setAttribute(k, ((Boolean)val).booleanValue());
		} else if( val instanceof String ) {
			setAttribute(k, (String)val);
		} else if( val instanceof List) {
			setAttribute(k, (List<?>)val);
		} else if( val instanceof Map) {
			setAttribute(k, (Map<?,?>)val);
		}
	}

}
