/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

public interface IServerConstants {
	public static final String SERVER_AS_PREFIX = "org.jboss.ide.eclipse.as."; //$NON-NLS-1$
	public static final String SERVER_AS_32 = "org.jboss.ide.eclipse.as.32"; //$NON-NLS-1$
	public static final String SERVER_AS_40 = "org.jboss.ide.eclipse.as.40"; //$NON-NLS-1$
	public static final String SERVER_AS_42 = "org.jboss.ide.eclipse.as.42"; //$NON-NLS-1$
	public static final String SERVER_AS_50 = "org.jboss.ide.eclipse.as.50"; //$NON-NLS-1$
	public static final String SERVER_AS_51 = "org.jboss.ide.eclipse.as.51"; //$NON-NLS-1$
	public static final String SERVER_AS_60 = "org.jboss.ide.eclipse.as.60"; //$NON-NLS-1$
	public static final String SERVER_AS_70 = "org.jboss.ide.eclipse.as.70"; //$NON-NLS-1$
	public static final String SERVER_AS_71 = "org.jboss.ide.eclipse.as.71"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_80 = "org.jboss.ide.eclipse.as.wildfly.80"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_90 = "org.jboss.ide.eclipse.as.wildfly.90"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_100 = "org.jboss.ide.eclipse.as.wildfly.100"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_110 = "org.jboss.ide.eclipse.as.wildfly.110"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_120 = "org.jboss.ide.eclipse.as.wildfly.120"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_130 = "org.jboss.ide.eclipse.as.wildfly.130"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_140 = "org.jboss.ide.eclipse.as.wildfly.140"; //$NON-NLS-1$
	public static final String WF_SERVER_PREFIX = "org.jboss.ide.eclipse.as.wildfly."; //$NON-NLS-1$
	public static final String EAP_SERVER_PREFIX = "org.jboss.ide.eclipse.as.eap."; //$NON-NLS-1$
	public static final String SERVER_EAP_43 = "org.jboss.ide.eclipse.as.eap.43"; //$NON-NLS-1$
	public static final String SERVER_EAP_50 = "org.jboss.ide.eclipse.as.eap.50"; //$NON-NLS-1$
	public static final String SERVER_EAP_60 = "org.jboss.ide.eclipse.as.eap.60"; //$NON-NLS-1$
	public static final String SERVER_EAP_61 = "org.jboss.ide.eclipse.as.eap.61"; //$NON-NLS-1$
	public static final String SERVER_EAP_70 = "org.jboss.ide.eclipse.as.eap.70"; //$NON-NLS-1$
	public static final String SERVER_EAP_71 = "org.jboss.ide.eclipse.as.eap.71"; //$NON-NLS-1$

	public static final String[] ALL_JBOSS_SERVERS = new String[] {
			SERVER_AS_32,SERVER_AS_40,SERVER_AS_42,SERVER_AS_50,SERVER_AS_51,
			SERVER_AS_60,SERVER_AS_70,SERVER_AS_71,
			SERVER_WILDFLY_80,SERVER_WILDFLY_90,SERVER_WILDFLY_100,
			SERVER_WILDFLY_110,SERVER_WILDFLY_120,SERVER_WILDFLY_130,SERVER_WILDFLY_140,
			SERVER_EAP_43,SERVER_EAP_50,SERVER_EAP_60, SERVER_EAP_61, SERVER_EAP_70, SERVER_EAP_71
		};
		// NEW_SERVER_ADAPTER Add the new server id above this line

	// TODO move elsewhere
	public static final String EXT_JAR = ".jar";


}
