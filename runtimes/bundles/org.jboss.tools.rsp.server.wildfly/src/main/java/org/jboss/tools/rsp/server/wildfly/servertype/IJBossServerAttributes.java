/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IJBossServerAttributes extends DefaultServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	
	
	/*
	 * Optional Attributes
	 */
	public static final String VM_INSTALL_PATH = "vm.install.path";

	/*
	 * Launch attributes
	 */
	
	
}
