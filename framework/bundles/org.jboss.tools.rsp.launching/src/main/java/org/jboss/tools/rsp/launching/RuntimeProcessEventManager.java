/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.eclipse.debug.core.DebugEvent;
import org.jboss.tools.rsp.eclipse.debug.core.IDebugEventSetListener;

public class RuntimeProcessEventManager {

	private static RuntimeProcessEventManager instance = new RuntimeProcessEventManager();
	
	private List<IDebugEventSetListener> listeners = new ArrayList<>();
	
	public static RuntimeProcessEventManager getDefault() {
		return instance;
	}
	
	public synchronized void addListener(IDebugEventSetListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	public synchronized void removeListener(IDebugEventSetListener listener) {
		listeners.remove(listener);
	}
	
	private synchronized List<IDebugEventSetListener> getListeners() {
		return new ArrayList<>(listeners);
	}
	
	public void fireDebugEventSet(DebugEvent[] debugEvents) {
		getListeners().forEach(listener -> listener.handleDebugEvents(debugEvents));
	}
}
