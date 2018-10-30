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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;

class RSPServerSocketLauncher<T> extends ServerSocketLauncher<T> {

	public RSPServerSocketLauncher(Object localService, Class<T> remoteInterface, Socket socket,
			PrintWriter tracing) throws IOException {
		super(localService, remoteInterface, socket, tracing);
	}

	/**
	 * Start a thread that listens for messages in the message producer and forwards them to the message consumer.
	 * 
	 * @param messageProducer - produces messages, e.g. by reading from an input channel
	 * @param messageConsumer - processes messages and potentially forwards them to other consumers
	 * @param executorService - the thread is started using this service
	 * @return a future that is resolved when the started thread is terminated, e.g. by closing a stream
	 */

	@Override
	protected Future<Void> startListeningCalled(StreamMessageProducer messageProducer, MessageConsumer messageConsumer,
			ExecutorService execService, Object remoteProxy, RemoteEndpoint remoteEndpoint) {
		
		CustomMessageProcessor reader = new CustomMessageProcessor(messageProducer, messageConsumer, remoteProxy);
		final Future<?> result = execService.submit(reader);
		return toFutureVoid(result, messageProducer);
	}
	
	private Future<Void> toFutureVoid(Future<?> result, StreamMessageProducer messageProducer) {
		return new Future<Void>() {
			@Override
			public Void get() throws InterruptedException, ExecutionException {
				return (Void) result.get();
			}

			@Override
			public Void get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return (Void) result.get(timeout, unit);
			}

			@Override
			public boolean isDone() {
				return result.isDone();
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if (mayInterruptIfRunning) {
					messageProducer.close();
				}
				return result.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() {
				return result.isCancelled();
			}
		};
	}
	
	public static class CustomMessageProcessor implements Runnable {
		private static final Logger LOG = Logger.getLogger(ConcurrentMessageProcessor.class.getName());

		private boolean isRunning;

		private final MessageProducer messageProducer;
		private final MessageConsumer messageConsumer;
		private final Object remoteProxy;
		
		public CustomMessageProcessor(MessageProducer messageProducer, MessageConsumer messageConsumer, Object remoteProxy) {
			this.messageProducer = messageProducer;
			this.messageConsumer = messageConsumer;
			this.remoteProxy = remoteProxy;
		}

		public void run() {
			if (isRunning()) {
				throw new IllegalStateException("The message processor is already running.");
			}
			setRunning(true);
			try {
				ClientThreadLocal.setActiveClient((RSPClient)remoteProxy);
				messageProducer.listen(messageConsumer);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				setRunning(false);
			}
		}
		
		private synchronized boolean isRunning() {
			return isRunning;
		}
		
		private synchronized void setRunning(boolean val) {
			isRunning = val;
		}

	}

}