/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

public class DaoUtilities {
	
	private DaoUtilities() {
	}
	
	public static Class<?> getAttributeTypeClass(String type) {
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
