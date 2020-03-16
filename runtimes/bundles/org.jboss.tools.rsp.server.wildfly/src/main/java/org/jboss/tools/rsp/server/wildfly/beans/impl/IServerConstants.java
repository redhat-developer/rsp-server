/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.util.HashMap;
import java.util.Map;

public interface IServerConstants {

	public static final String AS_SERVER_PREFIX = "org.jboss.ide.eclipse.as."; //$NON-NLS-1$
	public static final String WF_SERVER_PREFIX = "org.jboss.ide.eclipse.as.wildfly."; //$NON-NLS-1$
	public static final String EAP_SERVER_PREFIX = "org.jboss.ide.eclipse.as.eap."; //$NON-NLS-1$
	
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
	public static final String SERVER_WILDFLY_150 = "org.jboss.ide.eclipse.as.wildfly.150"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_160 = "org.jboss.ide.eclipse.as.wildfly.160"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_170 = "org.jboss.ide.eclipse.as.wildfly.170"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_180 = "org.jboss.ide.eclipse.as.wildfly.180"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_190 = "org.jboss.ide.eclipse.as.wildfly.190"; //$NON-NLS-1$

	public static final String SERVER_EAP_43 = "org.jboss.ide.eclipse.as.eap.43"; //$NON-NLS-1$
	public static final String SERVER_EAP_50 = "org.jboss.ide.eclipse.as.eap.50"; //$NON-NLS-1$
	public static final String SERVER_EAP_60 = "org.jboss.ide.eclipse.as.eap.60"; //$NON-NLS-1$
	public static final String SERVER_EAP_61 = "org.jboss.ide.eclipse.as.eap.61"; //$NON-NLS-1$
	public static final String SERVER_EAP_70 = "org.jboss.ide.eclipse.as.eap.70"; //$NON-NLS-1$
	public static final String SERVER_EAP_71 = "org.jboss.ide.eclipse.as.eap.71"; //$NON-NLS-1$
	public static final String SERVER_EAP_72 = "org.jboss.ide.eclipse.as.eap.72"; //$NON-NLS-1$
	public static final String SERVER_EAP_73 = "org.jboss.ide.eclipse.as.eap.73"; //$NON-NLS-1$

	public static final String[] ALL_JBOSS_SERVERS = new String[] {
			SERVER_AS_32,SERVER_AS_40,SERVER_AS_42,SERVER_AS_50,SERVER_AS_51,
			SERVER_AS_60,SERVER_AS_70,SERVER_AS_71,
			SERVER_WILDFLY_80,SERVER_WILDFLY_90,SERVER_WILDFLY_100,
			SERVER_WILDFLY_110,SERVER_WILDFLY_120,SERVER_WILDFLY_130,
			SERVER_WILDFLY_140, SERVER_WILDFLY_150, SERVER_WILDFLY_160, 
			SERVER_WILDFLY_170, SERVER_WILDFLY_180,SERVER_WILDFLY_190,
			SERVER_EAP_43,SERVER_EAP_50,SERVER_EAP_60, SERVER_EAP_61, 
			SERVER_EAP_70, SERVER_EAP_71, SERVER_EAP_72, SERVER_EAP_73
		};
		// NEW_SERVER_ADAPTER Add the new server id above this line

	
	
	// Turns out the old runtime strings are required for integration with
	// the stacks.yaml and download-runtimes keys. MEH!
	
	public static final String AS_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime."; //$NON-NLS-1$
	public static final String WF_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime.wildfly."; //$NON-NLS-1$
	public static final String EAP_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime.eap."; //$NON-NLS-1$
	
