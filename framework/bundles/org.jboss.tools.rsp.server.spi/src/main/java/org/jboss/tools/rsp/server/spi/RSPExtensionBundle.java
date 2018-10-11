/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public abstract class RSPExtensionBundle implements BundleActivator {

	private BundleListener listener = null;
	protected abstract void addExtensions();
	
	protected void addExtensionsToBundle(BundleContext context, String bundleId) {
		Bundle b = null;
		Bundle[] all = context.getBundles();
		for( int i = 0; i < all.length && b == null; i++ ) {
			if( all[i].getSymbolicName().equals(bundleId)) {
				b = all[i];
			}
		}
		if( b.getState() != Bundle.ACTIVE) {
			listener = new BundleListener() {
				@Override
				public void bundleChanged(BundleEvent event) {
					if( event.getBundle().getSymbolicName().equals(bundleId)) {
						if( event.getType() == BundleEvent.STARTED ) {
							addExtensions();
							context.removeBundleListener(listener);
							listener = null;
						}
					}
				}
			};
			context.addBundleListener(listener);
		} else {
			addExtensions();
		}
	}

}
