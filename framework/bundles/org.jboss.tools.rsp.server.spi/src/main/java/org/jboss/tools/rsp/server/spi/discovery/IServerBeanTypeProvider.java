/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.discovery;

/**
 * This is a provider of server bean types, in order,
 * that should be queried as to whether a given folder
 * is of that type of server.
 */
public interface IServerBeanTypeProvider {
	/**
	 * Return the list of server bean types 
	 * @return
	 */
	public ServerBeanType[] getServerBeanTypes();
}
