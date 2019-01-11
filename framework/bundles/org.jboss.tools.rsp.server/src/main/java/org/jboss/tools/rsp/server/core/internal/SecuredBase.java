/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.core.internal;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.spi.model.IServerModel;

import com.google.gson.Gson;
/**
 * Helper class for storing runtime and server attributes.
 */
public abstract class SecuredBase extends Base {
	private ISecureStorageProvider secureStorage = null;
	
	/**
	 * Create a new object.
	 * 
	 * @param file
	 */
	public SecuredBase(File file, ISecureStorageProvider storage) {
		this(file, null, storage);
	}
	
	/**
	 * Create a new object.
	 * 
	 * @param file
	 * @param id
	 * @param secure storage
	 */
	public SecuredBase(File file, String id, ISecureStorageProvider provider) {
		super(file, id); 
		secureStorage = provider;
	}
	
	private boolean isSecureKey(String key) {
		return key != null && key.startsWith(IServerModel.SECURE_ATTRIBUTE_PREFIX);
	}

	/**
	 * Returns <code>true</code> if the attribute is currently set, and <code>false</code>
	 * otherwise.
	 * 
	 * @param attributeName
	 * @return <code>true</code> if the attribute is currently set, and <code>false</code>
	 *    otherwise
	 */
	@Override
	public boolean isAttributeSet(String attributeName) {
		if( !isSecureKey(attributeName))
			return super.isAttributeSet(attributeName);
		if( canAccessSecureStorage()) {
			String securedNode = getSecuredKey();
			try {
				return secureStorage.getSecureStorage().propertyExists(securedNode, attributeName);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return false;
	}

	@Override
	public String getAttribute(String attributeName, String defaultValue) {
		if( !isSecureKey(attributeName))
			return super.getAttribute(attributeName, defaultValue);
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				return secureStorage.getSecureStorage().getNode(securedNode)
					.getStringProperty(attributeName, defaultValue);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return defaultValue;
	}

	@Override
	public int getAttribute(String attributeName, int defaultValue) {
		if( !isSecureKey(attributeName))
			return super.getAttribute(attributeName, defaultValue);
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				return secureStorage.getSecureStorage().getNode(securedNode)
					.getIntegerProperty(attributeName, defaultValue);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return defaultValue;
	}

	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		if( !isSecureKey(attributeName))
			return super.getAttribute(attributeName, defaultValue);
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				return secureStorage.getSecureStorage().getNode(securedNode)
					.getBooleanProperty(attributeName, defaultValue);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAttribute(String attributeName, List<String> defaultValue) {
		if( !isSecureKey(attributeName))
			return super.getAttribute(attributeName, defaultValue);
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				String fromSecure = secureStorage.getSecureStorage().getNode(securedNode)
					.getStringProperty(attributeName, (String)null);
				if( fromSecure != null ) {
					List<String> ret = new Gson().fromJson(fromSecure, List.class);
					return ret;
				}
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return defaultValue;
	}

	@Override
	public Map getAttribute(String attributeName, Map defaultValue) {
		if( !isSecureKey(attributeName))
			return super.getAttribute(attributeName, defaultValue);
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				String fromSecure = secureStorage.getSecureStorage().getNode(securedNode)
					.getStringProperty(attributeName, (String)null);
				if( fromSecure != null ) {
					Map<String,String> ret = new Gson().fromJson(fromSecure, Map.class);
					return ret;
				}
			} catch(CryptoException ce) {
				// TODO log
			}
		}
		return defaultValue;
	}

	@Override
	public void setAttribute(String attributeName, int value) {
		if( !isSecureKey(attributeName)) {
			super.setAttribute(attributeName, value);
			return;
		}
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				secureStorage.getSecureStorage().getNode(securedNode)
					.setIntegerProperty(attributeName, value);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
	}

	@Override
	public void setAttribute(String attributeName, boolean value) {
		if( !isSecureKey(attributeName)) {
			super.setAttribute(attributeName, value);
			return;
		}
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				secureStorage.getSecureStorage().getNode(securedNode)
					.setBooleanProperty(attributeName, value);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
	}

	@Override
	public void setAttribute(String attributeName, String value) {
		if( !isSecureKey(attributeName)) {
			super.setAttribute(attributeName, value);
			return;
		}
		
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			try {
				secureStorage.getSecureStorage().getNode(securedNode)
					.setStringProperty(attributeName, value);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
	}

	@Override
	public void setAttribute(String attributeName, List<String> value) {
		if( !isSecureKey(attributeName)) {
			super.setAttribute(attributeName, value);
			return;
		}
		
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			Gson gson = new Gson();
			String val = gson.toJson(value);
			try {
				secureStorage.getSecureStorage().getNode(securedNode)
					.setStringProperty(attributeName, val);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
	}

	@Override
	public void setAttribute(String attributeName, Map value) {
		if( !isSecureKey(attributeName)) {
			super.setAttribute(attributeName, value);
			return;
		}
		
		if( canAccessSecureStorage() ) {
			String securedNode = getSecuredKey();
			Gson gson = new Gson();
			String val = gson.toJson(value);
			try {
				secureStorage.getSecureStorage().getNode(securedNode)
					.setStringProperty(attributeName, val);
			} catch(CryptoException ce) {
				// TODO log
			}
		}
	}
	
	
	private String getSecuredKey() {
		return ServerCoreActivator.BUNDLE_ID + "/servers/" + getId() + "/"; 
	}
	
	private boolean canAccessSecureStorage() {
		return canAccessSecureStorage(true);
	}
	
	private boolean canAccessSecureStorage(boolean prompt) {
		return secureStorage != null && secureStorage.getSecureStorage(prompt) != null;
	}
	
}