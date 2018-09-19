package org.jboss.tools.rsp.secure.model;

public class NullSecureStorageProvider implements ISecureStorageProvider {
	@Override
	public ISecureStorage getSecureStorage() {
		return null;
	}
}
