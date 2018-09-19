package org.jboss.tools.rsp.secure.model;

import java.io.IOException;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.crypto.NotInitializedCryptoException;

public interface ISecureStorage {
	public void load() throws CryptoException;
	public void save() throws IOException, CryptoException;
	
	public ISecureNode getNode(String node) throws NotInitializedCryptoException;
	
	public boolean propertyExists(String nodePath, String propertyName)  throws NotInitializedCryptoException;
	
	public interface ISecureNode {
		public ISecureNode getChildNode(String segment);
		public String getStringProperty(String prop, String defaultValue);
		public void setStringProperty(String prop, String val);
		public int getIntegerProperty(String prop, int defaultValue);
		public void setIntegerProperty(String prop, int val);
		public boolean getBooleanProperty(String prop, boolean defaultValue);
		public void setBooleanProperty(String prop, boolean val);
	}
}
