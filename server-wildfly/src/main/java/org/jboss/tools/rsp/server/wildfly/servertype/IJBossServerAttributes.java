/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

public interface IJBossServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = ServerManagementAPIConstants.SERVER_HOME_DIR;
	
	
	
	/*
	 * Optional Attributes
	 */
	public static final String VM_INSTALL_PATH = "vm.install.path";
	
	
	/*
	 * Launch attributes
	 */
	
	
}
