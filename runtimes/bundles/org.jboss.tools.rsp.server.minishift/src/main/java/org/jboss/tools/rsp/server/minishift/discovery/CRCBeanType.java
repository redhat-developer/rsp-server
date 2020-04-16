/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.discovery;

import org.jboss.tools.rsp.server.minishift.discovery.MinishiftVersionLoader.MinishiftVersions;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;

public class CRCBeanType extends MinishiftBeanType {

	protected CRCBeanType() {
		super("CRC", "CRC 1.X");
	}	
	
	@Override
	protected boolean isSupported(MinishiftVersions vers) {
		return vers.getCRCVersion() != null;
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		return MinishiftServerTypes.CRC_1X_ID;
	}

	@Override
	protected String getFullVersion(MinishiftVersions props) {
		if (props != null
				&& isSupported(props)
				&& props.getCRCVersion() != null) {
			return props.getCRCVersion();
		}
		return null;
	}
}
