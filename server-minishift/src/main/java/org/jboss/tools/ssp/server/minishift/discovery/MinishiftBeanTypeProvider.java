package org.jboss.tools.ssp.server.minishift.discovery;

import java.io.File;
import java.util.HashMap;

import org.jboss.tools.ssp.server.minishift.discovery.MinishiftVersionLoader.MinishiftVersions;
import org.jboss.tools.ssp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.ssp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.ssp.server.spi.discovery.ServerBeanType;

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
			if( location.isDirectory())
				return false;
			return getFullVersion(location) != null;
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
		
	}
}
