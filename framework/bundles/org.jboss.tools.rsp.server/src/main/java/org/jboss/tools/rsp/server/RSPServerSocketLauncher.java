/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.server.spi.client.MessageContextStore;
import org.jboss.tools.rsp.server.spi.client.MessageContextStore.MessageContext;

class RSPServerSocketLauncher<T> extends SocketLauncher<T> {

	public RSPServerSocketLauncher(Object localService, Class<T> remoteInterface, Socket socket,
			PrintWriter tracing) throws IOException {
		super(localService, remoteInterface, socket, tracing);
	}
	protected Builder<T> createBuilder(Class<T> remoteInterface) {
		MessageContextStore<T> contextStore = new MessageContextStore<T>();
		return createBuilder(contextStore);
	}
	
	protected Builder<T> createBuilder(MessageContextStore<T> store) {
		return new Builder<T>() {
			protected ConcurrentMessageProcessor createMessageProcessor(MessageProducer reader, 
					MessageConsumer messageConsumer, T remoteProxy) {
				return new CustomConcurrentMessageProcessor<T>(reader, messageConsumer, remoteProxy, store);
			}
		};
	}

	/*
	 * The custom message processor, which can make sure to persist which clients are 
	 * making a given request before propagating those requests to the server implementation. 
	 */
	public static class CustomConcurrentMessageProcessor<T> extends ConcurrentMessageProcessor {

		private T remoteProxy;
		private final MessageContextStore<T> threadMap;
		public CustomConcurrentMessageProcessor(MessageProducer reader, MessageConsumer messageConsumer,
				T remoteProxy, MessageContextStore<T> threadMap) {
			super(reader, messageConsumer);
			this.remoteProxy = remoteProxy;
			this.threadMap = threadMap;
		}

		protected void processingStarted() {
			super.processingStarted();
			if (threadMap != null) {
				threadMap.setContext(new MessageContext<T>(remoteProxy));
			}
		}

		protected void processingEnded() {
			super.processingEnded();
			if (threadMap != null)
				threadMap.clear();

		}
	}


}