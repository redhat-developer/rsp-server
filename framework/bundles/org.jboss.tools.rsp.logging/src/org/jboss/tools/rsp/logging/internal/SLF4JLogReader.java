/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging.internal;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SLF4J-based log reader.
 */

public final class SLF4JLogReader implements LogListener {
	private static final Logger LOG = LoggerFactory.getLogger(SLF4JLogReader.class);

	@Override
	public void logged(final LogEntry entry) {
		final int level = entry.getLevel();
		switch (level) {
		case LogService.LOG_DEBUG: {
			SLF4JLogReader.debug(entry);
			break;
		}
		case LogService.LOG_ERROR: {
			SLF4JLogReader.error(entry);
			break;
		}
		case LogService.LOG_INFO: {
			SLF4JLogReader.info(entry);
			break;
		}
		case LogService.LOG_WARNING: {
			SLF4JLogReader.warn(entry);
			break;
		}
		default: {
			SLF4JLogReader.warn(entry);
			break;
		}
		}
	}

	private static void warn(final LogEntry entry) {
		final String name = entry.getBundle().getSymbolicName();
		final String message = entry.getMessage();
		final Throwable ex = entry.getException();
		if (ex != null) {
			SLF4JLogReader.LOG.warn("[{}]: {}: ", name, message, ex);
		} else {
			SLF4JLogReader.LOG.warn("[{}]: {}", name, message);
		}
	}

	private static void info(final LogEntry entry) {
		final String name = entry.getBundle().getSymbolicName();
		final String message = entry.getMessage();
		final Throwable ex = entry.getException();
		if (ex != null) {
			SLF4JLogReader.LOG.info("[{}]: {}: ", name, message, ex);
		} else {
			SLF4JLogReader.LOG.info("[{}]: {}", name, message);
		}
	}

	private static void error(final LogEntry entry) {
		final String name = entry.getBundle().getSymbolicName();
		final String message = entry.getMessage();
		final Throwable ex = entry.getException();
		if (ex != null) {
			SLF4JLogReader.LOG.error("[{}]: {}: ", name, message, ex);
		} else {
			SLF4JLogReader.LOG.error("[{}]: {}", name, message);
		}
	}

	private static void debug(final LogEntry entry) {
		final String name = entry.getBundle().getSymbolicName();
		final String message = entry.getMessage();
		final Throwable ex = entry.getException();
		if (ex != null) {
			SLF4JLogReader.LOG.debug("[{}]: {}: ", name, message, ex);
		} else {
			SLF4JLogReader.LOG.debug("[{}]: {}", name, message);
		}
	}
}