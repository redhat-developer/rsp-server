package org.jboss.tools.rsp.server.util.generation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DeploymentGeneration {
	public boolean createWar(File destination) {
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
		
		try {
			
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
			
			ZipEntry e = new ZipEntry("index.jsp");
			out.putNextEntry(e);
			byte[] data = indexjsp.getBytes();
			out.write(data, 0, data.length);
			out.closeEntry();
			
			ZipEntry e2 = new ZipEntry("META-INF/MANIFEST.MF");
			out.putNextEntry(e2);
			byte[] data2 = manifest.getBytes();
			out.write(data, 0, data2.length);
			out.closeEntry();

			ZipEntry e3 = new ZipEntry("WEB-INF/web.xml");
			out.putNextEntry(e3);
			byte[] data3 = webxml.getBytes();
			out.write(data3, 0, data3.length);
			out.closeEntry();

			out.close();
			return true;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}
}
