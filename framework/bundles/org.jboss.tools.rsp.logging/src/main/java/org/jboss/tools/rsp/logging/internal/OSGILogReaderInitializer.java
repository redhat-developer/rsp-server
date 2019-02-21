/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSGILogReaderInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(OSGILogReaderInitializer.class);

	private ServiceListener serviceListener = null;
	private SLF4JLogReader logReader = new SLF4JLogReader();
	private ServiceTracker<LogReaderService, LogReaderService> tracker;

	public ServiceListener getServiceListener() {
		if (serviceListener == null)
			serviceListener = createServiceListener();
		return serviceListener;
	}

	public void addServiceListener(BundleContext context) {
		final String filter = "(objectclass=" + LogReaderService.class.getName() + ")";
		try {
			context.addServiceListener(getServiceListener(), filter);
		} catch (final InvalidSyntaxException e) {
			LOG.error("error adding service listener: ", e);
		}
	}

	public void addLogListenerToServices(BundleContext context) {
		LogReaderService[] services = getAllReaders(context);
		for (int i = 0; i < services.length; i++) {
			services[i].addLogListener(logReader);
		}
	}

	public void removeLogListenerToServices(BundleContext context) {
		LogReaderService[] services = getAllReaders(context);
		for (int i = 0; i < services.length; i++) {
			services[i].removeLogListener(logReader);
		}
	}

	private LogReaderService[] getAllReaders(BundleContext context) {
		ServiceTracker<LogReaderService, LogReaderService> tracker = getTracker(context);
		List<LogReaderService> list = new ArrayList<>();
		final Object[] currentReaders = tracker.getServices();
		if (currentReaders != null) {
			for (int index = 0; index < currentReaders.length; index++) {
				final LogReaderService reader = (LogReaderService) currentReaders[index];
				list.add(reader);
			}
		}
		return list.toArray(new LogReaderService[list.size()]);
	}

	private ServiceTracker<LogReaderService, LogReaderService> getTracker(BundleContext context) {
		if (tracker == null) {
			this.tracker = new ServiceTracker<>(context, LogReaderService.class.getName(), null);
			this.tracker.open();
		}
		return tracker;
	}

	public void dispose(BundleContext context) {
		removeLogListenerToServices(context);
		if (tracker != null) {
			tracker.close();
		}
	}

	private ServiceListener createServiceListener() {
		return new ServiceListener() {

			@Override
			public void serviceChanged(ServiceEvent event) {
				final ServiceReference<?> ref = event.getServiceReference();
				if (ref == null) {
					return;
				}

				final Bundle bundle = ref.getBundle();
				if (bundle == null) {
					return;
				}

				final BundleContext context = bundle.getBundleContext();
				if (context == null) {
					return;
				}

				final LogReaderService reader = (LogReaderService) context.getService(ref);
				if (reader != null) {
					if (event.getType() == ServiceEvent.REGISTERED) {
						LOG.debug("adding a log listener to {}", reader);
						reader.addLogListener(logReader);
					} else if (event.getType() == ServiceEvent.UNREGISTERING) {
						LOG.debug("removing a log listener from {}", reader);
						reader.removeLogListener(logReader);
					}
				}
			}
		};
	}

	public void init(BundleContext context) {
		addLogListenerToServices(context);
		addServiceListener(context);
	}
}
