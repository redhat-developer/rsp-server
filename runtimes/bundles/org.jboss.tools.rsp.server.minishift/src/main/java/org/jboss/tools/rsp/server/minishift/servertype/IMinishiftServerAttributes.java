/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.server.spi.model.IServerModel;

public interface IMinishiftServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String MINISHIFT_BINARY = ServerManagementAPIConstants.SERVER_HOME_FILE;
	
	
	/*
	 * Optional
	 */
	public static final String MINISHIFT_VM_DRIVER = "minishift.vmdriver";

	public static final String MINISHIFT_PROFILE = "minishift.profile";
	
	public static final String MINISHIFT_HOME = "minishift.home";
	
	public static final String MINISHIFT_PROFILE_DEFAULT = "minishift";
	
	public static final String MINISHIFT_CPUS = "cpu";
	
	public static final String MINISHIFT_MEMORY = "memory";

	
	// Only for CDK
	public static final String MINISHIFT_REG_USERNAME = IServerModel.SECURE_ATTRIBUTE_PREFIX + "minishift.username";

	public static final String MINISHIFT_REG_PASSWORD = IServerModel.SECURE_ATTRIBUTE_PREFIX + "minishift.password";
	
	//Only for CRC
	public static final String CRC_IMAGE_PULL_SECRET = "crc.image.pull.secret";
	
	public static final String CRC_BUNDLE = "crc.bundle";

	
	/*
	 * Launch attributes
	 */
	public static final String LAUNCH_OVERRIDE_BOOLEAN 			= "server.args.launch.override.boolean";
	public static final String OLD_LAUNCH_OVERRIDE_BOOLEAN = "args.override.boolean";
	
	
}
