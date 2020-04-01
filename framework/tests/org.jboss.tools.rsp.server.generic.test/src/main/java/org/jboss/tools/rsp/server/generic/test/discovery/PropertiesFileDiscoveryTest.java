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
import org.jboss.tools.rsp.server.generic.discovery.PropertiesFileDiscovery;
import org.junit.Test;

public class PropertiesFileDiscoveryTest {

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
		File propFile = new File(root, "file.properties");
		Properties singleProps = new Properties();
		singleProps.put("a", "b");
		singleProps.put("serverName", "Wonka");
		singleProps.put("serverVersion", "5.0.");
		singleProps.put("c", "d");
		singleProps.store(new FileOutputStream(propFile), null);
		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.properties", false, "serverName", "Wonka",
				"file.properties", false, "serverVersion", "5.0.");
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
		File propFile = new File(root, "file.properties");
		Properties singleProps = new Properties();
		singleProps.put("a", "b");
		singleProps.put("serverName", "Weird");
		singleProps.put("serverVersion", "5.0.");
		singleProps.put("c", "d");
		singleProps.store(new FileOutputStream(propFile), null);
		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.properties", false, "serverName", "Wonka",
				"file.properties", false, "serverVersion", "5.0.");
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
		File propFile = new File(root, "file.properties");
		Properties singleProps = new Properties();
		singleProps.put("a", "b");
		singleProps.put("serverName", "Wonka");
		singleProps.put("serverVersion", "6.0.");
		singleProps.put("c", "d");
		singleProps.store(new FileOutputStream(propFile), null);
		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.properties", false, "serverName", "Wonka",
				"file.properties", false, "serverVersion", "5.0.");
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
		File nameFile = new File(root, "file.name.properties");
		File versionFile = new File(root, "file.version.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Wonka");
		nameProps.store(new FileOutputStream(nameFile), null);

		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "5.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				"file.version.properties", false, "serverVersion", "5.0.");
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
		File nameFile = new File(root, "file.name.properties");
		File versionFile = new File(root, "file.version.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Weird");
		nameProps.store(new FileOutputStream(nameFile), null);

		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "5.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				"file.version.properties", false, "serverVersion", "5.0.");
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
		File nameFile = new File(root, "file.name.properties");
		File versionFile = new File(root, "file.version.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Wonka");
		nameProps.store(new FileOutputStream(nameFile), null);

		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "6.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				"file.version.properties", false, "serverVersion", "5.0.");
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
		File versionFile = new File(root, "file.version.properties");
		
		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "5.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				"file.version.properties", false, "serverVersion", "5.0.");
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
		File nameFile = new File(root, "file.name.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Wonka");
		nameProps.store(new FileOutputStream(nameFile), null);

		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				"file.version.properties", false, "serverVersion", "5.0.");
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
		File versionFile = new File(root, "file.version.properties");
		
		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "5.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				null, false, null, null,
				"file.version.properties", false, "serverVersion", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		
		discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				null, false, null, null,
				"file.version.properties", true, "serverVersion", "5.0.");
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
		File nameFile = new File(root, "file.name.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Wonka");
		nameProps.store(new FileOutputStream(nameFile), null);

		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
				null, false, null, null);
		assertTrue(discovery.isServerRoot(root));
		
		discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"file.name.properties", false, "serverName", "Wonka",
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
		File nameFile = new File(root, "file.name.properties");
		File versionFile = new File(root, "file.version.properties");
		
		Properties nameProps = new Properties();
		nameProps.put("a", "b");
		nameProps.put("serverName", "Wonka");
		nameProps.store(new FileOutputStream(nameFile), null);

		Properties versionProps = new Properties();
		versionProps.put("serverVersion", "5.0.");
		versionProps.put("c", "d");
		versionProps.store(new FileOutputStream(versionFile), null);

		
		PropertiesFileDiscovery discovery = new PropertiesFileDiscovery(
				"test.id", "TestName", "server.type.id", 
				"*.properties", true, "serverName", "Wonka",
				"*.properties", true, "serverVersion", "5.0.");
		assertTrue(discovery.isServerRoot(root));
		ServerBean sb = discovery.createServerBean(root);
		assertNotNull(sb);
	}
}
