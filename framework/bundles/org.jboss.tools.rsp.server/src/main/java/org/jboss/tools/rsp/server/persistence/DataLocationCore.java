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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jboss.tools.rsp.server.spi.model.IDataStoreModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLocationCore implements IDataStoreModel {
	private static final Logger LOG = LoggerFactory.getLogger(DataLocationCore.class);
	public static final String SYSPROP_DATA_LOCATION = "org.jboss.tools.rsp.data";
	public static final String SYSPROP_RSP_ID = "org.jboss.tools.rsp.id";
	public static final String DATA_LOCATION_LEGACY_DEFAULT = ".org.jboss.tools.rsp.data";
	public static final String DATA_LOCATION_DEFAULT = ".rsp";
	public static final String RSP_ID_DEFAULT = "no_id";
	
	private static final String SYSPROP_USER_HOME = "user.home";

	private File fLocation = null;
	public DataLocationCore() {
		this(null);
	}

	public DataLocationCore(File loc) {
		if( loc == null ) {
			checkUpdateWorkspace(loc);
			this.fLocation = getDefaultDataLocation();
			//this.fLocation = getLegacy1DefaultDataLocation();
		} else {
			this.fLocation = loc;
		}
	}

	public File getDataLocation() {
		return fLocation;
	}
	
	/*
	 * Migrate a workspace if necessary
	 * Return the file to use as the data folder
	 */
	private File checkUpdateWorkspace(File chosen) {
		if( chosen != null ) 
			return chosen;
		
		// Chosen is null. That means use defaults. 
		// That means migrate old defaults to new ones
		File oldDefault = getLegacy1DefaultDataLocation();
		File newDefault = getCurrentDefaultDataLocation();
		if( shouldMigrate(oldDefault,newDefault) ) {
			// There's data in the old default that can be copied to the new one
			try {
				newDefault.mkdirs();
				copyFolder(oldDefault.toPath(), newDefault.toPath());
			} catch(IOException ioe) {
				// TODO log
				LOG.error("Unable to migrate workspace to new location", ioe);
				return oldDefault;
			}
		}
		return newDefault;
	}
	
	private boolean shouldMigrate(File old, File newLoc) {
		if( old.exists() && old.list().length > 0 ) {
			if( !newLoc.exists() || newLoc.list().length == 0 ) {
				return true;
			}
		}
		return false;
	}
	
	public  void copyFolder(Path src, Path dest) throws IOException {
	    Files.walk(src)
	        .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
	}

	private void copy(Path source, Path dest) {
	    try {
	        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
	    } catch (Exception e) {
	        throw new RuntimeException(e.getMessage(), e);
	    }
	}
	
	protected File getDefaultDataLocation() {
		File data = null;
		String prop = System.getProperty(SYSPROP_DATA_LOCATION);

		if (prop != null) {
			data = new File(prop);
		} else {
			data = getCurrentDefaultDataLocation();
		}

		if (!data.exists()) {
			data.mkdirs();
		}

		return data;
	}
	
	private File getLegacy1DefaultDataLocation() {
		File home = new File(System.getProperty(SYSPROP_USER_HOME));
		return new File(home, DATA_LOCATION_LEGACY_DEFAULT);
	}

	private File getCurrentDefaultDataLocation() {
		File home = new File(System.getProperty(SYSPROP_USER_HOME));
		File root = new File(home, DATA_LOCATION_DEFAULT);
		String rspId = System.getProperty(SYSPROP_RSP_ID);
		if( rspId == null )
			rspId = RSP_ID_DEFAULT;
		File dataDir = new File(root, rspId);
		return dataDir;
	}
}
