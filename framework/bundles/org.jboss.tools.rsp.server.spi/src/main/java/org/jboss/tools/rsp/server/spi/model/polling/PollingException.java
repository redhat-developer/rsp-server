/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

public class PollingException extends Exception {

	private static final long serialVersionUID = -7830978018908940551L;

	public PollingException(String message) {
		super(message);
	}

	public PollingException(String message, Throwable t) {
		super(message, t);
	}
}