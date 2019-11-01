/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import java.io.File;
import java.nio.file.Path;

import org.jboss.tools.rsp.server.spi.publishing.AbstractPublishController;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardJBossPublishController extends AbstractPublishController {

	private static final Logger LOG = LoggerFactory.getLogger(StandardJBossPublishController.class);
	
	
	private static final String[] supportedSuffix = new String[] {
		".jar", ".war", ".ear", ".rar", ".xml"
	};
	
	public StandardJBossPublishController(IServer server, AbstractJBossServerDelegate delegate) {
		super(server, delegate);
	}
	
	protected Path getDeploymentFolder() {
		// TODO this may need to be abstracted out eventually if we 
		// support things like custom config folders etc. 
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		Path p = new File(home).toPath().resolve("server").resolve("default").resolve("deploy");
		return p;
	}
	
	@Override
	protected String[] getSupportedSuffixes() {
		return supportedSuffix;
	}
	
}
