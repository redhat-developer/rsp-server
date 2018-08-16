package org.jboss.tools.rsp.server.minishift.discovery;

import java.io.File;
import java.util.HashMap;

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftVersionLoader.MinishiftVersions;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class MinishiftBeanTypeProvider implements IServerBeanTypeProvider {

	ServerBeanType minishiftType = new MinishiftBeanType();
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		
		return new ServerBeanType[] {minishiftType};
	}
	public class MinishiftBeanType extends ServerBeanType {
		private HashMap<String,MinishiftVersions> versionsCache = new HashMap<>();
		
		
		protected MinishiftBeanType() {
			super("MINISHIFT", "Minishift 1.12+");
		}

		@Override
		public boolean isServerRoot(File location) {
			if( location.isFile()) {
				return getFullVersion(location) != null;
			}
			MinishiftDiscovery disc = new MinishiftDiscovery();
			if( disc.folderContainsMinishiftBinary(location)) {
				return true;
			}
			return false;
		}

		@Override
		public String getFullVersion(File root) {
			MinishiftVersions props = findOrLoad(root);
			if( props != null ) {
				String msVersion = props.getMinishiftVersion();
				if( msVersion != null ) {
					return msVersion;
				}
			}
			return null;
		}

		private MinishiftVersions findOrLoad(File root) {
			String path = root.getAbsolutePath();
			MinishiftVersions props = null;
			if( versionsCache.get(path) != null ) {
				props = versionsCache.get(path);
			} else {
				MinishiftDiscovery disc = new MinishiftDiscovery();
				if(disc.isMinishiftBinaryFile(root)) {
					props = MinishiftVersionLoader.getVersionProperties(root.getAbsolutePath());
					if( props != null ) {
						versionsCache.put(path, props);
					}
				}
			}
			return props;
		}

		@Override
		public String getUnderlyingTypeId(File root) {
			MinishiftVersions props = findOrLoad(root);
			if( props != null ) {
				if( props.getCDKVersion() != null ) {
					return "CDK";
				}
			}
			return "MINISHIFT";
		}

		@Override
		public String getServerAdapterTypeId(String version) {
			return MinishiftServerTypes.MINISHIFT_1_12_ID;
		}
		
		// TODO if the user put a folder here, find the right path
		public ServerBean createServerBean(File rootLocation) {
			if( rootLocation.isDirectory()) {
				MinishiftDiscovery disc = new MinishiftDiscovery();
				if( disc.folderContainsMinishiftBinary(rootLocation)) {
					rootLocation = disc.getMinishiftBinaryFromFolder(rootLocation);
				}
			}
			String version = getFullVersion(rootLocation);
			ServerBean server = new ServerBean(
					rootLocation.getPath(), getServerBeanName(rootLocation),
					getId(), getUnderlyingTypeId(rootLocation), version, 
					getMajorMinorVersion(version), getServerAdapterTypeId(version));
			return server;
		}
	}
}
