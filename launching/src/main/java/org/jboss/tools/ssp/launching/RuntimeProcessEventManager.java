package org.jboss.tools.ssp.launching;

import org.eclipse.debug.core.DebugEvent;

public class RuntimeProcessEventManager {
	private static RuntimeProcessEventManager instance = new RuntimeProcessEventManager();
	public static RuntimeProcessEventManager getDefault() {
		return instance;
	}
	public void fireDebugEventSet(DebugEvent[] debugEvents) {
		// TODO Auto-generated method stub
		// Code relevant to here is in DebugPlugin but it depends on jobs. Yuck.
	}
	
	
}
