/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;

public class MinishiftServerTypes {
	public static final String MINISHIFT_1_12_ID = "org.jboss.tools.openshift.cdk.server.type.minishift.v1_12";
	public static final String MINISHIFT_1_12_NAME = "Minishift 1.12+";
	public static final String MINISHIFT_1_12_DESC = "A server adapter capable of controlling a Minishift 1.12+ runtime instance.";


	public static final BaseMinishiftServerType MINISHIFT_1_12_SERVER_TYPE = 
			new MinishiftServerType(MINISHIFT_1_12_ID, MINISHIFT_1_12_NAME, MINISHIFT_1_12_DESC);
}
