/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.redhat.credentials.RedHatAccessCredentials;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class CDKServerType extends BaseMinishiftServerType {

	public CDKServerType(String id, String name, String desc) {
		super(id, name, desc);
	}
	
	private IServerManagementModel getServerManagementModel() {
		if( LauncherSingleton.getDefault() == null )
			return null;
		if( LauncherSingleton.getDefault().getLauncher() == null )
			return null;
		if( LauncherSingleton.getDefault().getLauncher().getModel() == null )
			return null;
		return LauncherSingleton.getDefault().getLauncher().getModel();
	}
	
	private ISecureStorageProvider getSecureStorage() {
		IServerManagementModel model = getServerManagementModel();
		return model == null ? null : model.getSecureStorageProvider();
	}
	private boolean isRedHatUsernameSet() {
		ISecureStorageProvider storage= getSecureStorage();
		if( storage != null ) {
			return RedHatAccessCredentials.getGlobalRedhatUser(storage) != null;
		}
		return false;
	}
	private boolean isRedHatPasswordSet() {
		ISecureStorageProvider storage = getSecureStorage();
		if( storage != null ) {
			return RedHatAccessCredentials.getGlobalRedhatPassword(storage) != null;
		}
		return false;
	}
	
	@Override 
	protected void fillRequiredAttributes(CreateServerAttributesUtility attrs) {
		attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"A filesystem path pointing to a minishift binary file.", null);

		// If there's a global setting for rht username, then this is optional, not required
		if( !isRedHatUsernameSet()) {
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration username.", null);
		}

		// If there's a global setting for rht password, then this is optional, not required
		if( !isRedHatPasswordSet()) {
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration password", null, true);
		}
	}
	
	@Override
	protected void fillOptionalAttributes(CreateServerAttributesUtility attrs) {
		attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_VM_DRIVER, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"The driver to use for the Minishift VM. Possible values: [virtualbox vmwarefusion kvm xhyve hyperv] (default \"kvm\")", null);
		
		attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_PROFILE, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"A minishift profile. Default value is 'minishift'", "minishift");

		attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_HOME, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"A attribute to set the MINISHIFT_HOME environment variable when interacting with the server. The MINISHIFT_HOME environment variable allows you to choose a different home directory for Minishift", null);
		
		// If there's a global setting for rht username, then this is optional, not required
		if( isRedHatUsernameSet()) {
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration username.", null);
		}

		// If there's a global setting for rht password, then this is optional, not required
		if( isRedHatPasswordSet()) {
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration password", null, true);
		}

	}
	
	@Override
	protected boolean isCDK() {
		return true;
	}
	
	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		setGlobalCredentialsIfUnset(server);
		return new CDKServerDelegate(server);
	}
	
	protected void setGlobalCredentialsIfUnset(IServer server) {
		if( !isRedHatUsernameSet() ) {
			String user = server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, (String)null);
			if( user != null && user.isEmpty()) {
				RedHatAccessCredentials.setGlobalRedhatUser(server.getServerManagementModel().getSecureStorageProvider(), user);
			}
		}
		
		if( !isRedHatPasswordSet() ) {
			String pass = server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, (String)null);
			if( pass != null && pass.isEmpty()) {
				RedHatAccessCredentials.setGlobalRedhatUser(server.getServerManagementModel().getSecureStorageProvider(), pass);
			}
		}
	}
}
