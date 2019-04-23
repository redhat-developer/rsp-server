/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.discovery;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinishiftDiscovery {

	private static final Logger LOG = LoggerFactory.getLogger(MinishiftDiscovery.class);

	private static final Pattern WHITELIST_PATTERN = Pattern.compile("cdk-[0-9][.][0-9].*-minishift-(linux|darwin|windows)-amd64(.exe)?");

	private static final String MINISHIFT = "minishift";
	private static final String MINISHIFT_EXE = "minishift.exe";

	public boolean isMinishiftBinaryFile(File file) {
		return isMinishiftBinaryFile(file, true);
	}
	
	public boolean isMinishiftBinaryFile(File file, boolean checkPermissions) {

		try {
			File resolvedFile = file.toPath().toRealPath().toFile();
			// must exist and be a file
			if(!resolvedFile.exists() || !resolvedFile.isFile())
				return false;
			
			// If we've been asked to check permissions and this file isn't executable, fail
			if( checkPermissions && !resolvedFile.canExecute()) 
				return false;
			
			String name = resolvedFile.getName();
			return name.equals(MINISHIFT) || name.equals(MINISHIFT_EXE) 
					|| whitelistMatchesName(name);
		} catch (IOException e) {
			LOG.error("Could not determine if {} is a minishift binary.", file.getAbsolutePath());
		}
		return false;
	}
	
	public File getMinishiftBinaryFromFolder(File root) {
		return getMinishiftBinaryFromFolder(root, true);
	}
	
	public File getMinishiftBinaryFromFolder(File root, boolean checkPermissions) {
		File ms = new File(root, MINISHIFT);
		if (isMinishiftBinaryFile(ms, checkPermissions)) 
			return ms;
		ms = new File(root, MINISHIFT_EXE);
		if (isMinishiftBinaryFile(ms, checkPermissions)) 
			return ms;
		return folderWhiteListBin(root);
	}
	
	public boolean folderContainsMinishiftBinary(File f) {
		File bin = getMinishiftBinaryFromFolder(f);
		return bin != null && bin.exists() && bin.isFile();
	}
	
	private File folderWhiteListBin(File folder) {
		if( folder == null || !folder.exists()) {
			return null;
		}
		String[] children = folder.list();
		for( int i = 0; i < children.length; i++ ) {
			if( whitelistMatchesName(children[i])) {
		    	 return new File(folder, children[i]);
		     }
		}
		return null;
	}
	
	private boolean whitelistMatchesName(String name) {
		Matcher m = WHITELIST_PATTERN.matcher(name);
		return m.matches();
	}
}
