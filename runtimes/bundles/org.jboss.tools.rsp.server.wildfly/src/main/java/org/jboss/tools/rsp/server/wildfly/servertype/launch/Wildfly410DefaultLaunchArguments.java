/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class Wildfly410DefaultLaunchArguments extends Wildfly340DefaultLaunchArguments {
	public Wildfly410DefaultLaunchArguments(IServer s) {
		super(s);
	}

	@Override
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs()
				+ getJavaVMArgs();
	}

}
