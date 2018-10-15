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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RSPExtensionBundle implements BundleActivator {
	
	private static final Logger LOG = LoggerFactory.getLogger(RSPExtensionBundle.class);
	
	private BundleListener listener = null;

	protected abstract void addExtensions();
	protected abstract void removeExtensions();

	protected void addExtensions(final String symbolicName, final BundleContext context) {
		Bundle bundle = getBundle(symbolicName, context);
		if (bundle == null) {
			LOG.debug("No extension added. Bundle {} not found.", symbolicName);
			return;
		}

		if (bundle.getState() == Bundle.ACTIVE) {
			LOG.debug("Adding extensions. Bundle {} is started.", symbolicName);
			addExtensions();
		} else {
			LOG.debug("Adding extensions once server bundle is started. Registering bundle listener");
			this.listener = event -> onBundleStarted(event, symbolicName, context);
			context.addBundleListener(listener);
		}
	}

	private void onBundleStarted(BundleEvent event, String symbolicName, BundleContext context) {
		if (event.getBundle().getSymbolicName().equals(symbolicName) 
				&& event.getType() == BundleEvent.STARTED) {
				LOG.debug("Adding extensions. Bundle {} is started.", symbolicName);
				addExtensions();
				removeBundleListener(context);
		}
	}

	private void removeBundleListener(BundleContext context) {
		context.removeBundleListener(listener);
		this.listener = null;
	}

	protected void removeExtensions(final String symbolicName, final BundleContext context) {
		Bundle bundle = getBundle(symbolicName, context);
		if (bundle == null) {
			LOG.debug("No extension added. Bundle {} not found.", symbolicName);
			return;
		}

		if (bundle.getState() == Bundle.ACTIVE) {
			LOG.debug("Removing extensions from bundle {}.", symbolicName);
			removeExtensions();
		}
	}

	private Bundle getBundle(String symbolicName, BundleContext context) {
		if (isEmpty(symbolicName)
				|| context == null) {
			return null;
		}
		Bundle bundle = null;
		Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length && bundle == null; i++) {
			if (bundles[i].getSymbolicName().equals(symbolicName)) {
				bundle = bundles[i];
			}
		}
		return bundle;
	}

	private boolean isEmpty(String string) {
		return string == null
				|| string.isEmpty();
	}
	
}
