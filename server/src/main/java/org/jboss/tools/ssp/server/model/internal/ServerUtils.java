package org.jboss.tools.ssp.server.model.internal;

import java.util.List;
import java.util.Map;

import org.jboss.tools.ssp.api.ServerManagementAPIConstants;

public class ServerUtils {
	
	public static Class getAttributeTypeClass(String type) {
		if( ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return String.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_LIST.equals(type)) {
			return List.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_MAP.equals(type)) {
			return Map.class;
		}
		return null;
	}

}
