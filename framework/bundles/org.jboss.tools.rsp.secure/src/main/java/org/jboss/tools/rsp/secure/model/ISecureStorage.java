/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.secure.model;

import java.io.IOException;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.crypto.NotInitializedCryptoException;

/**
 * ISecureStorage is the interface for accessing, reading, and writing to a
 * secure file that is stored on disk in a secure manner.
 * 
 */
public interface ISecureStorage {

	/**
	 * Load the secure storage.
	 * 
	 * @throws CryptoException
	 */
	public void load() throws CryptoException;

	/**
	 * Save the secure storage
	 * 
	 * @throws IOException
	 * @throws CryptoException
	 */
	public void save() throws IOException, CryptoException;

	/**
	 * Get an object representing a given property domain. This node will be able to
	 * give access to child nodes, or set properties directly for this node.
	 * 
	 * @param node
	 * @return
	 * @throws NotInitializedCryptoException
	 */
	public ISecureNode getNode(String node) throws NotInitializedCryptoException;

	/**
	 * Check if a given property exists in the secure storage model
	 * 
	 * @param nodePath
	 * @param propertyName
	 * @return
	 * @throws NotInitializedCryptoException
	 */
	public boolean propertyExists(String nodePath, String propertyName) throws NotInitializedCryptoException;

	/**
	 * A secure storage node represents access to a given domain of properties,
	 * including any child domains or any properties set directly in this domain
	 */
	public interface ISecureNode {
		/**
		 * Get a child domain for this node
		 * 
		 * @param segment
		 * @return
		 */
		public ISecureNode getChildNode(String segment);

		/**
		 * Get a string property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param defaultValue
		 * @return
		 */
		public String getStringProperty(String prop, String defaultValue);

		/**
		 * Set a string property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param val
		 */
		public void setStringProperty(String prop, String val);

		/**
		 * Get an integer property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param defaultValue
		 * @return
		 */
		public int getIntegerProperty(String prop, int defaultValue);

		/**
		 * Set an integer property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param val
		 */
		public void setIntegerProperty(String prop, int val);

		/**
		 * Get a boolean property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param defaultValue
		 * @return
		 */
		public boolean getBooleanProperty(String prop, boolean defaultValue);

		/**
		 * Set a boolean property in the current ISecureNode domain
		 * 
		 * @param prop
		 * @param val
		 */
		public void setBooleanProperty(String prop, boolean val);
	}
}
