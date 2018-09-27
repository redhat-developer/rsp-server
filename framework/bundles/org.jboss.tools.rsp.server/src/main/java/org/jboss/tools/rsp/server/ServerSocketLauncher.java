/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.jboss.tools.rsp.api.SocketLauncher;

/*
 * This class probably shouldn't be in this bundle, and should instead
 * be in the server bundle, but I was having problems getting it to compile
 * properly... so it's here instead. 
 */

public abstract class ServerSocketLauncher<T> extends SocketLauncher<T> {

	public ServerSocketLauncher(Object localService, Class<T> remoteInterface, Socket socket, PrintWriter tracing)
			throws IOException {
		super(localService, remoteInterface, socket, tracing);
	}
	

	protected Builder<T> createBuilder(Class<T> remoteInterface) {
		return new CustomBuilder<T>();
	}

	
	public class CustomBuilder<T> extends Builder<T> {
		@SuppressWarnings("unchecked")
		public Launcher<T> create() {
			if (input == null)
				throw new IllegalStateException("Input stream must be configured.");
			if (output == null)
				throw new IllegalStateException("Output stream must be configured.");
			if (localServices == null)
				throw new IllegalStateException("Local service must be configured.");
			if (remoteInterfaces == null)
				throw new IllegalStateException("Remote interface must be configured.");
			
			MessageJsonHandler jsonHandler = createJsonHandler();
			RemoteEndpoint remoteEndpoint = createRemoteEndpoint(jsonHandler);
			T remoteProxy;
			if (localServices.size() == 1 && remoteInterfaces.size() == 1) {
				remoteProxy = ServiceEndpoints.toServiceObject(remoteEndpoint, remoteInterfaces.iterator().next());
			} else {
				remoteProxy = (T) ServiceEndpoints.toServiceObject(remoteEndpoint, (Collection<Class<?>>) (Object) remoteInterfaces, classLoader);
			}
			StreamMessageProducer reader = new StreamMessageProducer(input, jsonHandler, remoteEndpoint);
			MessageConsumer messageConsumer = wrapMessageConsumer(remoteEndpoint);
			ExecutorService execService = executorService != null ? executorService : Executors.newCachedThreadPool();
			return createLauncher(reader, messageConsumer, execService, remoteProxy, remoteEndpoint);
		}

		private Launcher<T> createLauncher(StreamMessageProducer reader, MessageConsumer messageConsumer,
				ExecutorService execService, T remoteProxy, RemoteEndpoint remoteEndpoint) {
			return new Launcher<T> () {
				@Override
				public Future<Void> startListening() {
					return startListeningCalled(reader, messageConsumer, execService, remoteProxy, remoteEndpoint);
				}
				@Override
				public T getRemoteProxy() {
					return remoteProxy;
				}
	
				@Override
				public RemoteEndpoint getRemoteEndpoint() {
					return remoteEndpoint;
				}
			};
			
		}
	}
	protected abstract Future<Void> startListeningCalled(StreamMessageProducer reader, MessageConsumer messageConsumer,
			ExecutorService execService, Object remoteProxy, RemoteEndpoint remoteEndpoint);

}
