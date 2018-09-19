/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.secure;

import java.io.File;
import java.util.HashMap;
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

public class SecureStorageGuardian implements ISecureStorageProvider {
	private File file;
	private Map<RSPClient, byte[]> permissions;
	private ISecureStorage storage = null;
	public SecureStorageGuardian(File file) {
		this.file = file;
		this.permissions = new HashMap<>();
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
	public void authenticateClient(RSPClient client, ICapabilityManagement capabilities) throws InterruptedException, ExecutionException {
		if( canPromptClient(client, capabilities)) {
			String msg = "Please provide a secure-storage password to either create a new, or load an existing, secure storage."; 
			StringPrompt prompt = new StringPrompt(100, msg);
			String secureKey = client.promptString(prompt).get();
			int tries = 0;
			while(secureKey != null && secureKey.length() != 0 && tries < 10) {
				try {
					addClient(client, secureKey.getBytes());
					// success at decrypting the file, or, file didn't exist yet
					return;
				} catch(CryptoException ce) {
					// TODO log
					ce.printStackTrace();
				}
				tries += 1;
			}
		}
	}
	
	private boolean canPromptClient(RSPClient client, ICapabilityManagement capabilities) {
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
		return null;
	}
}
