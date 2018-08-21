package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.io.File;

import org.jboss.tools.rsp.launching.utils.FileUtil;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public abstract class JBossServerBeanType extends ServerBeanType implements IServerConstants, IJBossServerResourceConstants {
	protected String systemJarPath;
	protected JBossServerBeanType(String id, String name, String systemJarPath) {
		super(id, name);
		this.systemJarPath = systemJarPath;
	}

	@Override
	public abstract boolean isServerRoot(File location);

	@Override
	public String getUnderlyingTypeId(File root) {
		return getId();
	}

	@Override
	public abstract String getServerAdapterTypeId(String version);


	@Override
	public String getFullVersion(File root) {
		return getFullVersion(root, new File(root, systemJarPath));
	}
	
	public String getFullVersion(File root, File systemJar) {
		return JBossManifestUtility.getFullServerVersionFromZip(systemJar);
	}
	
	public String getFullServerVersionFromZipLegacy(File systemJarFile) {
		return JBossManifestUtility.getFullServerVersionFromZip(systemJarFile);
	}
	
	public static boolean scanFolderJarsForManifestProp(File location, String mainFolder, String property, String propPrefix) {
		return JBossManifestUtility.scanFolderJarsForManifestProp(location, mainFolder, property, propPrefix);
	}
	
	public static String asPath(String... vals) {
		return FileUtil.asPath(vals);
	}
	
	public static String getManifestPropFromFolderJars(File location, String mainFolder, String property) {
		return JBossManifestUtility.getManifestPropFromFolderJars(location, mainFolder, property);
	}
}
