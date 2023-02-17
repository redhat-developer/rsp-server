/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DeploymentAssemblyDiscovery {
	// Constants for discovering deployment assemblies
	private static final String RSP_SETTINGS_FOLDER = ".rsp";
	private static final String WTP_SETTINGS_FOLDER = ".settings";
	private static final String WTP_SETTINGS_ASSEMBLY_FILE = "org.eclipse.wst.common.component";

	public static interface IDeploymentAssembler {
		/*
		 * Return a DeploymentAssemblyFile if you can, or null if one does not exist. 
		 * Throw an IOException if an assembler should be available but it fails to load
		 * 
		 * @param reference
		 * @return
		 * @throws IOException
		 */
		public DeploymentAssemblyFile getAssemblerFile(DeployableReference reference) throws IOException;
	}

	private static final class RspAssembler implements IDeploymentAssembler {

		private File findRspAssemblyJson(String path) {
			String needle = ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE_DEFAULT;
			File f = new File(new File(path, RSP_SETTINGS_FOLDER), needle);
			if( f.exists()) {
				return f;
			}
			return null;
		}
		
		@Override
		public DeploymentAssemblyFile getAssemblerFile(DeployableReference reference) throws IOException {
			Map<String, Object>  opts = reference.getOptions();
			String assemblyFileS = opts == null ? null : (String)opts.get(ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE);
			File assemblyFile = assemblyFileS == null || assemblyFileS.isEmpty() ? null : new File(assemblyFileS);
			boolean optionEmpty = (assemblyFileS == null || assemblyFileS.isEmpty());
			if( optionEmpty) {
				assemblyFile = findRspAssemblyJson(reference.getPath());
			}
			boolean useAssembly = assemblyFile != null && assemblyFile.exists();
			if( useAssembly ) {
				try {
					String contents = readFile(assemblyFile);
					Map<String, Object> assemblyAsJson = new Gson().fromJson(contents, Map.class);
					DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
					return asObj;
				} catch( JsonSyntaxException jse ) {
					throw new IOException(jse);
				}
			}
			return null;
		}		
	}
	

	private static final class WtpAssembler implements IDeploymentAssembler {

		private String findWtpOutputDirFromComponentDescriptor(IMemento[] properties) {
			for( int i = 0; i < properties.length; i++ ) {
				String name = properties[i].getString("name");
				if( "java-output-path".equals(name)) {
					String outputDir = properties[i].getString("value");
					if( outputDir != null && outputDir.length() > 0) {
						if( outputDir.startsWith("/")) {
							outputDir = outputDir.substring(1);
						}
						int nextSlash = outputDir.indexOf("/");
						if( nextSlash != -1 ) {
							outputDir = outputDir.substring(nextSlash);
						}
					}
					return outputDir.startsWith("/") ? outputDir.substring(1) : outputDir;
				}
			}
			return null;
		}
		
		@Override
		public DeploymentAssemblyFile getAssemblerFile(DeployableReference reference) throws IOException {
			Map<String, Object>  opts = reference.getOptions();
			String assemblyFileS = opts == null ? null : (String)opts.get(ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE);
			boolean optionEmpty = (assemblyFileS == null || assemblyFileS.isEmpty());
			if( !optionEmpty) {
				// Should never be reached but whatever
				return null;
			}
			
			File f = new File(new File(reference.getPath(), WTP_SETTINGS_FOLDER), WTP_SETTINGS_ASSEMBLY_FILE);
			if( !f.exists()) {
				return null;
			}
			try {
				IMemento loaded = XMLMemento.loadMemento(f.getAbsolutePath());
				IMemento module = loaded.getChild("wb-module");
				if( module != null ) {
					IMemento[] properties = module.getChildren("property");
					String outputDir = findWtpOutputDirFromComponentDescriptor(properties);
					IMemento[] resources = module.getChildren("wb-resource");
					List<Map<String,String>> mappings = new ArrayList<Map<String,String>>();
					for( int i = 0; i < resources.length; i++ ) {
						String sourcePath = resources[i].getString("source-path");
						String deployPath = resources[i].getString("deploy-path");
						sourcePath = sourcePath.startsWith("/") ? sourcePath.substring(1) : sourcePath;
						if( sourcePath != null && deployPath != null ) {
							Map<String,String> map1 = new HashMap<String,String>();
							if( sourcePath.equals("src/main/java") || sourcePath.equals("src/main/java/")) {
								sourcePath = outputDir;
							}
							map1.put("source-path", sourcePath);
							map1.put("deploy-path", deployPath);
							mappings.add(map1);
						}
					}
					Map<String,Object> ret = new HashMap<String,Object>();
					ret.put("mappings", mappings);
					DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(ret);
					return asObj;
				}
			} catch(IOException ioe) {
				throw ioe;
			}
			return null;
		}		
	}

	private static String readFile(File file) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
		}
		return content;
	}
	
	private static final IDeploymentAssembler[] ASSEMBLER_LIST =
			new IDeploymentAssembler[] {
					new RspAssembler(), 
					new WtpAssembler(),
			}; 
	
	public static IDeploymentAssembler[] getAssemblers(DeployableReference reference) {
		return ASSEMBLER_LIST;
	}
	
}
