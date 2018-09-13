/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.beans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.test.util.IOUtil;
import org.junit.Assert;

/**
 * This class is intended to assist in the creation ONLY 
 * for servers and runtimes. It's scope includes:
 * 
 *   1) Creating mock folders for each server type
 *   2) Creating IServer objects out of either mock folders or legitimate installations
 *
 */
public class MockServerCreationUtilities extends Assert {
	
	private static HashMap<String, String> asSystemJar = new HashMap<String, String>();
	private static HashMap<String, String> serverRuntimeMap = new HashMap<String, String>();
	private static final String twiddle_suffix = ".mf.twiddle.jar";
	private static final String twiddle_3_2_8 = "3.2.8" + twiddle_suffix;
	private static final String twiddle_4_0_5 = "4.0.5" + twiddle_suffix;
	private static final String twiddle_4_2_3 = "4.2.3" + twiddle_suffix;
	private static final String twiddle_5_0_0 = "5.0.0" + twiddle_suffix;
	private static final String twiddle_5_0_1 = "5.0.1" + twiddle_suffix;
	private static final String twiddle_5_1_0 = "5.1.0" + twiddle_suffix;
	private static final String twiddle_6_0_0 = "6.0.0" + twiddle_suffix;
	// TODO: get rid of the jars since they're not loaded any more & wf13,wf14 are not included
	private static final String as_server_7_0_jar = "7.0.0.mf.jboss-as-server.jar";
	private static final String as_server_7_1_jar = "7.1.0.mf.jboss-as-server.jar";
	private static final String wildfly_8_0_jar = "wf8.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_9_0_jar = "wf9.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_10_0_jar = "wf10.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_11_0_jar = "wf11.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_12_0_jar = "wf12.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_13_0_jar = "wf13.0.0.mf.jboss-as-server.jar";
	private static final String wildfly_14_0_jar = "wf14.0.0.mf.jboss-as-server.jar";
	private static final String twiddle_eap_4_3 = "eap4.3" + twiddle_suffix;
	private static final String twiddle_eap_5_0 = "eap5.0" + twiddle_suffix;
	private static final String twiddle_eap_5_1 = "eap5.1" + twiddle_suffix;
	private static final String eap_server_6_0_jar = "eap6.0.0.mf.jboss-as-server.jar";
	private static final String eap_server_6_1_jar = "eap6.1.0.mf.jboss-as-server.jar";
	private static final String eap_server_7_0_jar = "eap7.0.0.mf.jboss-as-server.jar";
	private static final String eap_server_7_1_jar = "eap7.1.0.mf.jboss-as-server.jar";
	private static final String jpp_server_6_0_jar = "jpp6.0.0.mf.jboss-as-server.jar";
	private static final String jpp_server_6_1_jar = "jpp6.1.0.mf.jboss-as-server.jar";
	private static final String gatein_3_4_0_jar = "gatein3.4.0.mf.jboss-as7-integration.jar";
	private static final String run_jar = "run.jar";
	private static final String service_xml = "service.xml";
	
	// ADDITIONAL NON_DEFAULT SERVER TYPES TO TEST
	public static final String TEST_SERVER_TYPE_GATEIN_34 = "TEST_SERVER_TYPE_GATEIN_34";
	public static final String TEST_SERVER_TYPE_GATEIN_35 = "TEST_SERVER_TYPE_GATEIN_35";
	public static final String TEST_SERVER_TYPE_GATEIN_36 = "TEST_SERVER_TYPE_GATEIN_36";
	public static final String TEST_SERVER_TYPE_JPP_60 = "TEST_SERVER_TYPE_JPP_60";
	
	/* Represents a mock structure for jpp 6.1 ER3,  NOT final!  */
	public static final String TEST_SERVER_TYPE_JPP_61 = "TEST_SERVER_TYPE_JPP_61";
	
	public static final String TEST_SERVER_TYPE_EAP_65 = "TEST_SERVER_TYPE_EAP_65";
	
	public static final String TEST_SERVER_TYPE_WONKA_1 = "TEST_SERVER_TYPE_WONKA_1_MISMATCH";
	
	
	public static final String[] TEST_SERVER_TYPES_TO_MOCK = new String[] { 
		TEST_SERVER_TYPE_GATEIN_34, 
		TEST_SERVER_TYPE_GATEIN_35,
		TEST_SERVER_TYPE_GATEIN_36,
		TEST_SERVER_TYPE_JPP_60,
		TEST_SERVER_TYPE_JPP_61,
		TEST_SERVER_TYPE_EAP_65,
		TEST_SERVER_TYPE_WONKA_1
	};
	
