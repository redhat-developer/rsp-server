/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;

public class MinishiftServerTypes {
	
	public static final String MINISHIFT_1_12_ID = "org.jboss.tools.openshift.cdk.server.type.minishift.v1_12";
	public static final String MINISHIFT_1_12_NAME = "Minishift 1.12+";
	public static final String MINISHIFT_1_12_DESC = "A server adapter capable of controlling a Minishift 1.12+ runtime instance.";

	public static final String CDK_3X_ID = "org.jboss.tools.openshift.cdk.server.type.minishift.cdk.3x";
	public static final String CDK_3X_NAME = "CDK 3.x";
	public static final String CDK_3X_DESC = "A server adapter capable of controlling a CDK 3.x runtime instance.";

	public static final String RUNTIME_MINISHIFT_17_ID = "org.jboss.tools.openshift.cdk.server.runtime.type.minishift.17";
	public static final String RUNTIME_CDK_30_ID = "org.jboss.tools.openshift.cdk.server.runtime.type.cdk.30";
	public static final String RUNTIME_CDK_32_ID = "org.jboss.tools.openshift.cdk.server.runtime.type.cdk.32";
	
	public static final BaseMinishiftServerType MINISHIFT_1_12_SERVER_TYPE = 
			new MinishiftServerType(MINISHIFT_1_12_ID, MINISHIFT_1_12_NAME, MINISHIFT_1_12_DESC);

	public static final BaseMinishiftServerType CDK_3X_SERVER_TYPE = 
			new CDKServerType(CDK_3X_ID, CDK_3X_NAME, CDK_3X_DESC);
	
	public static final Map<String, String> RUNTIME_TO_SERVER = new HashMap<>();
	static {
		RUNTIME_TO_SERVER.put(RUNTIME_MINISHIFT_17_ID, MINISHIFT_1_12_ID);
		RUNTIME_TO_SERVER.put(RUNTIME_CDK_30_ID, CDK_3X_ID);
		RUNTIME_TO_SERVER.put(RUNTIME_CDK_32_ID, CDK_3X_ID);
	};

	private MinishiftServerTypes() {
		// inhibit instantiation
	}

}
