package org.jboss.tools.ssp.server.wildfly.beans.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jboss.tools.ssp.server.wildfly.impl.util.JBossManifestUtility;

public class DataVirtualization6ServerBeanType extends ServerBeanTypeUnknownAS72Product {
	public DataVirtualization6ServerBeanType() {
		super( "DV",//$NON-NLS-1$
				"JBoss Data Virtualization",//$NON-NLS-1$
				AS7_MODULE_LAYERED_SERVER_MAIN);
	}	
	
	@Override
	public String getServerBeanName(File root) {
		// TODO bug in upstream; ICondition is not public (??) 
		return "JBoss Data Virtualization " + getFullVersion(root, null);
	}
	
	public String getFullVersion(File location, File systemJarFile) {
		String productSlot = getSlot(location);
		boolean hasDV = "dv".equalsIgnoreCase(productSlot);
		if( hasDV ) {
			List<String> layers = Arrays.asList(getLayers(location));
			if( layers.contains("dv") ) {
				String dvProductDir = "org.jboss.as.product.dv.dir";
				File[] modules = new File[]{new File(location, "modules")};
				String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(modules, dvProductDir, 
						"META-INF", "JBoss-Product-Release-Version");
				if( vers.startsWith("6."))
					return vers;
			}
		}
		return null;
	}
	
	public String getUnderlyingTypeId(File location, File systemFile) {
		if( getFullVersion(location, systemFile) != null ) 
			return "DV";
		return null;
	}
}