	static {
		asSystemJar.put(IServerConstants.SERVER_AS_32, twiddle_3_2_8);
		asSystemJar.put(IServerConstants.SERVER_AS_40, twiddle_4_0_5);
		asSystemJar.put(IServerConstants.SERVER_AS_42, twiddle_4_2_3);
		asSystemJar.put(IServerConstants.SERVER_AS_50, twiddle_5_0_0);
		asSystemJar.put(IServerConstants.SERVER_AS_51, twiddle_5_1_0);
		asSystemJar.put(IServerConstants.SERVER_AS_60, twiddle_6_0_0);
		asSystemJar.put(IServerConstants.SERVER_AS_70, as_server_7_0_jar);
		asSystemJar.put(IServerConstants.SERVER_AS_71, as_server_7_1_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_80, wildfly_8_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_90, wildfly_9_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_100, wildfly_10_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_110, wildfly_11_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_120, wildfly_12_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_130, wildfly_13_0_jar);
		asSystemJar.put(IServerConstants.SERVER_WILDFLY_140, wildfly_14_0_jar);
		asSystemJar.put(IServerConstants.SERVER_EAP_43, twiddle_eap_4_3);
		asSystemJar.put(IServerConstants.SERVER_EAP_50, twiddle_eap_5_1);
		asSystemJar.put(IServerConstants.SERVER_EAP_60, eap_server_6_0_jar);
		asSystemJar.put(IServerConstants.SERVER_EAP_61, eap_server_6_1_jar);
		asSystemJar.put(IServerConstants.SERVER_EAP_70, eap_server_7_0_jar);
		asSystemJar.put(IServerConstants.SERVER_EAP_71, eap_server_7_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_EAP_65, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_JPP_60, jpp_server_6_0_jar);
		asSystemJar.put(TEST_SERVER_TYPE_JPP_61, jpp_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_WONKA_1, eap_server_6_1_jar);
		asSystemJar.put(TEST_SERVER_TYPE_GATEIN_34, gatein_3_4_0_jar);
		// NEW_SERVER_ADAPTER Add the new runtime constant above this line
	}

	public static String[] getJBossServerTypeParameters() {
		return IServerConstants.ALL_JBOSS_SERVERS;
	}

