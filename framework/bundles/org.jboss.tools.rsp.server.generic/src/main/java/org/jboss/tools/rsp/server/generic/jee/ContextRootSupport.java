/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.jee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;

public abstract class ContextRootSupport {

	/**
	 * The main entry point. Given a deployment, find all context roots available
	 * @param strat
	 * @param baseUrl
	 * @param deployableOutputName
	 * @param ds
	 * @return
	 */
	public abstract String[] getDeploymentUrls(String strat, String baseUrl, 
			String deployableOutputName, DeployableState ds);
	
	/**
	 * Is the provided file name representing a custom internal web descriptor
	 * that can locate the context root? 
	 * @param name
	 * @return
	 */
	protected abstract String[] getCustomWebDescriptorsRelativePath();

	/**
	 * Find the context root from an internal web descriptor
	 * @param descriptorContents
	 * @return
	 */
	protected abstract String findFromWebDescriptorString(String descriptorContents);
	
	protected String[] findFromDescriptor(DeployableState ds) {
		String source = ds.getReference().getPath();
		File src = new File(source);
		if( src.exists() && src.isFile()) {
			return findFromDescriptorInArchive(ds);
		}
		if( src.exists() && src.isDirectory()) {
			return findFromDescriptorInFolder(ds);
		}
		return null;
	}

	protected String[] findFromDescriptorInFolder(DeployableState ds) {
		String source = ds.getReference().getPath();
		File asFile = new File(source);
		String[] descriptors = getCustomWebDescriptorsRelativePath();
		ArrayList<String> ret = new ArrayList<>();
		for( int i = 0; i < descriptors.length; i++ ) {
			Path p = asFile.toPath().resolve(descriptors[i]);
			if( p.toFile().exists()) {
				addContextRootIfNotNull(p, ret);
			}
		}


		String[] earDescriptor = getApplicationDescriptorRelativePath();
		for( int i = 0; i < earDescriptor.length; i++ ) {
			Path p = asFile.toPath().resolve(earDescriptor[i]);
			if( p.toFile().exists()) {
				InputStream is = null;
				try {
					is = new FileInputStream(p.toFile());
					String asXml = readString(is);
			    	String[] ctxt = findFromEarDescriptorString(asXml);
			    	if( ctxt != null ) {
			    		ret.addAll(Arrays.asList(ctxt));
			    	}
				} catch(IOException ioe) {
				} finally {
					if( is != null ) {
						try {
							is.close();
						} catch(IOException ioe) {}
					}
				}
			}
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	protected void addContextRootIfNotNull(Path c, ArrayList<String> ret) {
		String found = findContextRoot(c);
		if( found != null )
			ret.add(found);
	}

	protected String findContextRoot(Path c) {
		try {
			return findFromWebDescriptorString(new String(Files.readAllBytes(c)));
		} catch(IOException ioe) {
			
		}
		return null;
	}

	protected String[] findFromDescriptorInArchive(DeployableState ds) {
		ArrayList<String> ret = new ArrayList<>();
		try {
			ZipFile zipFile = new ZipFile(ds.getReference().getPath());
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    List<String> customWebDescriptorList = Arrays.asList(getCustomWebDescriptorsRelativePath());
		    List<String> earDescriptorList = Arrays.asList(getApplicationDescriptorRelativePath());
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        if( customWebDescriptorList.contains(entry.getName())) {
		        	String contents = readString(zipFile.getInputStream(entry));
		        	String ctxt = findFromWebDescriptorString(contents);
		        	if( ctxt != null ) {
		        		ret.add(ctxt);
		        	}
		        } else if( earDescriptorList.contains(entry.getName())) {
		        	String contents = readString(zipFile.getInputStream(entry));
		        	String[] ctxt = findFromEarDescriptorString(contents);
		        	if( ctxt != null ) {
		        		ret.addAll(Arrays.asList(ctxt));
		        	}
		        }
		    }
		} catch(IOException ioe) {
			
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}
	
	protected String[] getApplicationDescriptorRelativePath() {
		return new String[] { "application.xml", "META-INF/application.xml"};
	}

	public static String readString(InputStream inputStream) throws IOException {
	    ByteArrayOutputStream into = new ByteArrayOutputStream();
	    byte[] buf = new byte[4096];
	    for (int n; 0 < (n = inputStream.read(buf));) {
	        into.write(buf, 0, n);
	    }
	    into.close();
	    return new String(into.toByteArray(), "UTF-8"); // Or whatever encoding
	}

	
	private String[] findFromEarDescriptorString(String contents) {
		ArrayList<String> ret = new ArrayList<>();
		XMLMemento mem = XMLMemento.createReadRoot(new ByteArrayInputStream(contents.getBytes()));
		IMemento[] modChildren = mem.getChildren("module");
		if( modChildren != null  ) {
			for( int i = 0; i < modChildren.length; i++ ) {
				IMemento[] webChildren = modChildren[i].getChildren("web");
				if( webChildren != null ) {
					for( int j = 0; j < webChildren.length; j++ ) {
						IMemento[] ctxtRootChildren = webChildren[j].getChildren("context-root");
						if( ctxtRootChildren != null ) {
							for( int k = 0; k < ctxtRootChildren.length; k++ ) {
								XMLMemento xml1 = (XMLMemento)ctxtRootChildren[k];
								String ctxtRoot = xml1.getTextData();
								if( ctxtRoot != null ) {
									ret.add(ctxtRoot);
								}
							}
						}
					}
				}
			}
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	

	protected String append(String context, String baseUrl) {
		return new String(removeTrailingSlash(baseUrl) + ensureStartsWithSlash(context));
	}

	protected String[] append(String[] contexts, String baseUrl) {
		ArrayList<String> ret = new ArrayList<>();
		for( int i = 0; i < contexts.length; i++ ) {
			ret.add(append(contexts[i], baseUrl));
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}
	

	protected String removeWarSuffix(String name) {
		String noSuffix = name;
		if( name.toLowerCase().endsWith(".war")) {
			noSuffix = noSuffix.substring(0, noSuffix.length()-4);
		}
		return noSuffix;
	}
	protected String ensureStartsWithSlash(String s) {
		return s.startsWith("/") ? s : "/" + s;
	}
	protected String removeTrailingSlash(String s) {
		return s.endsWith("/") ? s.substring(0,s.length()-1) : s;
	}
	
}
