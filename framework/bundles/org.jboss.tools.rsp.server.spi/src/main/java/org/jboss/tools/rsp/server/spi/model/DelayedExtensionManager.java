package org.jboss.tools.rsp.server.spi.model;

import java.util.ArrayList;

public class DelayedExtensionManager {
	private static DelayedExtensionManager INSTANCE = new DelayedExtensionManager();
	public static DelayedExtensionManager getDefault() {
		return INSTANCE;
	}
	
	private ArrayList<IDelayedExtension> list = new ArrayList<>();
	
	public interface IDelayedExtension {
		public void addExtensionsToModel();
	}
	
	public void addDelayedExtension(IDelayedExtension e) {
		list.add(e);
	}
	
	public IDelayedExtension[] getDelayedExtensions() {
		return (IDelayedExtension[]) list.toArray(new IDelayedExtension[list.size()]);
	}
	
}
