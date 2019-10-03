package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;

public class WildFlyAttributeHelper {
	public static boolean overrideLaunchArgs(IServer server) {
		if( server.containsAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN) ) {
			return server.getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN, false);
		}
		if( server.containsAttribute(IJBossServerAttributes. OLD_LAUNCH_OVERRIDE_BOOLEAN) ) {
			return server.getAttribute(IJBossServerAttributes.OLD_LAUNCH_OVERRIDE_BOOLEAN, false);
		}
		return false;
	}


	public static  String getOverridenProgramArgs(IServer server) {
		if( server.containsAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_PROGRAM_ARGS) ) {
			return server.getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_PROGRAM_ARGS, (String)null);
		}
		if( server.containsAttribute(IJBossServerAttributes. OLD_LAUNCH_OVERRIDE_PROGRAM_ARGS) ) {
			return server.getAttribute(IJBossServerAttributes.OLD_LAUNCH_OVERRIDE_PROGRAM_ARGS, (String)null);
		}
		return null;
	}

	public static  String getOverridenVmArgs(IServer server) {
		if( server.containsAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_VM_ARGS) ) {
			return server.getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_VM_ARGS, (String)null);
		}
		if( server.containsAttribute(IJBossServerAttributes. OLD_LAUNCH_OVERRIDE_VM_ARGS) ) {
			return server.getAttribute(IJBossServerAttributes.OLD_LAUNCH_OVERRIDE_VM_ARGS, (String)null);
		}
		return null;
	}
}