	public static Object[] getJBossServerTypeParametersPlusAdditionalMocks() {
		ArrayList<Object> l = new ArrayList<Object>(Arrays.asList(getJBossServerTypeParameters()));
		l.addAll(Arrays.asList(TEST_SERVER_TYPES_TO_MOCK));
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	/*
	 * Only for use with JBoss servers, since deploy-only has no custom layout
	 */
	public static File createMockServerLayout(String serverType) {
		boolean isEap = false;
		if(serverType.startsWith(IServerConstants.EAP_SERVER_PREFIX))
			isEap = true;
		String name = serverType;
		File serverDir = null;
		if (IServerConstants.SERVER_AS_32.equals(serverType) ||
				IServerConstants.SERVER_AS_40.equals(serverType) ||
				IServerConstants.SERVER_AS_42.equals(serverType) ||
				IServerConstants.SERVER_AS_50.equals(serverType) ||
				IServerConstants.SERVER_AS_51.equals(serverType) ||
				IServerConstants.SERVER_AS_60.equals(serverType) ||
				IServerConstants.SERVER_EAP_43.equals(serverType) ||
				IServerConstants.SERVER_EAP_50.equals(serverType)) {
			name += (isEap ? "/jbossas" : "");
			serverDir = createAS6AndBelowMockServerDirectory(serverType + getRandomString(), 
					asSystemJar.get(serverType), "default");
		} else if (IServerConstants.SERVER_AS_70.equals(serverType)
				|| IServerConstants.SERVER_AS_71.equals(serverType)) {
			serverDir = createAS7StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_EAP_60.equals(serverType)) {
			serverDir = createEAP6StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_EAP_61.equals(serverType)) {
			serverDir = createAS72EAP61StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_EAP_70.equals(serverType)) {
			serverDir = createEAP70StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_EAP_71.equals(serverType)) {
			serverDir = createEAP71StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_80.equals(serverType)) {
			serverDir = createWildfly80MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_90.equals(serverType)) {
			serverDir = createWildfly90MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_100.equals(serverType)) {
			serverDir = createWildfly100MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_110.equals(serverType)) {
			serverDir = createWildfly110MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_120.equals(serverType)) {
			serverDir = createWildfly120MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_130.equals(serverType)) {
			serverDir = createWildfly130MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (IServerConstants.SERVER_WILDFLY_140.equals(serverType)) {
			serverDir = createWildfly140MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (TEST_SERVER_TYPE_GATEIN_34.equals(serverType)) {
			serverDir = createGateIn34MockServerDirectory(name);
		} else if (TEST_SERVER_TYPE_GATEIN_35.equals(serverType)) {
			serverDir = createGateIn35MockServerDirectory(name);
		} else if (TEST_SERVER_TYPE_GATEIN_36.equals(serverType)) {
			serverDir = createGateIn36MockServerDirectory(name);
		} else if (TEST_SERVER_TYPE_JPP_60.equals(serverType)) {
			serverDir = createJPP60MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (TEST_SERVER_TYPE_JPP_61.equals(serverType)) {
			serverDir = createJPP61MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (TEST_SERVER_TYPE_EAP_65.equals(serverType)) {
			serverDir = createAS72EAP65StyleMockServerDirectory(name, serverType, asSystemJar.get(serverType));
		} else if (TEST_SERVER_TYPE_WONKA_1.equals(serverType)) {
			serverDir = createAS72Wonka1MockServerDirectory(name, serverType, asSystemJar.get(serverType));
		}
		// NEW_SERVER_ADAPTER add mock folder structure above
		return serverDir;
	}
	
	private static File createAS6AndBelowMockServerDirectory(String name, String twiddleJar, String configurationName )  {
		File loc = new File(getMocksBaseDir(), name);
		loc.mkdirs();
		File bin = new File(loc, "bin");
		bin.mkdirs();
		File server = new File(loc, "server");
		server.mkdirs();
		new File(server, configurationName).mkdirs();
		File configConf = new File(server, "conf");
		configConf.mkdirs();
		
    	File twiddleLoc = getServerMockResource(twiddleJar);
    	File twiddleDest = new File(bin, "twiddle.jar");
		IOUtil.fileSafeCopy(twiddleLoc,twiddleDest);
		File runJar = getServerMockResource("run.jar");
		IOUtil.fileSafeCopy(runJar, new File(bin, "run.jar"));
		File serviceXml = getServerMockResource("jboss-service.xml");
		IOUtil.fileSafeCopy(serviceXml, new File(configConf,"jboss-service.xml"));
		return loc;
	}
	

	private static File createGateIn34MockServerDirectory(String name) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IServerConstants.SERVER_AS_71), null, null);
		File dest = new File(loc, "/gatein/modules/org/gatein/main/");
		dest.mkdirs();
		File serverJarLoc = getServerMockResource(asSystemJar.get(TEST_SERVER_TYPE_GATEIN_34));
		IOUtil.fileSafeCopy(serverJarLoc, new File(dest, "anything.jar"));
		return loc;
	}
	
	private static File createGateIn35MockServerDirectory(String name) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IServerConstants.SERVER_AS_71), null, null);
		String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
		File propFile = new File(loc, GATEIN_35_PROPERTY_FILE);
		propFile.getParentFile().mkdirs();
		try {
			IOUtil.setContents(propFile, "version=3.5.0");
		} catch(IOException ioe) {
			IOUtil.completeDelete(loc);
		}
		return loc;
	}
	private static File createGateIn36MockServerDirectory(String name) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, false, asSystemJar.get(IServerConstants.SERVER_AS_71), null, null);
		String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
		File propFile = new File(loc, GATEIN_35_PROPERTY_FILE);
		propFile.getParentFile().mkdirs();
		try {
			IOUtil.setContents(propFile, "version=3.6.0");
		} catch(IOException ioe) {
			IOUtil.completeDelete(loc);
		}
		return loc;
	}

	
	private static File createAS7StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, false, serverJar, null, null);
		return loc;
	}
	
	private static File createWildfly80MockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, true, serverJar, null, null);
		return loc;
	}
	
	private static File createWildfly90MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar,
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 9.0.0.Beta2\n");
	}

	private static File createWildfly100MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar, 
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 10.0.0.Final\n");
	}
	
	private static File createWildfly110MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar, 
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 11.0.0.Alpha1-SNAPSHOT\n");
	}
	
	private static File createWildfly120MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar, 
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 12.0.0.Alpha1-SNAPSHOT\n");
	}
	private static File createWildfly130MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar, 
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 13.0.0.xyz\n");
	}

	private static File createWildfly140MockServerDirectory(String name, String serverTypeId, String serverJar) {
		return createWildflyServerDirectory(name, serverTypeId, serverJar, 
				"JBoss-Product-Release-Name: WildFly Full\nJBoss-Product-Release-Version: 14.0.0.Final\n"); 
	}

	private static File createWildflyServerDirectory(String name, String serverTypeId, String serverJar, String manString) {
		File loc = new File(getMocksBaseDir(), name);
		createAS7xProductStructure(loc, true, serverJar, null, null);
		File productDir = new File(loc, "modules/system/layers/base/org/jboss/as/product/wildfly-full/dir/META-INF/");
		productDir.mkdirs();
		try {
			IOUtil.setContents(new File(productDir, "manifest.mf"), manString);
		} catch(IOException ioe) {
			
		}
		return loc;
	}

	private static File createAS72EAP61StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.1.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static File createEAP70StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.0.0.GA\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}

	private static File createEAP71StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: JBoss EAP\nJBoss-Product-Release-Version: 7.1.0.GA\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}
	
	private static File createAS72EAP65StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.5.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, true, serverJar, "eap", manString);
		return loc;
	}
	private static File createAS72Wonka1MockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String wonkaManString = "JBoss-Product-Release-Name: WONKA\nJBoss-Product-Release-Version: 1.0.0.Alpha\nJBoss-Product-Console-Slot: wonka";
		String eapManContents = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.1.1.GA\nJBoss-Product-Console-Slot: eap";

		createAS7xProductStructure(loc, true, serverJar, null, null);
		try {
			createProductConfASgt7(loc, "wonka", new String[]{"notwonka"});
			
			File metainf = new File(loc, getProductMetaInfFolderPath("eap", true));
			createProductMetaInfFolder(metainf, eapManContents);

			File metainf2 = new File(loc, getProductMetaInfFolderPath("wonka", true, "notwonka"));
			createProductMetaInfFolder(metainf2, wonkaManString);
		} catch(IOException ioe) {
			IOUtil.completeDelete(loc);
		}

		return loc;
	}
	

	private static File createEAP6StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.0.0.Alpha\nJBoss-Product-Console-Slot: eap";
		createAS7xProductStructure(loc, false, serverJar, "eap", manString);
		return loc;
	}
	
	private static File createJPP60MockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: Portal Platform\nJBoss-Product-Release-Version: 6.0.0.CR01\nJBoss-Product-Console-Slot: jpp";
		createAS7xProductStructure(loc, false, serverJar, "jpp", manString);
		return loc;
	}

	private static File createJPP61MockServerDirectory(String name, String serverTypeId, String serverJar) {
		File loc = new File(getMocksBaseDir(), name);
		String manString = "JBoss-Product-Release-Name: Portal Platform\nJBoss-Product-Release-Version: 6.1.0.ER03\nJBoss-Product-Console-Slot: jpp";
		createAS7xProductStructure(loc, true, serverJar, "jpp", manString);
		return loc;
	}

	private static void createAS7xProductStructure(File loc,  boolean includeLayers, String serverJar, String slot,
			String manifestContents ) {
		try {
			createStandaloneXML(loc);
			copyASSystemJar(loc, serverJar, includeLayers);
			new File(loc, "jboss-modules.jar").createNewFile();
			new File(loc, "bin").mkdirs();
			if( slot != null ) {
				createProductConfASgt7(loc, slot, null);
				createProductMetaInfFolder(loc, slot, includeLayers, manifestContents);
			}
		} catch(IOException ioe) {
			IOUtil.completeDelete(loc);
		}
	}
	
	private static void copyASSystemJar(File loc, String serverJar, boolean includeLayers)  {
		File serverJarBelongs = includeLayers ? 
				new File(loc, "modules/system/layers/base/org/jboss/as/server/main") :
				new File(loc, "modules/org/jboss/as/server/main");
		serverJarBelongs.mkdirs();
		File serverJarLoc = getServerMockResource(serverJar);
		if( serverJarLoc != null ) 
			IOUtil.fileSafeCopy(serverJarLoc, new File(serverJarBelongs,"anything.jar"));
	}

	private static void createProductMetaInfFolder(File loc, String slot, boolean includeLayers, String manifestContents) throws IOException {
		File metainf = new File(loc, getProductMetaInfFolderPath(slot, includeLayers));
		createProductMetaInfFolder(metainf, manifestContents);
	}

	private static void createProductMetaInfFolder(File metainf, String manifestContents) throws IOException {
		metainf.mkdirs();
		File manifest = new File(metainf, "MANIFEST.MF");
		IOUtil.setContents(manifest, manifestContents);		
	}

	private static String getProductMetaInfFolderPath(String slot, boolean includeLayers) {
		return getProductMetaInfFolderPath(slot, includeLayers, null);
	}

	private static String getProductMetaInfFolderPath(String slot, boolean includeLayers, String insideLayer) {
		if (insideLayer == null) {
			if (!includeLayers) {
				return "modules/org/jboss/as/product/" + slot + "/dir/META-INF";
			}
			return "modules/system/layers/base/org/jboss/as/product/" + slot + "/dir/META-INF";
		}
		// Now we know it lives inside a layer
		return "modules/system/layers/" + insideLayer + "/org/jboss/as/product/" + slot + "/dir/META-INF";
	}
	
	private static void createProductConfASgt7(File loc, String slot, String[] layers) throws IOException  {
		loc.mkdirs();
		File bin = new File(loc, "bin");
		bin.mkdirs();
		File productConf = new File(bin, "product.conf");
		IOUtil.setContents(productConf, "slot=" + slot);
		if (layers != null && layers.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("layers=");
			for (int i = 0; i < layers.length; i++) {
				sb.append(layers[i]);
				if( i < layers.length-1) {
					sb.append(",");
				}
			}
			File layersConf = new File(loc, "modules/layers.conf");
			new File(loc, "modules").mkdirs();
			IOUtil.setContents(layersConf, sb.toString());
		}
	}
	
	private static void createStandaloneXML(File as7Root) throws IOException {
		Path standaloneXml = as7Root.toPath().resolve("standalone").resolve("configuration").resolve("standalone.xml");
		standaloneXml.toFile().getParentFile().mkdirs();
		IOUtil.setContents(standaloneXml.toFile(), "<server></server>");
	}
	

	public static File getRandomAbsoluteFolder() {
		String random = getRandomString();
		return new File(getMocksBaseDir(), random);
	}
	
	private static String getRandomString() {
		return String.valueOf(System.currentTimeMillis());
	}
	/*
	 * Where to create our mocked servers
	 */
	public static File getMocksBaseDir() {
		// TODO 
		File ret =  new File(".", "src/test/resources/output/");
		ret.mkdirs();
		return ret;
	}

	private static File MOCK_TMP_DIR = null;
	/*
	 * Find a specific resource required to mock a server
	 */
	public static File getServerMockResource(String path) {
		if( MOCK_TMP_DIR == null ) {
			createTmpDir();
		}
		File expected = new File(MOCK_TMP_DIR, path); 
		if( !expected.exists()) {
			return extractToTmpDir(path);
		}
		return expected;
	}
	
	private static void createTmpDir() {
		try {
			MOCK_TMP_DIR = Files.createTempDirectory("serverMock").toFile();
			MOCK_TMP_DIR.mkdirs();
		} catch(IOException ioe) {
			throw new RuntimeException();
		}

	}

	protected static File extractToTmpDir(String path) {
		File pathFile = new File(MOCK_TMP_DIR, path);
		if( !pathFile.exists()) {
			ClassLoader classLoader = MockServerCreationUtilities.class.getClassLoader();
			InputStream is = classLoader.getResourceAsStream("serverMock/" + path);
			if( is == null )
				return null;
			
			try {
				Files.copy(is, pathFile.toPath());
			} catch(IOException ioe) {
				throw new RuntimeException();
			}
		}
		return pathFile;
	}
	
}
