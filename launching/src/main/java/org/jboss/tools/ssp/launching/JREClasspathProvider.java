package org.jboss.tools.ssp.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.tools.ssp.eclipse.core.runtime.IPath;
import org.jboss.tools.ssp.eclipse.jdt.internal.launching.LibraryInfo;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.eclipse.jdt.launching.LibraryLocation;
import org.jboss.tools.ssp.internal.launching.LibraryInfoCache;

public class JREClasspathProvider {

	public static String[] getJREClasspath(IVMInstall vm) {
		List<String> ret = new ArrayList<>();
		LibraryLocation[] libs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		File vmInstallLocation= vm.getInstallLocation();
		if (vmInstallLocation != null) {
			LibraryInfo libraryInfo= LibraryInfoCache.getLibraryInfo(vmInstallLocation.getAbsolutePath());
			if (libraryInfo != null) {
				// only return endorsed and bootstrap classpath entries if we have the info
				// libraries in the 'ext' directories are not loaded by the boot class loader
				String[] extensionDirsArray = libraryInfo.getExtensionDirs();
				Set<String> extensionDirsSet = new HashSet<>();
				for (int i = 0; i < extensionDirsArray.length; i++) {
					extensionDirsSet.add(extensionDirsArray[i]);
				}
				for (int i = 0; i < libs.length; i++) {
					LibraryLocation location = libs[i];
					IPath libraryPath = location.getSystemLibraryPath();
					String dir = libraryPath.toFile().getParent();
					// exclude extension directory entries
					if (!extensionDirsSet.contains(dir)) {
						ret.add(libraryPath.toOSString());
					}
				}
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
}
