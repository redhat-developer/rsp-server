/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.jdt.internal.launching.LibraryInfo;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.junit.Ignore;
import org.junit.Test;

public class LaunchingJarTest {

	private static final String PROPERTY_JAVA_HOME = "java.home";
	private static final String ENV_JAVA_HOME = "JAVA_HOME";
	private static final String JAVA_SPECIFICATION_VERSION = "java.specification.version";
	private static final String JAVA_SPECIFICATION_NAME = "java.specification.name";

	@Test
	public void testFindJavaExecutable() {
		String javaHome = getAssertedJavaHome();
		getAssertedJavaExecutable(javaHome);
	}

	@Test
	public void testLaunchingSupportExtraction() {
		String javaHome = getAssertedJavaHome();
		getAssertedJavaExecutable(javaHome);

		LaunchingSupportUtils util = new LaunchingSupportUtils();
		File launchingJar = getAssertedLaunchingSupportFile(util);

		launchingJar.delete();
		assertFalse(launchingJar.exists());
		getAssertedLaunchingSupportFile(util);
	}

	@Ignore("As of org.eclipse.jdt.launching_3.12.0.v20181119-1621 org.eclipse.jdt.internal.launching.support.LibraryDetector "
			+ "wont detect the boot classpath on Java >= 9. It should check 'java.class.path' where it's using '.boot.class.path'."
			+ "Extension and Endorsed dir dont exist any more.")
	@Test
	public void testLaunchingSupportLibraryDetector() {
		LaunchingSupportUtils util = new LaunchingSupportUtils();
		String javaHome = getAssertedJavaHome();
		File exe = getAssertedJavaExecutable(javaHome);
		File launchingJar = getAssertedLaunchingSupportFile(util);

		LibraryInfo info = util.runLaunchingSupportLibraryDetector(exe, launchingJar);
		assertNotNull(info);

		assertNotNull(info.getVersion());
		assertNotNull(info.getBootpath());
		assertTrue(info.getBootpath().length > 0);
		assertNotNull(info.getEndorsedDirs());
		assertTrue(info.getEndorsedDirs().length > 0);
		assertNotNull(info.getExtensionDirs());
		assertTrue(info.getExtensionDirs().length > 0);
	}

	@Test
	public void testLaunchingSupportSysprops() {
		String javaHome = getAssertedJavaHome();
		getAssertedJavaExecutable(javaHome);

		getAssertedLaunchingSupportFile(new LaunchingSupportUtils());

		IVMInstall svmTmp = StandardVMType.getDefault().createVMInstall("testId");
		assertNotNull(svmTmp);
		svmTmp.setInstallLocation(new File(javaHome));

		try {
			String[] props = new String[] { JAVA_SPECIFICATION_NAME, JAVA_SPECIFICATION_VERSION };
			Map<String, String> ret = svmTmp.evaluateSystemProperties(props, new NullProgressMonitor());
			assertNotNull(ret);
			assertNotNull(ret.get(JAVA_SPECIFICATION_NAME));
			assertNotNull(ret.get(JAVA_SPECIFICATION_VERSION));
		} catch (CoreException ce) {
			fail(ce.getMessage());
		}
	}

	private String getAssertedJavaHome() {
		String javaHome = System.getenv().get(ENV_JAVA_HOME);
		if (javaHome == null || javaHome.isEmpty()) {
			javaHome = System.getProperty(PROPERTY_JAVA_HOME);
		}
		assertNotNull("Java home is not set!", javaHome);
		assertTrue(new File(javaHome).exists());
		return javaHome;
	}

	private File getAssertedJavaExecutable(String javaHome) {
		File exe = StandardVMType.findJavaExecutable(new File(javaHome));
		assertNotNull(exe);
		return exe;
	}

	private File getAssertedLaunchingSupportFile(LaunchingSupportUtils launchSupport) {
		File launchingJar = launchSupport.getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());
		return launchingJar;
	}
}
