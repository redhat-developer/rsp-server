/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.secure.model;

public class NullSecureStorageProvider implements ISecureStorageProvider {
	@Override
	public ISecureStorage getSecureStorage() {
		return null;
	}

	@Override
	public ISecureStorage getSecureStorage(boolean prompt) {
		return null;
	}

	@Override
	public boolean currentThreadHasSystemPermissions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void grantCurrentThreadSystemPermissions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revokeCurrentThreadSystemPermissions() {
		// TODO Auto-generated method stub
		
	}
}
