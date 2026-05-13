/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
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

public class Wildfly340DefaultLaunchArguments extends JBoss71DefaultLaunchArguments {
	public Wildfly340DefaultLaunchArguments(IServer s) {
		super(s);
	}

	@Override
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m "; //$NON-NLS-1$
	}

	@Override
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs()
				+ "-Dorg.jboss.logmanager.nocolor=true -Djboss.bind.address.management=localhost " //$NON-NLS-1$
				+ getJdkSerialFilter();
	}

	@Override
	protected String getJaxpProvider() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the JDK serial filter argument for protection against deserialization attacks.
	 *
	 * @return the serial filter argument
	 */
	protected String getJdkSerialFilter() {
		return "-Djdk.serialFilter=\"maxbytes=10485760;maxdepth=128;maxarray=100000;maxrefs=300000\" "; //$NON-NLS-1$
	}

}
