/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.util;

import java.util.function.Function;

import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class TestServerType extends AbstractServerType {

		private Function<IServer, IServerDelegate> delegateProvider;

		public TestServerType(String id, String name, String desc, Function<IServer, IServerDelegate> delegateProvider) {
			super(id, name, desc);
			this.delegateProvider = delegateProvider;
		}

		@Override
		public IServerDelegate createServerDelegate(IServer server) {
			return delegateProvider.apply(server);
		}
	}