	public static final String RUNTIME_AS_32 = "org.jboss.ide.eclipse.as.runtime.32"; //$NON-NLS-1$
	public static final String RUNTIME_AS_40 = "org.jboss.ide.eclipse.as.runtime.40"; //$NON-NLS-1$
	public static final String RUNTIME_AS_42 = "org.jboss.ide.eclipse.as.runtime.42"; //$NON-NLS-1$
	public static final String RUNTIME_AS_50 = "org.jboss.ide.eclipse.as.runtime.50"; //$NON-NLS-1$
	public static final String RUNTIME_AS_51 = "org.jboss.ide.eclipse.as.runtime.51"; //$NON-NLS-1$
	public static final String RUNTIME_AS_60 = "org.jboss.ide.eclipse.as.runtime.60"; //$NON-NLS-1$
	public static final String RUNTIME_AS_70 = "org.jboss.ide.eclipse.as.runtime.70"; //$NON-NLS-1$
	public static final String RUNTIME_AS_71 = "org.jboss.ide.eclipse.as.runtime.71"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_80 = "org.jboss.ide.eclipse.as.runtime.wildfly.80"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_90 = "org.jboss.ide.eclipse.as.runtime.wildfly.90"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_100 = "org.jboss.ide.eclipse.as.runtime.wildfly.100"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_110 = "org.jboss.ide.eclipse.as.runtime.wildfly.110"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_120 = "org.jboss.ide.eclipse.as.runtime.wildfly.120"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_130 = "org.jboss.ide.eclipse.as.runtime.wildfly.130"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_140 = "org.jboss.ide.eclipse.as.runtime.wildfly.140"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_150 = "org.jboss.ide.eclipse.as.runtime.wildfly.150"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_160 = "org.jboss.ide.eclipse.as.runtime.wildfly.160"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_170 = "org.jboss.ide.eclipse.as.runtime.wildfly.170"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_180 = "org.jboss.ide.eclipse.as.runtime.wildfly.180"; //$NON-NLS-1$
	public static final String RUNTIME_WILDFLY_190 = "org.jboss.ide.eclipse.as.runtime.wildfly.190"; //$NON-NLS-1$

	public static final String RUNTIME_EAP_43 = "org.jboss.ide.eclipse.as.runtime.eap.43"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_50 = "org.jboss.ide.eclipse.as.runtime.eap.50"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_60 = "org.jboss.ide.eclipse.as.runtime.eap.60"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_61 = "org.jboss.ide.eclipse.as.runtime.eap.61"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_70 = "org.jboss.ide.eclipse.as.runtime.eap.70"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_71 = "org.jboss.ide.eclipse.as.runtime.eap.71"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_72 = "org.jboss.ide.eclipse.as.runtime.eap.72"; //$NON-NLS-1$
	public static final String RUNTIME_EAP_73 = "org.jboss.ide.eclipse.as.runtime.eap.73"; //$NON-NLS-1$

	public static final Map<String, String> RUNTIME_TO_SERVER = new HashMap<String, String>() {{
		put(RUNTIME_AS_32, SERVER_AS_32);
		put(RUNTIME_AS_40, SERVER_AS_40);
		put(RUNTIME_AS_42, SERVER_AS_42);
		put(RUNTIME_AS_50, SERVER_AS_50);
		put(RUNTIME_AS_51, SERVER_AS_51);
		put(RUNTIME_AS_60, SERVER_AS_60);
		put(RUNTIME_AS_70, SERVER_AS_70);
		put(RUNTIME_AS_71, SERVER_AS_71);
		put(RUNTIME_WILDFLY_80, SERVER_WILDFLY_80);
		put(RUNTIME_WILDFLY_90, SERVER_WILDFLY_90);
		put(RUNTIME_WILDFLY_100, SERVER_WILDFLY_100);
		put(RUNTIME_WILDFLY_110, SERVER_WILDFLY_110);
		put(RUNTIME_WILDFLY_120, SERVER_WILDFLY_120);
		put(RUNTIME_WILDFLY_130, SERVER_WILDFLY_130);
		put(RUNTIME_WILDFLY_140, SERVER_WILDFLY_140);
		put(RUNTIME_WILDFLY_150, SERVER_WILDFLY_150);
		put(RUNTIME_WILDFLY_160, SERVER_WILDFLY_160);
		put(RUNTIME_WILDFLY_170, SERVER_WILDFLY_170);
		put(RUNTIME_WILDFLY_180, SERVER_WILDFLY_180);
		put(RUNTIME_WILDFLY_190, SERVER_WILDFLY_190);

		put(RUNTIME_EAP_43, SERVER_EAP_43);
		put(RUNTIME_EAP_50, SERVER_EAP_50);
		put(RUNTIME_EAP_60, SERVER_EAP_60);
		put(RUNTIME_EAP_61, SERVER_EAP_61);
		put(RUNTIME_EAP_70, SERVER_EAP_70);
		put(RUNTIME_EAP_71, SERVER_EAP_71);
		put(RUNTIME_EAP_72, SERVER_EAP_72);
		put(RUNTIME_EAP_73, SERVER_EAP_73);
    }};
	
	
	// TODO move elsewhere
	public static final String EXT_JAR = ".jar";


}
