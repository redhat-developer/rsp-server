/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.client;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.server.spi.client.MessageContextStore.MessageContext;


/**
 * This class is to keep track of which client is actively 
 * making a request on the server. 
 * 
 * While many tutorials online indicate not to use thread-local
 * with an executor service, and while the lsp launchers do use
 * an executor service, the fact is that each thread reads one
 * socket until that socket is closed, at which point the thread
 * may be repurposed for a new connection.
 * 
 * With that in mind, there can only possibly be one client per 
 * thread at any given time. 
 * 
 * Clients can use this to discover which client is making a given request
 * or to ensure all clients are authenticated for various purposes.
 * 
 * @author rob
 *
 */
public class ClientThreadLocal {
	 private static MessageContextStore<RSPClient> activeClient;
	 
	 public synchronized static MessageContextStore<RSPClient> getStore() {
		 if( activeClient == null ) {
			 activeClient = new MessageContextStore<>();
		 }
		 return activeClient;
	 }
	 
	 public synchronized static RSPClient getActiveClient() {
		 MessageContext<RSPClient> context = getStore().getContext();
		 return context == null ? null : context.getRemoteProxy();
	 }
	 
	 public synchronized static void setActiveClient(RSPClient client) {
		 getStore().setContext(new MessageContext<RSPClient>(client));
	 }
}
