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
	 * JBoss Optional Attributes
	 */
	public static final String VM_INSTALL_PATH = "vm.install.path";
	public static final String JBOSS_SERVER_HOST = "jboss.server.host";
	public static final String JBOSS_SERVER_HOST_DEFAULT = "localhost";
	public static final String JBOSS_SERVER_PORT = "jboss.server.port";
	public static final int JBOSS_SERVER_PORT_DEFAULT = 8080;

	public static final String WILDFLY_CONFIG_FILE_DEFAULT = "standalone.xml";
	public static final String WILDFLY_CONFIG_FILE = "wildfly.server.config.file";
	public static final String WILDFLY_DEPLOY_DIR = "wildfly.server.deploy.directory";
	
	
	public static final String WILDFLY_PUBLISH_RESTART_PATTERN_KEY = "wildfly.publish.restart.pattern";
	public static final String WILDFLY_PUBLISH_RESTART_PATTERN_DEFAULT = "\\.class$|\\.jar$";
	

	/*
	 * Launch attributes
	 */
	public static final String LAUNCH_OVERRIDE_BOOLEAN = "args.override.boolean";
	public static final String LAUNCH_OVERRIDE_PROGRAM_ARGS = "program.args.override.string";
	public static final String LAUNCH_OVERRIDE_VM_ARGS = "vm.args.override.string";
	
}
