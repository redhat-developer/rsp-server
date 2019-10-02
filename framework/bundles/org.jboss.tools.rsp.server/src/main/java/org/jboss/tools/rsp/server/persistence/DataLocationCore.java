/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.persistence;

import java.io.File;

import org.jboss.tools.rsp.server.spi.model.IDataStoreModel;

public class DataLocationCore implements IDataStoreModel {

	public static final String SYSPROP_DATA_LOCATION = "org.jboss.tools.rsp.data";
	public static final String SYSPROP_DATA_DEFAULT_LOCATION = ".org.jboss.tools.rsp.data";
	private static final String SYSPROP_USER_HOME = "user.home";

	private File fLocation = null;
	public DataLocationCore() {
	}

	public DataLocationCore(File loc) {
		this.fLocation = loc;
	}

	public File getDataLocation() {
		if( fLocation != null ) 
			return fLocation;
		
		return getDefaultDataLocation();
	}
	
	protected File getDefaultDataLocation() {
		File data = null;
		String prop = System.getProperty(SYSPROP_DATA_LOCATION);

		if (prop != null) {
			data = new File(prop);
		} else {
			File home = new File(System.getProperty(SYSPROP_USER_HOME));
			data = new File(home, SYSPROP_DATA_DEFAULT_LOCATION);
		}

		if (!data.exists()) {
			data.mkdirs();
		}

		return data;
	}
}
