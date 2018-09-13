package org.jboss.tools.rsp.server.minishift.discovery;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinishiftDiscovery {
	private static final Pattern WHITELIST_PATTERN = Pattern.compile("cdk-[0-9][.][0-9].*-minishift-(linux|darwin|windows)-amd64(.exe)?");

	private static final String MINISHIFT = "minishift";
	private static final String MINISHIFT_EXE = "minishift.exe";

	public boolean isMinishiftBinaryFile(File file) {
		String name = file.getName();
		if( file.isFile() && file.exists() && file.canExecute()) {
			if( name.equals(MINISHIFT) || name.equals( MINISHIFT_EXE))
				return true;
		}
		return false;
	}
	
	public File getMinishiftBinaryFromFolder(File root) {
		File ms = new File(root, MINISHIFT);
		if( ms.exists()) 
			return ms;
		return folderWhiteListBin(root);
	}
	
	public boolean folderContainsMinishiftBinary(File f) {
		File bin = getMinishiftBinaryFromFolder(f);
		return bin != null && bin.exists() && bin.isFile();
	}
	
	public File getMinishiftBinaryInFolder(File folder) {
		return folderWhiteListBin(folder);
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
