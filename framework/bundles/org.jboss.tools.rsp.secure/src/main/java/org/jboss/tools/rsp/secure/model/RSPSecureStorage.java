/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.secure.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.crypto.CryptoUtils;
import org.jboss.tools.rsp.secure.crypto.NotInitializedCryptoException;

public class RSPSecureStorage implements ISecureStorage {
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";
	
	private static final String COMMENT = "rsp secure storage";
	private static final String HASH_COMMENT = "#" + COMMENT;

	private File backingFile;
	private byte[] key;

	private Properties secretData;
	private CryptoUtils util;

	public RSPSecureStorage(File file, byte[] key) {
		this(file, key, ALGORITHM, TRANSFORMATION);
	}
	
	public RSPSecureStorage(File file, byte[] key, String algo, String transform) {
		this.backingFile = file;
		this.util = new CryptoUtils(algo, transform);
		this.key = util.keyTo16(key);
	}

	public void setKey(byte[] key) {
		this.key = key;
		this.secretData = null;
	}

	public void load() throws CryptoException {
		Properties tmp = new Properties();
		if (backingFile != null && backingFile.exists()) {
			try {
				byte[] encrypted = util.getBytesFromFile(backingFile);
				byte[] decrypted = util.decrypt(key, encrypted);
				byte[] magicBytes = HASH_COMMENT.getBytes();
				if (!startsWith(decrypted, magicBytes)) {
					throw new CryptoException("Invalid key", null);
				}
				String test = new String(decrypted);
				tmp.load(new ByteArrayInputStream(decrypted));
				Arrays.fill(decrypted, (byte) 0);
				this.secretData = tmp;
			} catch (IOException | CryptoException e) {
				throw new CryptoException("Unable to decrypt secure storage", e);
			}
		} else if (!backingFile.exists()) {
			// new file, it's ok to create
			this.secretData = tmp;
		}
	}

	private boolean startsWith(byte[] all, byte[] magic) {
		for (int i = 0; i < magic.length; i++) {
			if (i >= all.length) {
				return false;
			}
			if (all[i] != magic[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean isInitialized() {
		return secretData != null;
	}

	public void save() throws CryptoException {
		try {
			if (backingFile != null && isInitialized()) {
				backingFile.getParentFile().mkdirs();
				StringWriter sw = new StringWriter();
				this.secretData.store(sw, COMMENT);
				byte[] raw = sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
				byte[] encrypted = util.encrypt(key, raw);
				util.writeBytesToFile(backingFile, encrypted);
			} else if (!isInitialized()) {
				throw new NotInitializedCryptoException("Secure storage not initialized", null);
			}
		} catch (IOException ioe) {
			throw new CryptoException("Secure storage not initialized", ioe);
		}
	}

	@Override
	public boolean propertyExists(String nodePath, String propertyName) throws NotInitializedCryptoException {
		if (!isInitialized()) {
			throw new NotInitializedCryptoException("Secure storage not initialized", null);
		}
		return ((SecureNode) getNode(nodePath)).propertyExists(propertyName);
	}

	public ISecureNode getNode(String nodePath) throws NotInitializedCryptoException {
		if (!isInitialized()) {
			throw new NotInitializedCryptoException("Secure storage not initialized", null);
		}
		return new SecureNode(nodePath);
	}

	public class SecureNode implements ISecureNode {
		private String path;

		public SecureNode(String path) {
			this.path = path;
		}

		public SecureNode getChildNode(String segment) {
			return new SecureNode(append(segment));
		}

		private String append(String segment) {
			String toCheck = path;
			if (!path.endsWith("/"))
				toCheck = toCheck + "/";
			toCheck += segment;
			return toCheck;
		}

		public boolean propertyExists(String property) {
			return secretData.containsKey(append(property));
		}

		public String getStringProperty(String prop, String defaultValue) {
			String ret = secretData.getProperty(append(prop));
			return ret == null ? defaultValue : ret;
		}

		public void setStringProperty(String prop, String val) {
			secretData.setProperty(append(prop), val);
			try {
				save();
			} catch (CryptoException ce) {
				// TODO log it
			}
		}

		public int getIntegerProperty(String prop, int defaultValue) {
			String asString = getStringProperty(prop, null);
			if (asString == null)
				return defaultValue;
			try {
				return Integer.parseInt(asString);
			} catch (NumberFormatException nfe) {
				return defaultValue;
			}
		}

		public void setIntegerProperty(String prop, int val) {
			secretData.setProperty(append(prop), Integer.toString(val));
		}

		public boolean getBooleanProperty(String prop, boolean defaultValue) {
			String asString = getStringProperty(prop, null);
			if (asString == null)
				return defaultValue;
			return Boolean.parseBoolean(asString);
		}

		public void setBooleanProperty(String prop, boolean val) {
			secretData.setProperty(append(prop), Boolean.toString(val));
		}
	}
}
