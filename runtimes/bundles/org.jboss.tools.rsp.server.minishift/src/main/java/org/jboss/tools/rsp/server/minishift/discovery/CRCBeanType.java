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
