/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.runtime.core;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static final String DownloadRuntime_Unable_to_fetch_license="Unable to fetch license for {0}";
	public static final String JBossRuntimeLocator_Searching="Searching";
	public static final String RuntimeExtensionManager_Invalid_runtime="Invalid runtime: id={0}, name={1}, version={2}, url={3}";
	
	private Messages() {
	}
}
