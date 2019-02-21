/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging;

public interface LoggingConstants {
	public static final String SYSPROP_LOG_LEVEL_FLAG = "rsp.log.level";

	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_DEBUG = 4;
}
