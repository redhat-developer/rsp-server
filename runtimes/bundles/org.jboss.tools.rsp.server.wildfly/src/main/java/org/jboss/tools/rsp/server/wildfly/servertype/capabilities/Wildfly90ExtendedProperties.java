/*******************************************************************************
 * Copyright (c) 2015-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.Wildfly80DefaultLaunchArguments;

public class Wildfly90ExtendedProperties extends AbstractWildflyExtendedProperties {

	public Wildfly90ExtendedProperties(IServer server) {
		super("9.x", "1.7", "1.8", "service:jmx:remote+http", new Wildfly80DefaultLaunchArguments(server), server);
	}
	
}
