/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class GenericServerExtensionModel {
	private IServerManagementModel rspModel;
	private HashMap<String, GenericServerTypeExtensionModel> map;
	public GenericServerExtensionModel(IServerManagementModel rspModel, 
			IServerBehaviorFromJSONProvider delegateProvider, InputStream is) {

		this.rspModel = rspModel;
		this.map = new HashMap<>();
		
		JSONMemento memento = createModelMemento(is);
		JSONMemento[] serverTypes = memento.getChild("serverTypes").getChildren();
		for( int i = 0; i < serverTypes.length; i++ ) {
			String id = serverTypes[i].getNodeName();
			GenericServerTypeExtensionModel oneType = 
					loadOneServer(serverTypes[i], delegateProvider);
			this.map.put(id, oneType);
		}
	}


	private JSONMemento createModelMemento(InputStream is) {
		JSONMemento read = JSONMemento.createReadRoot(is);
		return TemplateExtensionModelUtility.generateEffectiveMemento(read);
	}


	private GenericServerTypeExtensionModel loadOneServer(JSONMemento serverMemento, 
			IServerBehaviorFromJSONProvider delegateProvider) {
		return new GenericServerTypeExtensionModel(getRspModel(), delegateProvider, serverMemento);
	}


	public void registerExtensions() {
		ArrayList<GenericServerTypeExtensionModel> sub = new ArrayList<>(map.values());
		for( GenericServerTypeExtensionModel one : sub ) {
			if( one.getMyDownloadRuntimeProvider() != null ) 
				getRspModel().getDownloadRuntimeModel().addDownloadRuntimeProvider(one.getMyDownloadRuntimeProvider());
			if( one.getMyDiscovery() != null )
				getRspModel().getServerBeanTypeManager().addTypeProvider(one.getMyDiscovery());
			if( one.getMyServerType() != null )
				getRspModel().getServerModel().addServerType( one.getMyServerType() );
		}
	}

	public void unregisterExtensions() {
		ArrayList<GenericServerTypeExtensionModel> sub = new ArrayList<>(map.values());
		for( GenericServerTypeExtensionModel one : sub ) {
			if( one.getMyDownloadRuntimeProvider() != null ) 
				getRspModel().getDownloadRuntimeModel().removeDownloadRuntimeProvider(one.getMyDownloadRuntimeProvider());
			if( one.getMyDiscovery() != null )
				getRspModel().getServerBeanTypeManager().removeTypeProvider(one.getMyDiscovery());
			if( one.getMyServerType() != null )
				getRspModel().getServerModel().removeServerType( one.getMyServerType() );
		}
	}

	public IServerManagementModel getRspModel() {
		return rspModel;
	}
}
