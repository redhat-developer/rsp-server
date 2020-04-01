/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.test.discovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.server.generic.discovery.ExplodedManifestDiscovery;
import org.junit.Test;

public class ExplodedManifestDiscoveryTest {

	private String getUnifiedManifestContents(String name, String version) {
		return 	"Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Bundle-Name: " + name + "\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Version: " + version + "\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
	}
	
	private String getNameManifestContents(String name) {
		return 	"Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Bundle-Name: " + name + "\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
	}

	private String getVersionManifestContents(String version) {
		return 	"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Version: " + version + "\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n";
	}

	@Test 
	public void testSingleFileDiscovery() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File propFile = new File(root, "file.mf");
		Files.write(propFile.toPath(), getUnifiedManifestContents("Wonka", "5.0.0.Final").getBytes());
		
		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.mf", false, "Bundle-Name", "Wonka",
				"file.mf", false, "Bundle-Version", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		ServerBean sb = discovery.createServerBean(root);
		assertNotNull(sb);
	}

	@Test 
	public void testSingleFileDiscoveryWrongName() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File propFile = new File(root, "file.mf");
		Files.write(propFile.toPath(), getUnifiedManifestContents("Weird", "5.0.0.Final").getBytes());
		
		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.mf", false, "Bundle-Name", "Wonka",
				"file.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));
	}

	@Test 
	public void testSingleFileDiscoveryWrongVersion() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File propFile = new File(root, "file.mf");
		Files.write(propFile.toPath(), getUnifiedManifestContents("Wonka", "4.0.0.Final").getBytes());
		
		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.mf", false, "Bundle-Name", "Wonka",
				"file.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));
	}
	

	@Test 
	public void testSeparateFileDiscovery() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		File versionFile = new File(root, "file.version.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Wonka").getBytes());
		Files.write(versionFile.toPath(), getVersionManifestContents("5.0.0.Final").getBytes());
		
		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		ServerBean sb = discovery.createServerBean(root);
		assertNotNull(sb);
	}

	@Test 
	public void testSeparateFileDiscoveryWrongName() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		File versionFile = new File(root, "file.version.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Weird").getBytes());
		Files.write(versionFile.toPath(), getVersionManifestContents("5.0.0.Final").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));

	}

	@Test 
	public void testSeparateFileDiscoveryWrongVersion() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		File versionFile = new File(root, "file.version.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Wonka").getBytes());
		Files.write(versionFile.toPath(), getVersionManifestContents("4.0.0.Final").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));
	}

	@Test 
	public void testNameFileMissing() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File versionFile = new File(root, "file.version.mf");
		Files.write(versionFile.toPath(), getNameManifestContents("5.0.0.Final").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));
	}
	

	@Test 
	public void testVersionFileMissing() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Wonka").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertFalse(discovery.isServerRoot(root));
	}
	

	@Test 
	public void testNameFileMissingNullMatch() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File versionFile = new File(root, "file.version.mf");
		Files.write(versionFile.toPath(), getVersionManifestContents("5.0.0.Final").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				null, false, null, null,
				"file.version.mf", false, "Bundle-Version", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		
		discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				null, false, null, null,
				"file.version.mf", true, "Bundle-Version", "5.0.");
		assertTrue(discovery.isServerRoot(root));
	}
	

	@Test 
	public void testVersionFileMissingNullMatch() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Wonka").getBytes());

		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				null, false, null, null);
		assertTrue(discovery.isServerRoot(root));
		
		discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.mf", false, "Bundle-Name", "Wonka",
				null, true, null, null);
		assertTrue(discovery.isServerRoot(root));
	}
	

	@Test 
	public void testSeparateFileDiscoveryWithGlob() throws IOException {
		/*
		 String id, String name, String serverAdapterTypeId, 
		 String nameFileString, boolean nameFileStringIsPattern, 
		 String nameKey, String requiredNamePrefix, 
		 String versionFileString, boolean versionFileStringIsPattern, 
		 String versionKey, String requiredVersionPrefix
		 */
		
		File root = Files.createTempDirectory(getClass().getName()).toFile();
		File nameFile = new File(root, "file.name.mf");
		File versionFile = new File(root, "file.version.mf");
		Files.write(nameFile.toPath(), getNameManifestContents("Wonka").getBytes());
		Files.write(versionFile.toPath(), getVersionManifestContents("5.0.0.Final").getBytes());
		
		ExplodedManifestDiscovery discovery = new ExplodedManifestDiscovery(
				"test.id", "TestName", "server.type.id", 
				"*.mf", true, "Bundle-Name", "Wonka",
				"*.mf", true, "Bundle-Version", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		ServerBean sb = discovery.createServerBean(root);
		assertNotNull(sb);
	}
}
