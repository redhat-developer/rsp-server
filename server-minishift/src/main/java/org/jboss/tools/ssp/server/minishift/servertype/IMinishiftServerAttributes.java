/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.minishift.servertype;

public interface IMinishiftServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String MINISHIFT_BINARY = "minishift.binary.path";
	
	public static final String MINISHIFT_VM_DRIVER = "minishift.vmdriver";

	
	// Only for CDK
	public static final String MINISHIFT_REG_USERNAME = "minishift.username";

	public static final String MINISHIFT_REG_PASSWORD = "minishift.password";

	
	/*
	 * Launch attributes
	 */
	
	
}
