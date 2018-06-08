/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.launching.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.jboss.tools.ssp.eclipse.core.runtime.CoreException;
import org.jboss.tools.ssp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.ssp.eclipse.jdt.internal.launching.LibraryInfo;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall3;
import org.jboss.tools.ssp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.ssp.launching.LaunchingSupportUtility;
import org.jboss.tools.ssp.launching.util.FileUtil;
import org.junit.Test;

public class LaunchingJarTest {

    private static final String JAVA_HOME = "JAVA_HOME";
	private static final String JAVA_SPECIFICATION_VERSION = "java.specification.version";
	private static final String JAVA_SPECIFICATION_NAME = "java.specification.name";

	@Test
    public void testLaunchingSupportExtraction() {
		String javaHome = getAssertedJavaHome();
		getAssertedJavaExecutable(javaHome);
		
		LaunchingSupportUtility util = new LaunchingSupportUtility();
		File launchingJar = getAssertedLaunchingSupportFile(util);

		FileUtil.deleteDirectory(util.getLaunchingSupportFile().getParentFile(), true);
		assertFalse(launchingJar.exists());

		getAssertedLaunchingSupportFile(util);
    }

    @Test
    public void testLaunchingSupportLibraryDetector() {
		LaunchingSupportUtility util = new LaunchingSupportUtility();
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
		File javaExecutable = getAssertedJavaExecutable(javaHome);
		
		getAssertedLaunchingSupportFile(new LaunchingSupportUtility());

		IVMInstall svmTmp = StandardVMType.getDefault().createVMInstall("testId");
		assertNotNull(svmTmp);
		svmTmp.setInstallLocation(javaExecutable);
		
		assertTrue(svmTmp instanceof IVMInstall3);
		IVMInstall3 svm = (IVMInstall3) svmTmp;

		try {
			String[] props = new String[] {JAVA_SPECIFICATION_NAME, JAVA_SPECIFICATION_VERSION};
			Map<String, String> ret = svm.evaluateSystemProperties(props, new NullProgressMonitor());
			assertNotNull(ret);
			assertNotNull(ret.get(JAVA_SPECIFICATION_NAME));
			assertNotNull(ret.get(JAVA_SPECIFICATION_VERSION));
		} catch(CoreException ce) {
			fail(ce.getMessage());		
		}
	}

    private String getAssertedJavaHome() {
		String javaHome = System.getenv().get(JAVA_HOME);
		assertNotNull(JAVA_HOME + " is not set!", javaHome);
		return javaHome;
    }

    private File getAssertedJavaExecutable(String javaHome) {
		File f = new File(javaHome);
		File exe = StandardVMType.findJavaExecutable(f);
		assertNotNull(exe);
		return exe;
	}

	private File getAssertedLaunchingSupportFile(LaunchingSupportUtility launchSupport) {
		File launchingJar;
		launchingJar = launchSupport.getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());
		return launchingJar;
	}
}