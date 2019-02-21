/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

public class ShutdownExecutor {
	public static interface IShutdownHandler {
		public void shutdown();
	}
	
	private static ShutdownExecutor executor = new ShutdownExecutor();
	public static ShutdownExecutor getExecutor() {
		return executor;
	}
	
	public ShutdownExecutor() {
		currentHandler = getDefaultShutdownHandler();
	}
	
	private IShutdownHandler getDefaultShutdownHandler() {
		return new IShutdownHandler() {
			@Override
			public void shutdown() {
				System.exit(0);
			}
		};
	}
	
	private IShutdownHandler currentHandler;
	public void setHandler(IShutdownHandler handler) {
		this.currentHandler = handler;
	}
	
	public void shutdown() {
		currentHandler.shutdown();
	}
}
