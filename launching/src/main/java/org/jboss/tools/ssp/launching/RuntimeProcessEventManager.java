package org.jboss.tools.ssp.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;

public class RuntimeProcessEventManager {
	private static RuntimeProcessEventManager instance = new RuntimeProcessEventManager();
	
	private List<IDebugEventSetListener> listeners;
	
	
	
	public static RuntimeProcessEventManager getDefault() {
		return instance;
	}
	
	public RuntimeProcessEventManager() {
		listeners = new ArrayList<IDebugEventSetListener>();
	}
	
	public synchronized void addListener(IDebugEventSetListener listener) {
		if( !listeners.contains(listener)) 
			listeners.add(listener);
	}
	public synchronized void removeListener(IDebugEventSetListener listener) {
		listeners.remove(listener);
	}
	
	private synchronized List<IDebugEventSetListener> getListeners() {
		return new ArrayList<IDebugEventSetListener>(listeners);
	}
	
	public void fireDebugEventSet(DebugEvent[] debugEvents) {
		List<IDebugEventSetListener> toIt = getListeners();
		for( IDebugEventSetListener i : toIt) {
			i.handleDebugEvents(debugEvents);
		}
	}
}
