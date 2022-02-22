/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IEapXpServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String PROJECT_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	public static final String VM_INSTALL_PATH = "vm.install.path";
	public static final String MAVEN_BIN = "maven.install.path.binary";
	// JAVA_HOME=/home/rob/apps/java/java11/jdk-11.0.13/  mvn -Pbootable-jar wildfly-jar:dev-watch -Dwildfly.bootable.debug=true -Dwildfly.bootable.debug.port=65101

}
