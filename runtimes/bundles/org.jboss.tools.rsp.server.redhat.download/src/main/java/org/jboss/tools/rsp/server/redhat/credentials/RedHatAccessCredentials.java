package org.jboss.tools.rsp.server.redhat.credentials;

import org.jboss.tools.rsp.secure.crypto.NotInitializedCryptoException;
import org.jboss.tools.rsp.secure.model.ISecureStorage;
import org.jboss.tools.rsp.secure.model.ISecureStorage.ISecureNode;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.redhat.download.impl.Activator;

public class RedHatAccessCredentials {
	private static final String NODE_PREFIX = Activator.BUNDLE_ID + "/redhat/global/";
	private static final String USERNAME_GLOBAL_PREFERENCE = NODE_PREFIX + "redhat.access.global.username";
	private static final String PASSWORD_GLOBAL_PREFERENCE = NODE_PREFIX + "redhat.access.global.password";

	public static final String getGlobalRedhatUser(ISecureStorageProvider storage) {
		try {
			ISecureStorage storage2 = storage.getSecureStorage(true);
			if( storage2 != null ) {
				ISecureNode node = storage2.getNode(NODE_PREFIX);
				return node.getStringProperty(USERNAME_GLOBAL_PREFERENCE, (String) null);
			}
		} catch (NotInitializedCryptoException e) {
		}
		return null;
	}

	public static final String getGlobalRedhatPassword(ISecureStorageProvider storage) {
		try {
			ISecureStorage storage2 = storage.getSecureStorage(true);
			if( storage2 != null ) {
				ISecureNode node = storage2.getNode(NODE_PREFIX);
				return node.getStringProperty(PASSWORD_GLOBAL_PREFERENCE, (String) null);
			}
		} catch (NotInitializedCryptoException e) {
		}
		return null;
	}
	

	public static final boolean setGlobalRedhatUser(ISecureStorageProvider storage, String val) {
		try {
			ISecureStorage storage2 = storage.getSecureStorage(true);
			if( storage2 != null ) {
				ISecureNode node = storage2.getNode(NODE_PREFIX);
				node.setStringProperty(USERNAME_GLOBAL_PREFERENCE, val);
				return true;
			}
		} catch (NotInitializedCryptoException e) {
		}
		return false;
	}

	public static final boolean setGlobalRedhatPassword(ISecureStorageProvider storage, String val) {
		try {
			ISecureStorage storage2 = storage.getSecureStorage(true);
			if( storage2 != null ) {
				ISecureNode node = storage2.getNode(NODE_PREFIX);
				node.setStringProperty(PASSWORD_GLOBAL_PREFERENCE, val);
				return true;
			}
		} catch (NotInitializedCryptoException e) {
		}
		return false;
	}
}
