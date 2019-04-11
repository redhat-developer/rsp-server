/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.util.generation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DeploymentGeneration {
	public boolean createWar(File destination) {
		return createWar(destination, true);
	}
	public boolean createWar(File destination, boolean zip) {
		String webxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<web-app version=\"2.5\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\"\n" + 
				">\n" + 
				"	<!-- This is only here because Maven requires it to make a war. -->\n" + 
				"</web-app>";
		
		
		String indexjsp = "<html>\n" + 
				"<head>\n" + 
				"<title>Hello World!</title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"	<h1>Hello World!</h1>\n" + 
				"	<p>\n" + 
				"		It is now\n" + 
				"		<%= new java.util.Date() %></p>\n" + 
				"	<p>\n" + 
				"		You are coming from \n" + 
				"		<%= request.getRemoteAddr()  %></p>\n" + 
				"</body>";
		
		String manifest = "Manifest-Version: 1.0\n" + 
				"Archiver-Version: Plexus Archiver\n" + 
				"Created-By: Apache Maven\n" + 
				"Built-By: rob\n" + 
				"Build-Jdk: 1.8.0_151\n" + 
				"\n";
		
		Map<String, String> fileToContents = new HashMap<>();
		fileToContents.put("index.jsp", indexjsp);
		fileToContents.put("META-INF/MANIFEST.MF", manifest);
		fileToContents.put("WEB-INF/web.xml", webxml);
		
		if( zip ) {
			return zip(destination, fileToContents);
		} else {
			return writeFiles(destination, fileToContents);
		}
		
	}

	private boolean writeFiles(File destinationFolder, Map<String,String> contents) {
		for (Map.Entry<String,String> entry : contents.entrySet()) {
			String file = entry.getKey();
			String fileContents = entry.getValue();
			Path fileDest = destinationFolder.toPath().resolve(file);
			fileDest.getParent().toFile().mkdirs();
			try {
				Files.write(fileDest, fileContents.getBytes());
			} catch(IOException ioe) {
				ioe.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private boolean zip(File destinationZip, Map<String,String> contents) {
		try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destinationZip))) {
			for (Map.Entry<String,String> entry : contents.entrySet()) {
				String k = entry.getKey();
				String fileContents = entry.getValue();
				ZipEntry e = new ZipEntry(k);
				out.putNextEntry(e);
				byte[] data = fileContents.getBytes();
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			return true;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}
}
