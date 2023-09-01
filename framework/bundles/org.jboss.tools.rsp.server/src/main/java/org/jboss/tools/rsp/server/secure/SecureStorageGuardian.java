/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.secure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.model.ISecureStorage;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.secure.model.RSPSecureStorage;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureStorageGuardian implements ISecureStorageProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecureStorageGuardian.class);
	
	
	private File file;
	private Map<RSPClient, byte[]> permissions;
	private Map<RSPClient, Integer> maxTriesReached;
	private ISecureStorage storage = null;
	private ICapabilityManagement capabilities;
	public SecureStorageGuardian(File file, ICapabilityManagement capabilities) {
		this.file = file;
		this.capabilities = capabilities;
		this.permissions = new HashMap<>();
		this.maxTriesReached = new HashMap<>();
	}
	
	public void addClient(RSPClient client, byte[] key) throws CryptoException {
		RSPSecureStorage test = checkKey(key);
		if( storage == null ) {
			storage = test;
		}
		permissions.put(client, key);
	}

	public void removeClient(RSPClient client) {
		permissions.remove(client);
		maxTriesReached.remove(client);
	}

	private RSPSecureStorage checkKey(byte[] key) throws CryptoException {
		RSPSecureStorage tmp = new RSPSecureStorage(file, key);
		tmp.load();
		if( !file.exists()) {
			tmp.save();
		}
		return tmp;
	}
	
	/**
	 * Repeatedly prompt the user for a decryption key until they
	 * either provide an empty / null response, or choose the correct key.
	 * 
	 * Try a maximum of 10 times.
	 * 
	 * @param client
	 * @param capabilities
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void authenticateClient(RSPClient client, int maxTries) throws InterruptedException, ExecutionException {
		if( canPromptClient(client, capabilities)) {
			String msg = "Please provide a secure-storage password to either create a new, or load an existing, secure storage."; 
			String failedMsg = "The provided password did not decrypt the secure storage. ";
			int tries = maxTriesReached.get(client) == null ? 0 : maxTriesReached.get(client);
			while(tries < maxTries) {
				String msgToUse = (tries == 0 ? msg : failedMsg + msg);
				StringPrompt prompt = new StringPrompt(100, msgToUse, true);
				String secureKey = client.promptString(prompt).get();
				tries = tries + 1;
				maxTriesReached.put(client, tries);
				boolean empty = secureKey == null || secureKey.length() == 0 || secureKey.trim().length() == 0;
				if( empty ) {
					return;
				}
				try {
					addClient(client, secureKey.getBytes());
					// success at decrypting the file, or, file didn't exist yet
					return;
				} catch(CryptoException ce) {
					LOG.error(ce.getMessage(), ce);
				}
			}
		}
	}
	
	private boolean canPromptClient(RSPClient client, ICapabilityManagement capabilities) {
		if( client == null )
			return false;
		
		String prop = capabilities.getCapabilityProperty(client,
				ICapabilityKeys.STRING_PROTOCOL_VERSION);
		if( prop != null && !ICapabilityKeys.PROTOCOL_VERSION_0_9_0.equals(prop)) {
			String canPrompt = capabilities.getCapabilityProperty(client,
					ICapabilityKeys.BOOLEAN_STRING_PROMPT);
			return Boolean.parseBoolean(canPrompt);
		}
		return false;
	}

	@Override
	public ISecureStorage getSecureStorage() {
		RSPClient rspc = ClientThreadLocal.getActiveClient();
		if( rspc != null && permissions.containsKey(rspc) && permissions.get(rspc) != null ) {
			return storage;
		}
		
		// A thread with no client associated with it but has elevated permissions
		// can access the secure storage without validation
		if( rspc == null && currentThreadHasSystemPermissions()) {
			return storage;
		}
		
		return null;
	}
	
	public ISecureStorage getSecureStorage(boolean prompt) {
		ISecureStorage storage = getSecureStorage();
		RSPClient rspc = ClientThreadLocal.getActiveClient();
		int maxTries = 4;
		int tries = maxTriesReached.get(rspc) == null ? 0 : maxTriesReached.get(rspc);
		if( rspc != null && storage == null && prompt && tries <= maxTries) {
			try {
				authenticateClient(rspc, maxTries);
				if( getSecureStorage() != null ) {
					return getSecureStorage();
				}
			} catch(InterruptedException | ExecutionException ie) {
				if( ie instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				LOG.error(ie.getMessage(), ie);
			}
		}
		return storage;
	}

	
	
	private List<Thread> systemPermissions = new ArrayList<Thread>();
	@Override
	public boolean currentThreadHasSystemPermissions() {
		return systemPermissions.contains(Thread.currentThread());
	}

	@Override
	public void grantCurrentThreadSystemPermissions() {
		systemPermissions.add(Thread.currentThread());
	}

	@Override
	public void revokeCurrentThreadSystemPermissions() {
		systemPermissions.remove(Thread.currentThread());
	}

}
