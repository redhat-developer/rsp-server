/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class GateIn33AS71DefaultLaunchArguments extends JBoss71DefaultLaunchArguments {
	public GateIn33AS71DefaultLaunchArguments(IServer s) {
		super(s);
	}
	
	@Override
	public String getStartDefaultProgramArgs() {
		return DASH + JB7_MP_ARG + SPACE + QUOTE 
				+ getServerHome().append(MODULES).toOSString() 
				+ ":" //$NON-NLS-1$
				+ getServerHome().append("gatein").append(MODULES).toOSString()  //$NON-NLS-1$
				+ QUOTE 
				+ getLoggingProgramArg()
				+ SPACE + DASH + JB7_JAXPMODULE + SPACE + JB7_JAXP_PROVIDER
				+ SPACE + JB7_STANDALONE_ARG;
	}
	
	@Override
	protected String getLoggingProgramArg() {
		// logging params removed
		return new String();
	}
	
	protected String getJBossJavaFlags() {
		IPath basedir = getBaseDirectory();
		IPath gateInConfig = 
				basedir.append("configuration").append("gatein"); //$NON-NLS-1$ //$NON-NLS-2$
		String s1 = "-Dexo.conf.dir=" + QUOTE //$NON-NLS-1$
				+ gateInConfig.toOSString() + QUOTE + SPACE
				+ "-Dgatein.conf.dir=" + QUOTE  //$NON-NLS-1$
				+ gateInConfig.toOSString() + QUOTE  + SPACE 
				+ "-Dexo.conf.dir.name=gatein"  + SPACE //$NON-NLS-1$
				+ "-Dexo.product.developing=true";//$NON-NLS-1$
		return super.getJBossJavaFlags() + s1;
	}
}
