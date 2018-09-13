/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

public class RequiresInfoException extends Exception {

	private static final long serialVersionUID = 5050044329807740335L;
	private boolean checked = false;

	public RequiresInfoException(String msg) {
		super(msg);
	}

	public void setChecked() { 
		this.checked = true; 
	}

	public boolean getChecked() { 
		return this.checked; 
	}
}