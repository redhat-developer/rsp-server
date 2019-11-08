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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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
	private boolean lockedByUs = false;
	private String lockContent;

	public DataLocationCore(String lockContent) {
		this(null, lockContent);
	}
	
	public DataLocationCore(File loc, String lockContent) {
		this.lockContent = lockContent;
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
			if( !migrateFolders(oldDefault, newDefault)) {
				return oldDefault;
			}
		}
		return newDefault;
	}
	
	private boolean migrateFolders(File oldDefault, File newDefault) {
		// There's data in the old default that can be copied to the new one
		try {
			newDefault.mkdirs();
			copyFolder(oldDefault.toPath(), newDefault.toPath());
			return true;
		} catch(IOException ioe) {
			// TODO log
			LOG.error("Unable to migrate workspace to new location", ioe);
			return false;
		}
	}

	private boolean shouldMigrate(File old, File newLoc) {
		if( old.exists() && old.list().length > 0 ) {
			if( !newLoc.exists() || newLoc.list().length == 0 ) {
				return true;
			}
		}
		return false;
	}
	
	public void copyFolder(Path src, Path dest) throws IOException {
		List<IOException> all = new ArrayList<>();
		try(Stream<Path> st = Files.walk(src)) {
			Iterator<Path> stIt = st.iterator();
			while(stIt.hasNext()) {
				Path srcIt = stIt.next();
				Path destIt = dest.resolve(src.relativize(srcIt));
				try {
					Files.copy(srcIt, destIt, StandardCopyOption.REPLACE_EXISTING);
				} catch(IOException ioe) {
					all.add(ioe);
				}
			}
		}
		if( all.size() > 0 ) {
			String msg = "";
			for( IOException one : all ) {
				msg += one.getMessage() + "\n";
			}
			throw new IOException(msg);
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
	
	protected File getLegacy1DefaultDataLocation() {
		File home = new File(System.getProperty(SYSPROP_USER_HOME));
		return new File(home, DATA_LOCATION_LEGACY_DEFAULT);
	}

	protected File getCurrentDefaultDataLocation() {
		File home = new File(System.getProperty(SYSPROP_USER_HOME));
		File root = new File(home, DATA_LOCATION_DEFAULT);
		String rspId = System.getProperty(SYSPROP_RSP_ID);
		if( rspId == null )
			rspId = RSP_ID_DEFAULT;
		File dataDir = new File(root, rspId);
		return dataDir;
	}

	public synchronized boolean isInUse() {
		return new File(getDataLocation(), ".lock").exists();
	}

	public synchronized boolean lock() throws IOException {
		File f = new File(getDataLocation(), ".lock");
		if( f.exists()) {
			throw new IOException("Workspace already locked");
		}
		f.deleteOnExit();
		Files.write(f.toPath(), this.lockContent.getBytes());
		this.lockedByUs = true;
		return true;
	}
	public synchronized boolean unlock() throws IOException {
		File f = new File(getDataLocation(), ".lock"); 
		if( f.exists()) {
			if( !lockedByUs ) {
				throw new IOException("Workspace can only be unlocked by its locker.");
			}
			boolean b = f.delete();
			if( b ) {
				lockedByUs = false;
			}
			return b;
		}
		return true;
	}
}
