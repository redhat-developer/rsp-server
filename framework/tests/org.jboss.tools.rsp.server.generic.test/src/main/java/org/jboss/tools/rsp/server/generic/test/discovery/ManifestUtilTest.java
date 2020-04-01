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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;
import org.junit.Test;

public class ManifestUtilTest {
	@Test
	public void testSearchPropertiesInputStream() {
		String propertiesFile = "key1=val1\nkey2=val2\nkey3=val3";
		ByteArrayInputStream is = new ByteArrayInputStream(propertiesFile.getBytes());
		try {
			String ret = ManifestUtility.searchPropertiesInputStream(is, new String[] {"key2"});
			assertEquals(ret, "val2");
		} catch(IOException ioe) {
			fail();
		}
	}

	@Test
	public void testSearchPropertiesMultipleKeysInputStream() {
		String propertiesFile = "key1=val1\nkey2=val2\nkey3=val3";
		ByteArrayInputStream is = new ByteArrayInputStream(propertiesFile.getBytes());
		try {
			String ret = ManifestUtility.searchPropertiesInputStream(is, new String[] {"key1", "key2"});
			assertEquals(ret, "val1");
		} catch(IOException ioe) {
			fail();
		}
	}

	@Test
	public void testSearchPropertiesMultipleKeysInputStream2() {
		String propertiesFile = "key1=val1\nkey2=val2\nkey3=val3";
		ByteArrayInputStream is = new ByteArrayInputStream(propertiesFile.getBytes());
		try {
			String ret = ManifestUtility.searchPropertiesInputStream(is, new String[] {"key2", "key1"});
			assertEquals(ret, "val2");
		} catch(IOException ioe) {
			fail();
		}
	}
	
	@Test 
	public void testManifest() {
		String contents = "Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Bundle-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Version: 0.22.7.Final\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
		
		String expectedResults = "o.j.t.r.s.g.test.GenericServerTestActivator";
		String results = ManifestUtility.getPropertyFromManifestContents(contents, "Bundle-Activator");
		assertEquals(results, expectedResults);
	}

	@Test 
	public void testManifestLastLineNoNewline() {
		String contents = "Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Bundle-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Version: 0.22.7.Final\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator";
		
		String expectedResults = null;
		String results = ManifestUtility.getPropertyFromManifestContents(contents, "Bundle-Activator");
		assertEquals(results, expectedResults);
	}
	
	@Test 
	public void testManifestInJar() {
		String contents = "Manifest-Version: 1.0\n" + 
				"Bundle-ManifestVersion: 2\n" + 
				"Bundle-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-SymbolicName: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Automatic-Module-Name: org.jboss.tools.rsp.server.generic.test.generic.test\n" + 
				"Bundle-Version: 0.22.7.Final\n" + 
				"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" + 
				"Bundle-Activator: o.j.t.r.s.g.test.GenericServerTestActivator\n";
		File dest = null;
		ZipOutputStream zos = null;
		FileOutputStream fos = null;
		try {
			dest = Files.createTempFile(getClass().getName(), ".jar").toFile();
			
			fos = new FileOutputStream(dest.getAbsolutePath());
            zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zos.write(contents.getBytes(), 0, contents.getBytes().length);
            zos.closeEntry();
		} catch(IOException ioe) {
			fail();
		} finally {
            try {
            	if( zos != null )
            		zos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
            	if( fos != null )
            		fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String expectedResults = "o.j.t.r.s.g.test.GenericServerTestActivator";
		String results = ManifestUtility.getManifestPropertiesFromZip(dest, "Bundle-Activator");
		assertEquals(results, expectedResults);
		
	}

}
