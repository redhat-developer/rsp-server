/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc.
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
import org.jboss.tools.rsp.server.wildfly.servertype.launch.Wildfly110DefaultLaunchArguments;

public class Wildfly140ExtendedProperties extends AbstractWildflyExtendedProperties {

	public Wildfly140ExtendedProperties(IServer server) {
		super("14", "1.8", "11.0", "service:jmx:remote+http", new Wildfly110DefaultLaunchArguments(server), server);
	}
}
