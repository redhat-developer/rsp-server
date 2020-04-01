/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.test.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.persistence.DataLocationCore;
import org.jboss.tools.rsp.server.spi.model.IDataStoreModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.junit.Test;

public class ExtensionModelDiscoveryTest {
	private String getServerDefinitionText(String stName, String discoveryId, 
			String discoveryType, String discoveryName, 
			String discoveryNameFile, String discoveryNameKey, String discoveryNameRequirement,
			String discoveryVersionFile, String discoveryVersionKey, String discoveryVersionRequirement) {
		
		String ret = "{\n" + 
				"	\"serverTypes\": {\n" + 
				"		\"" + stName + "\": {\n" + 
				"			\"discoveries\": {\n" + 
				"				\""+discoveryId+"\": {\n" + 
				"					\"discoveryType\": \""+discoveryType+"\",\n" + 
				"					\"name\": \""+discoveryName+"\",\n";
		if( discoveryNameFile != null ) 
			ret += "					\"nameFile\": \""+discoveryNameFile+"\",\n";
		if( discoveryNameKey != null )
			ret += "					\"nameKey\": \""+discoveryNameKey+"\",\n";
		if( discoveryNameRequirement != null )
			ret += "					\"nameRequiredPrefix\": \""+discoveryNameRequirement+"\",\n"; 
		if( discoveryVersionFile != null )
			ret += 	"					\"versionFile\": \""+discoveryVersionFile+"\",\n"; 
		if( discoveryVersionKey != null )
			ret += 	"					\"versionKey\": \""+discoveryVersionKey+"\",\n"; 
		if( discoveryVersionRequirement != null )
			ret += 	"					\"versionRequiredPrefix\": \""+discoveryVersionRequirement+"\",\n";
		ret += 	"					\"lazyignoremesuperlazytest\": \"blah\"\n";

		ret += 
				"				}\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"}";
		return ret;
	}
	private String getUnifiedManifestContents(String name, String version) {
		return 	"Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Implementation-Title: " + name + "\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Implementation-Version: " + version + "\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
	}

	private String getNameManifestContents(String name) {
		return 	"Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Implementation-Title: " + name + "\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
	}

	private String getVersionManifestContents(String version) {
		return 	"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Implementation-Version: " + version + "\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n";
	}
	private String getNamePropertiesContents(String string) {
		return "Test1=test1val\nImplementation-Title=" + string;
	}


	private String getVersionPropertiesContents(String string) {
		return "Test2=test2val\nImplementation-Version=" + string;
	}


	private String getUnifiedPropertiesContents(String string, String string2) {
		return getNamePropertiesContents(string) + "\n" + getVersionManifestContents(string2);
	}

	private void makeFile(File folder, String jarName, String manifestContents) throws IOException {
		File dest = new File(folder, jarName);
		dest.getParentFile().mkdirs();
		Files.write(dest.toPath(), manifestContents.getBytes());
	}
	private void makeZip(File folder, String jarName, String manifestContents) throws IOException {
		File dest = new File(folder, jarName);
		dest.getParentFile().mkdirs();
		FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
			fos = new FileOutputStream(dest.getAbsolutePath());
	        zos = new ZipOutputStream(fos);
	        zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
	        zos.write(manifestContents.getBytes(), 0, manifestContents.getBytes().length);
	        zos.closeEntry();
        } finally {
        	try {
        		if( zos != null )
        			zos.close();
        	} catch(IOException ioe) {}
        	try {
        		if( fos != null ) 
        			fos.close();
        	} catch(IOException ioe) {}
        }
	}
	
	protected IServerManagementModel createAndRegister(String serverJson) throws IOException {
		File tmpFolder = Files.createTempDirectory(getClass().getName() + System.currentTimeMillis()).toFile();
		IDataStoreModel dsm = new DataLocationCore(tmpFolder, "blank");
		IServerManagementModel rspModel = new ServerManagementModel(dsm);
		GenericServerExtensionModel extModel = new GenericServerExtensionModel(rspModel, null, new ByteArrayInputStream(serverJson.getBytes()));
		
		assertNotNull(rspModel.getServerBeanTypeManager().getAllRegisteredTypes());
		assertEquals(rspModel.getServerBeanTypeManager().getAllRegisteredTypes().length, 0);
		extModel.registerExtensions();
		assertNotNull(rspModel.getServerBeanTypeManager().getAllRegisteredTypes());
		assertEquals(rspModel.getServerBeanTypeManager().getAllRegisteredTypes().length, 1);
		return rspModel;
	}
	
	@Test
	public void testJarManifest() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka.jar", "Implementation-Title", "Wonka", "lib/wonka.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Wonka", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka.jar", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testJarManifestSingleFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka.jar", "Implementation-Title", "Wonka", "lib/wonka.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Weird", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka.jar", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}

	@Test 
	public void testJarManifestSingleFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka.jar", "Implementation-Title", "Wonka", "lib/wonka.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Wonka", "3.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka.jar", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testJarManifestSeparateFileDiscovery() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");
		String versionManifest = getVersionManifestContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka1.jar", nameManifest);
		makeZip(serverRoot, "lib/wonka2.jar", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testJarManifestSeparateFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Weird");
		String versionManifest = getVersionManifestContents("5.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka1.jar", nameManifest);
		makeZip(serverRoot, "lib/wonka2.jar", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);

	}

	@Test 
	public void testJarManifestSeparateFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");
		String versionManifest = getVersionManifestContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka1.jar", nameManifest);
		makeZip(serverRoot, "lib/wonka2.jar", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}

	@Test 
	public void testJarManifestNameFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionManifestContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka2.jar", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testJarManifestVersionFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka1.jar", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testJarManifestNameFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				null,null,null, "lib/wonka2.jar", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionManifestContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka2.jar", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}
	

	@Test 
	public void testJarManifestVersionFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "jarManifest", "Wonka 4.x", 
				"lib/wonka1.jar", "Implementation-Title", "Wonka", null,null,null);
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeZip(serverRoot, "lib/wonka1.jar", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);

	}
	
	// Regular manifest
	
	@Test
	public void testManifest() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"META-INF/MANIFEST.MF", "Implementation-Title", "Wonka", "META-INF/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Wonka", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "META-INF/MANIFEST.MF", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testManifestSingleFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"META-INF/MANIFEST.MF", "Implementation-Title", "Wonka", "META-INF/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Weird", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "META-INF/MANIFEST.MF", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}

	@Test 
	public void testManifestSingleFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"META-INF/MANIFEST.MF", "Implementation-Title", "Wonka", "META-INF/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedManifestContents("Wonka", "3.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "META-INF/MANIFEST.MF", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testManifestSeparateFileDiscovery() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");
		String versionManifest = getVersionManifestContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/MANIFEST.MF", nameManifest);
		makeFile(serverRoot, "folder2/MANIFEST.MF", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testManifestSeparateFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Weird");
		String versionManifest = getVersionManifestContents("5.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/MANIFEST.MF", nameManifest);
		makeFile(serverRoot, "folder2/MANIFEST.MF", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);

	}

	@Test 
	public void testManifestSeparateFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");
		String versionManifest = getVersionManifestContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/MANIFEST.MF", nameManifest);
		makeFile(serverRoot, "folder2/MANIFEST.MF", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}

	@Test 
	public void testManifestNameFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionManifestContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder2/MANIFEST.MF", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testManifestVersionFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/MANIFEST.MF", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testManifestNameFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				null,null,null, "folder2/MANIFEST.MF", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionManifestContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder2/MANIFEST.MF", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}
	

	@Test 
	public void testManifestVersionFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "manifest", "Wonka 4.x", 
				"folder1/MANIFEST.MF", "Implementation-Title", "Wonka", null,null,null);
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNameManifestContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/MANIFEST.MF", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);

	}


	@Test
	public void testProperties() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder/test.properties", "Implementation-Title", "Wonka", "folder/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedPropertiesContents("Wonka", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder/test.properties", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testPropertiesSingleFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder/test.properties", "Implementation-Title", "Wonka", "folder/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedPropertiesContents("Weird", "4.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder/test.properties", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	@Test 
	public void testPropertiesSingleFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder/test.properties", "Implementation-Title", "Wonka", "folder/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String manifestContents = getUnifiedPropertiesContents("Wonka", "3.3.2");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder/test.properties", manifestContents);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testPropertiesSeparateFileDiscovery() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNamePropertiesContents("Wonka");
		String versionManifest = getVersionPropertiesContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/test.properties", nameManifest);
		makeFile(serverRoot, "folder2/test.properties", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}

	@Test 
	public void testPropertiesSeparateFileDiscoveryWrongName() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNamePropertiesContents("Weird");
		String versionManifest = getVersionPropertiesContents("5.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/test.properties", nameManifest);
		makeFile(serverRoot, "folder2/test.properties", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);

	}

	@Test 
	public void testPropertiesSeparateFileDiscoveryWrongVersion() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNamePropertiesContents("Wonka");
		String versionManifest = getVersionPropertiesContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/test.properties", nameManifest);
		makeFile(serverRoot, "folder2/test.properties", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}

	@Test 
	public void testPropertiesNameFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionPropertiesContents("3.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder2/test.properties", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testPropertiesVersionFileMissing() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNamePropertiesContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/test.properties", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertFalse(matches);
	}
	

	@Test 
	public void testPropertiesNameFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				null,null,null, "folder2/test.properties", "Implementation-Version", "4.");
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String versionManifest = getVersionPropertiesContents("4.0.0.Final");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder2/test.properties", versionManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);
	}
	
	@Test 
	public void testPropertiesVersionFileMissingNullMatch() throws IOException {
		String serverJson = getServerDefinitionText(
				"o.j.t.wonka", "wonka.4", "properties", "Wonka 4.x", 
				"folder1/test.properties", "Implementation-Title", "Wonka", null,null,null);
		IServerManagementModel rspModel = createAndRegister(serverJson);

		String nameManifest = getNamePropertiesContents("Wonka");

		File serverRoot = Files.createTempDirectory(getClass().getName() + "_server").toFile();
		makeFile(serverRoot, "folder1/test.properties", nameManifest);
		boolean matches = rspModel.getServerBeanTypeManager().getAllRegisteredTypes()[0].isServerRoot(serverRoot);
		assertTrue(matches);

	}
}
