/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.variables;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class ServerStringVariableManager implements IStringVariableManager {
	private IServer server;
	private IExternalVariableResolver external;

	public static interface IExternalVariableResolver {
		public String getNonServerKeyValue(String key);
	}

	
	public ServerStringVariableManager(IServer server, IExternalVariableResolver external) {
		this.server = server;
		this.external = external;

	}

	@Override
	public IValueVariable getValueVariable(String name) {
		String ret = server.getAttribute(name, (String) null);
		if( ret == null && external != null ) {
			ret = external.getNonServerKeyValue(name);					
		}
		final String ret2 = ret;
		return ret2 == null ? null : new IValueVariable() {
			@Override
			public String getValue() {
				return ret2;
			}
		};
	}
	
	@Override
	public IDynamicVariable getDynamicVariable(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}