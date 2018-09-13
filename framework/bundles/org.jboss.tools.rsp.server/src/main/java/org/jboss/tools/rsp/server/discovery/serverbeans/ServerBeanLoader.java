/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.discovery.serverbeans;

import java.io.File;

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;


/**
 * @author eskimo
 *
 */
public class ServerBeanLoader {
	private ServerBean bean = null;
	private ServerBeanType type = null;
	private File rootLocation = null;

	public ServerBeanLoader(File location) {
		rootLocation = location;
	}
	
	public ServerBean getServerBean() {
		if( bean == null )
			loadBeanInternal();
		return bean;
	}

	public ServerBeanType getServerBeanType() {
		if( bean == null )
			loadBeanInternal();
		return type;
	}

	
	private void loadBeanInternal() {
		this.type = loadTypeInternal(rootLocation);
		this.bean = type.createServerBean(rootLocation);
	}
	
	private ServerBeanType loadTypeInternal(File location) {
		ServerBeanType[] all = getServerBeanTypeManager().getAllRegisteredTypes();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].isServerRoot(location))
				return all[i];
		}
		return new ServerBeanTypeUnknown();
	}
	
	protected IServerBeanTypeManager getServerBeanTypeManager() {
		return ServerManagementModel.getDefault().getServerBeanTypeManager();
	}

	public String getFullServerVersion() {
		if( bean == null )
			loadBeanInternal();
		return bean.getFullVersion();
	}
	
	/**
	 * Get a string representation of this bean's 
	 * server type. This will usually be equivalent to 
	 * getServerType().getId(),  but may be overridden 
	 * in some cases that require additional differentiation. 
	 * 
	 * @return an org.eclipse.wst.server.core.IServerType's type id
	 * @since 3.0 (actually 2.4.101)
	 */
	public String getUnderlyingTypeId() {
		if( bean == null )
			loadBeanInternal();
		return bean.getSpecificType();
	}
	
	
	/**
	 * Get a server type id corresponding to an org.eclipse.wst.server.core.IServerType
	 * that matches with this server bean's root location. 
	 * 
	 * @return an org.eclipse.wst.server.core.IServerType's type id
	 */
	public String getServerAdapterId() {
		if( bean == null )
			loadBeanInternal();
		return bean.getServerAdapterTypeId();
	}
	
}
