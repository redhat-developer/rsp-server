/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class MinishiftPropertyUtility {
	public static String getMinishiftCommand(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String) null);
	}

	public static String getMinishiftVMDriver(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_VM_DRIVER, (String) null);
	}

	public static String getMinishiftProfile(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_PROFILE,
				IMinishiftServerAttributes.MINISHIFT_PROFILE_DEFAULT);
	}

	public static String getMinishiftUsername(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, (String) null);
	}

	public static String getMinishiftPassword(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, (String) null);
	}

}
