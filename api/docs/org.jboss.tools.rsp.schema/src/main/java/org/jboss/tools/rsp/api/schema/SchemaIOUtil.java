/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.api.schema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchemaIOUtil {
	public static String linePrefix(String original, String prefix ) {
		String after = ("\n" + original).replace("\n", "\n" + prefix).substring(1);
		return after;
	}
	
	public static String readFile(File file) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
		}
		return content;
	}

	public static boolean cleanFolder(Path folder) {
		boolean ret = true;
		File[] ts = folder.toFile().listFiles();
		for( int i = 0; i < ts.length; i++ ) {
			if( !ts[i].delete() ) 
				ret = false;
		}
		return ret;
	}

	public static String trimFirstLines(String contents, int numLines) {
		int beginning = -1;
		for( int i = 0; i < numLines; i++ ) {
			beginning = contents.indexOf('\n', beginning + 1);
			if( beginning == -1 ) {
				return "";
			}
		}
		return contents.substring(beginning).trim();
	}

	public static String safeReadFile(Path p) {
		if(p.toFile().exists()) {
			try {
				String content = new String(Files.readAllBytes(p));
				return content;
			} catch(IOException ioe) {
				// return ""
			}
		}
		return "";
	}
}
