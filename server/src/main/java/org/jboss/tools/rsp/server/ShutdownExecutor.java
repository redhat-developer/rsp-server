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
