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
    @Test
    public void testLaunchingSupportExtraction() {
		String javaHome = System.getenv().get("JAVA_HOME");
		assertNotNull(javaHome);
		File f = new File(javaHome);
		File exe = StandardVMType.findJavaExecutable(f);
		assertNotNull(exe);
		
		LaunchingSupportUtility util = new LaunchingSupportUtility();
		File launchingJar = util.getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());
		
		FileUtil.deleteDirectory(launchingJar.getParentFile(), true);
		assertFalse(launchingJar.exists());
		launchingJar = new LaunchingSupportUtility().getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());
		
    }
		
    @Test
    public void testLaunchingSupportLibraryDetector() {
		LaunchingSupportUtility util = new LaunchingSupportUtility();
		String javaHome = System.getenv().get("JAVA_HOME");
		assertNotNull(javaHome);
		File f = new File(javaHome);
		File exe = StandardVMType.findJavaExecutable(f);
		assertNotNull(exe);
		
		File launchingJar = util.getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());

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
		LaunchingSupportUtility util = new LaunchingSupportUtility();
		String javaHome = System.getenv().get("JAVA_HOME");
		assertNotNull(javaHome);
		File javaHomeFile = new File(javaHome);
		File exe = StandardVMType.findJavaExecutable(javaHomeFile);
		assertNotNull(exe);
		
		File launchingJar = util.getLaunchingSupportFile();
		assertNotNull(launchingJar);
		assertTrue(launchingJar.exists());

		
		IVMInstall svmTmp = StandardVMType.getDefault().createVMInstall("testId");
		assertNotNull(svmTmp);
		svmTmp.setInstallLocation(javaHomeFile);
		
		assertTrue(svmTmp instanceof IVMInstall3);
		IVMInstall3 svm = (IVMInstall3)svmTmp;
		
		
		String[] props = new String[] {"java.specification.name", "java.specification.version"};
		try {
			Map<String, String> ret = svm.evaluateSystemProperties(props, new NullProgressMonitor());
			assertNotNull(ret);
			assertNotNull(ret.get("java.specification.name"));
			assertNotNull(ret.get("java.specification.version"));
		} catch(CoreException ce) {
			fail(ce.getMessage());		
		}
    }

    
  }